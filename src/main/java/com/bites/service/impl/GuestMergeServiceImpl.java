package com.bites.service.impl;

import com.bites.entity.Cart;
import com.bites.entity.CartItem;
import com.bites.entity.Customer;
import com.bites.entity.CustomerAddress;
import com.bites.repository.CartItemRepository;
import com.bites.repository.CartRepository;
import com.bites.repository.CustomerAddressRepository;
import com.bites.service.GuestMergeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestMergeServiceImpl implements GuestMergeService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerAddressRepository customerAddressRepository;

    @Override
    @Transactional
    public void merge(String guestId, Customer customer) {
        mergeCart(guestId, customer);
        mergeAddresses(guestId, customer);
    }

    // Internal helper: moves every item from the guest's cart into the
    // customer's cart (creating one if they don't have one yet), combining
    // quantities for any item both carts happen to already have, then
    // deletes the now-empty guest cart row.
    private void mergeCart(String guestId, Customer customer) {
        Optional<Cart> guestCartOpt = cartRepository.findByGuestId(guestId);
        if (guestCartOpt.isEmpty()) {
            return;
        }
        Cart guestCart = guestCartOpt.get();
        List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());

        if (guestItems.isEmpty()) {
            cartRepository.delete(guestCart);
            return;
        }

        Cart customerCart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().customerId(customer.getId()).build()));

        for (CartItem guestItem : guestItems) {
            var existingInCustomerCart = cartItemRepository
                    .findByCartIdAndMenuItemId(customerCart.getId(), guestItem.getMenuItem().getId());

            if (existingInCustomerCart.isPresent()) {
                // Already have this dish in the customer cart — just add
                // the quantities together and leave the guest's row where
                // it is; it gets swept up by deleteByCartId below.
                CartItem item = existingInCustomerCart.get();
                item.setQuantity(item.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(item);
            } else {
                // New to the customer cart — just reassign this exact row
                // to it instead of copying, so priceAtAddition etc. carry
                // over untouched.
                guestItem.setCart(customerCart);
                cartItemRepository.save(guestItem);
            }
        }

        // Cleans up any items that were merged-by-quantity above (their
        // rows are still attached to guestCart); anything reassigned via
        // setCart() is already gone from this cart, so this is a no-op for those.
        cartItemRepository.deleteByCartId(guestCart.getId());
        cartRepository.delete(guestCart);
        log.info("Merged guest cart into customer account: customerId={}", customer.getId());
    }

    // Internal helper: reassigns every address the guest had saved onto the
    // customer account. Does not attempt to de-duplicate identical
    // addresses the customer might already have saved from a previous
    // session — acceptable for v1, worth revisiting if it turns out to
    // annoy people.
    private void mergeAddresses(String guestId, Customer customer) {
        List<CustomerAddress> guestAddresses =
                customerAddressRepository.findByGuestIdAndCustomerIdIsNullOrderByIsDefaultDesc(guestId);
        if (guestAddresses.isEmpty()) {
            return;
        }

        boolean customerAlreadyHasDefault =
                !customerAddressRepository.findByCustomerIdAndIsDefaultTrue(customer.getId()).isEmpty();
        boolean defaultAlreadyAssigned = false;

        for (CustomerAddress address : guestAddresses) {
            address.setCustomerId(customer.getId());
            // Never end up with two "default" addresses: if the customer
            // already had one, or we've already kept one from this merge,
            // clear the flag on every other merged address.
            if (customerAlreadyHasDefault || defaultAlreadyAssigned) {
                address.setDefault(false);
            } else if (address.isDefault()) {
                defaultAlreadyAssigned = true;
            }
        }

        customerAddressRepository.saveAll(guestAddresses);
        log.info("Merged {} guest address(es) into customer account: customerId={}",
                guestAddresses.size(), customer.getId());
    }
}
