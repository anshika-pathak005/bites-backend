package com.bites.controller.customer;

import com.bites.dto.request.AddCartItemRequest;
import com.bites.dto.request.UpdateCartItemRequest;
import com.bites.dto.response.CartResponse;
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

// Public endpoints — no login needed. Every guest is identified only by the
// sessionId their browser generated, never by a real account.
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    // Delegates all the actual cart logic — this controller only wires the
    // HTTP request/response around it.
    private final CartService cartService;

    // Fetches the current state of a guest's cart. Called when the cart page
    // loads, or when the cart icon badge needs an up-to-date item count.
    @GetMapping("/{sessionId}")
    public ApiResponse<CartResponse> getCart(@PathVariable String sessionId) {
        return ApiResponse.success(cartService.getOrCreateCart(sessionId));
    }

    // Adds a menu item to the cart (or increases its quantity if it's
    // already there). Called when the guest clicks "Add to Cart".
    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItem(request.sessionId(), request.menuItemId(), request.quantity());
        return ApiResponse.success(cart, "Item added to cart");
    }

    // Changes the quantity of one existing cart item (the +/- stepper next
    // to a cart line). Setting quantity to 0 removes the item.
    @PatchMapping("/{sessionId}/items/{cartItemId}")
    public ApiResponse<CartResponse> updateItemQuantity(
            @PathVariable String sessionId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse cart = cartService.updateItemQuantity(sessionId, cartItemId, request.quantity());
        return ApiResponse.success(cart, "Cart updated");
    }

    // Removes one item from the cart completely (the trash/remove icon).
    @DeleteMapping("/{sessionId}/items/{cartItemId}")
    public ApiResponse<CartResponse> removeItem(
            @PathVariable String sessionId,
            @PathVariable Long cartItemId
    ) {
        CartResponse cart = cartService.removeItem(sessionId, cartItemId);
        return ApiResponse.success(cart, "Item removed from cart");
    }
}
