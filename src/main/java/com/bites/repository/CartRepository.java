package com.bites.repository;

import com.bites.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data access for shopping carts.
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Used to find a guest's cart by their browser/session id, since guests aren't logged in.
    Optional<Cart> findBySessionId(String sessionId);
    // Used to find a logged-in customer's cart by their customer id.
    Optional<Cart> findByCustomerId(Long customerId);
}
