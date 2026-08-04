package com.bites.repository;

import com.bites.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Data access for a customer's saved delivery addresses.
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    // Used to list all saved addresses for a customer (e.g. address book screen).
    List<CustomerAddress> findByCustomerId(Long customerId);
    // Used to find a customer's default delivery address, e.g. to pre-fill checkout.
    List<CustomerAddress> findByCustomerIdAndIsDefaultTrue(Long customerId);
}
