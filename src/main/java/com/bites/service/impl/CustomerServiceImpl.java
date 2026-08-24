package com.bites.service.impl;

import com.bites.dto.response.CustomerResponse;
import com.bites.entity.Customer;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.CustomerMapper;
import com.bites.repository.CustomerOrderStatsProjection;
import com.bites.repository.CustomerRepository;
import com.bites.repository.OrderRepository;
import com.bites.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    // Used to look up a customer by phone number, and to save a brand new
    // customer row the first time a given phone number checks out.
    private final CustomerRepository customerRepository;

    // Converts Customer entity -> CustomerResponse DTO.
    private final CustomerMapper customerMapper;

    // Used to compute the Orders / Total Spent / Last Order columns on the
    // admin "Customers" screen.
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Customer findOrCreateByPhone(String name, String phone) {
        // Step 1:
        // Check if a customer with this phone number has ordered before.
        // Step 2:
        // If yes, just reuse that existing customer row — this keeps the
        // admin "Customers" screen showing one row per phone number, with
        // full order history, instead of a fresh row per order.
        // If no, create a brand new customer row right now and save it.
        log.debug("Resolving customer record");
        return customerRepository.findByPhone(phone)
                .orElseGet(() -> customerRepository.save(
                        Customer.builder()
                                .name(name)
                                .phone(phone)
                                .isGuest(true)
                                .build()
                ));
    }

    @Override
    public List<CustomerResponse> getAllForAdmin() {
        // Step 1:
        // Load every customer row from the database.
        List<Customer> customers = customerRepository.findAll();

        // Step 2:
        // Load Orders/Total Spent/Last Order for every customer in one
        // grouped query instead of hitting the orders table per row.
        Map<Long, CustomerOrderStatsProjection> statsByCustomerId = orderRepository.findOrderStatsGroupedByCustomer()
                .stream()
                .collect(Collectors.toMap(CustomerOrderStatsProjection::getCustomerId, Function.identity()));

        // Step 3:
        // Convert each customer into a CustomerResponse, filling in its
        // stats if it has placed at least one order (defaults to 0/null
        // otherwise, e.g. a customer row created but never checked out).
        return customers.stream()
                .map(customer -> enrich(customer, statsByCustomerId.get(customer.getId())))
                .toList();
    }

    @Override
    public CustomerResponse getById(Long id) {
        // Step 1:
        // Fetch the Customer entity, or fail clearly if the id doesn't exist.
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        // Step 2:
        // Look up this one customer's order stats — reuses the same grouped
        // query rather than adding a separate single-customer query, since
        // this is an admin detail screen (not a hot path).
        CustomerOrderStatsProjection stats = orderRepository.findOrderStatsGroupedByCustomer().stream()
                .filter(row -> row.getCustomerId().equals(id))
                .findFirst()
                .orElse(null);

        return enrich(customer, stats);
    }

    // Combines the base entity fields (via CustomerMapper) with the
    // computed order stats (nullable — a customer may have zero orders) to
    // produce the final CustomerResponse for the admin "Customers" screen.
    private CustomerResponse enrich(Customer customer, CustomerOrderStatsProjection stats) {
        CustomerResponse base = customerMapper.toResponse(customer);
        long ordersCount = stats != null ? stats.getOrdersCount() : 0L;
        BigDecimal totalSpent = stats != null ? stats.getTotalSpent() : BigDecimal.ZERO;
        LocalDateTime lastOrderAt = stats != null ? stats.getLastOrderAt() : null;

        return new CustomerResponse(
                base.id(),
                base.name(),
                base.phone(),
                base.email(),
                ordersCount,
                totalSpent,
                lastOrderAt,
                base.joinedAt()
        );
    }
}
