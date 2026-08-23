package com.bites.repository;

import com.bites.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data access for customer accounts.
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Used during login/checkout to find the customer account matching the entered phone number.
    Optional<Customer> findByPhone(String phone);
    // Used when registering a new customer to check the phone number isn't already taken.
    boolean existsByPhone(String phone);
    // Used to log a customer in by email, and by CustomerContext to resolve
    // the logged-in customer from the JWT's subject (email) on every request.
    Optional<Customer> findByEmail(String email);
    // Used when registering a new customer to check the email isn't already taken.
    boolean existsByEmail(String email);
}
