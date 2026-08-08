package com.bites.service.impl;

import com.bites.dto.response.CustomerResponse;
import com.bites.entity.Customer;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.CustomerMapper;
import com.bites.repository.CustomerRepository;
import com.bites.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    // Used to look up a customer by phone number, and to save a brand new
    // customer row the first time a given phone number checks out.
    private final CustomerRepository customerRepository;

    // Converts Customer entity -> CustomerResponse DTO.
    private final CustomerMapper customerMapper;

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
        // Step 2:
        // Convert each one into a CustomerResponse before sending it out.
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse getById(Long id) {
        // Step 1:
        // Fetch the Customer entity, or fail clearly if the id doesn't exist.
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        // Step 2:
        // Convert Entity -> Response DTO.
        return customerMapper.toResponse(customer);
    }
}
