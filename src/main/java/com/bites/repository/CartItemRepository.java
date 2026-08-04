package com.bites.repository;

import com.bites.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Data access for items sitting inside a customer's cart.
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Used to list every item currently in a given cart (e.g. viewing the cart page).
    List<CartItem> findByCartId(Long cartId);
    // Used when adding to cart, to check if this menu item is already in the cart so
    // its quantity can be updated instead of adding a duplicate row.
    Optional<CartItem> findByCartIdAndMenuItemId(Long cartId, Long menuItemId);
    // Used to clear out all items from a cart (e.g. after checkout or when emptying the cart).
    void deleteByCartId(Long cartId);
}
