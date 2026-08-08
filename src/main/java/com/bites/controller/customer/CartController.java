package com.bites.controller.customer;

import com.bites.dto.request.AddCartItemRequest;
import com.bites.dto.request.UpdateCartItemRequest;
import com.bites.dto.response.CartResponse;
import com.bites.security.GuestContext;
import com.bites.service.CartService;
import com.bites.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Public endpoints — no login needed. Every guest is identified purely by
// the HttpOnly guestId cookie GuestIdentityFilter resolves on every
// request — never a client-supplied id, and never in the URL/body, so a
// guest can never (even accidentally) address another guest's cart.
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    // Delegates all the actual cart logic — this controller only wires the
    // HTTP request/response around it.
    private final CartService cartService;

    // Resolved once per request by GuestIdentityFilter — see GuestContext.
    private final GuestContext guestContext;

    // Fetches the current state of this browser's cart. Called when the cart
    // page loads, or when the cart icon badge needs an up-to-date item count.
    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        return ApiResponse.success(cartService.getOrCreateCart(guestContext.getGuestId()));
    }

    // Adds a menu item to the cart (or increases its quantity if it's
    // already there). Called when the guest clicks "Add to Cart".
    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItem(guestContext.getGuestId(), request.menuItemId(), request.quantity());
        return ApiResponse.success(cart, "Item added to cart");
    }

    // Changes the quantity of one existing cart item (the +/- stepper next
    // to a cart line). Setting quantity to 0 removes the item.
    @PatchMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> updateItemQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse cart = cartService.updateItemQuantity(guestContext.getGuestId(), cartItemId, request.quantity());
        return ApiResponse.success(cart, "Cart updated");
    }

    // Removes one item from the cart completely (the trash/remove icon).
    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> removeItem(@PathVariable Long cartItemId) {
        CartResponse cart = cartService.removeItem(guestContext.getGuestId(), cartItemId);
        return ApiResponse.success(cart, "Item removed from cart");
    }
}
