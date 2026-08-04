package com.bites.service.impl;

import com.bites.dto.request.CustomerAddressRequest;
import com.bites.dto.response.CustomerAddressResponse;
import com.bites.dto.response.CustomerResponse;
import com.bites.entity.Customer;
import com.bites.entity.CustomerAddress;
import com.bites.entity.DeliveryArea;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.CustomerAddressMapper;
import com.bites.mapper.CustomerMapper;
import com.bites.repository.CustomerAddressRepository;
import com.bites.repository.CustomerRepository;
import com.bites.repository.DeliveryAreaRepository;
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

    // Used to load and save saved addresses belonging to a customer.
    private final CustomerAddressRepository customerAddressRepository;

    // Used to confirm a delivery area actually exists (and is active) before
    // it gets attached to a new address.
    private final DeliveryAreaRepository deliveryAreaRepository;

    // Converts Customer entity -> CustomerResponse DTO.
    private final CustomerMapper customerMapper;

    // Converts CustomerAddress entity <-> CustomerAddressResponse/Request DTOs.
    private final CustomerAddressMapper customerAddressMapper;

    @Override
    @Transactional
    public Customer findOrCreateByPhone(String name, String phone) {
        // Step 1:
        // Check if a customer with this phone number has ordered before.
        // Step 2:
        // If yes, just reuse that existing customer row — this is what
        // gives a repeat guest their order history without needing a login.
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
        Customer customer = findEntity(id);

        // Step 2:
        // Convert Entity -> Response DTO.
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerAddressResponse addAddress(Long customerId, CustomerAddressRequest request) {
        // Step 1:
        // Confirm the customer actually exists.
        Customer customer = findEntity(customerId);

        // Step 2:
        // Confirm the delivery area on the request actually exists and is
        // currently active — we should never save an address pointing at a
        // delivery area we no longer serve.
        DeliveryArea deliveryArea = deliveryAreaRepository.findById(request.deliveryAreaId())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid delivery area id: " + request.deliveryAreaId()));

        if (!deliveryArea.isActive()) {
            throw new BadRequestException("This delivery area is not currently serviceable");
        }

        // Step 3:
        // Build the address entity from the request, then attach the
        // customer and delivery area we just confirmed above (the mapper
        // itself is not allowed to do these lookups, so we do it here).
        CustomerAddress address = customerAddressMapper.toEntity(request);
        address.setCustomer(customer);
        address.setDeliveryArea(deliveryArea);

        // Step 4:
        // Save the new address and convert it to a response before returning.
        CustomerAddress saved = customerAddressRepository.save(address);
        return customerAddressMapper.toResponse(saved);
    }

    @Override
    public List<CustomerAddressResponse> getAddresses(Long customerId) {
        // Step 1:
        // Confirm the customer exists before looking up their addresses.
        findEntity(customerId);

        // Step 2:
        // Load every saved address for this customer and convert each one
        // to a response DTO.
        return customerAddressRepository.findByCustomerId(customerId).stream()
                .map(customerAddressMapper::toResponse)
                .toList();
    }

    // Internal helper: fetches a Customer entity by id, or throws a clear
    // "not found" error instead of letting a null slip through silently.
    private Customer findEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }
}
