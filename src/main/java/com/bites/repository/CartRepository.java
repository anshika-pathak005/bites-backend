package com.bites.repository;

import com.bites.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data access for shopping carts.
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Used to find this browser's cart by its guestId (from the HttpOnly cookie).
    Optional<Cart> findByGuestId(String guestId);
}
