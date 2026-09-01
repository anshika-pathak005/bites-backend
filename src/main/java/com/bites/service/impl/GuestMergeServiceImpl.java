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

import java.util.ArrayList;
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
    //
    // IMPORTANT: this deliberately tracks exactly which guest cart_item ids
    // are redundant (merged-by-quantity duplicates) and deletes ONLY those
    // by id, instead of the previous approach of reassigning some rows to
    // the customer cart and then bulk-deleting "everything still pointing
    // at the guest cart id". That bulk delete ran as its own query right
    // after save() calls that hadn't necessarily been flushed to the
    // database yet, which could catch rows that had just been reassigned
    // to the customer's cart in the same transaction — deleting cart items
    // that should have survived the merge, and leaving the customer's cart
    // in a state where a cart_item's cart_id didn't match what the
    // customer's session now resolves to (surfacing as "this cart item
    // does not belong to the given cart" the next time the customer
    // touched their cart). Deleting by explicit id list has no such
    // ordering dependency.
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

        List<Long> redundantGuestItemIds = new ArrayList<>();

        for (CartItem guestItem : guestItems) {
            var existingInCustomerCart = cartItemRepository
                    .findByCartIdAndMenuItemId(customerCart.getId(), guestItem.getMenuItem().getId());

            if (existingInCustomerCart.isPresent()) {
                // Customer's cart already has this dish — add the
                // quantities together and mark this guest row as
                // redundant so it gets explicitly deleted below.
                CartItem item = existingInCustomerCart.get();
                item.setQuantity(item.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(item);
                redundantGuestItemIds.add(guestItem.getId());
            } else {
                // New to the customer cart — reassign this exact row
                // to it instead of copying, so priceAtAddition etc.
                // carry over untouched. Its cart_id is now the customer
                // cart's, so it is intentionally NOT in the delete list.
                guestItem.setCart(customerCart);
                cartItemRepository.save(guestItem);
            }
        }

        // Flush the reassignments/quantity updates above to the database
        // BEFORE deleting anything, so the targeted delete below can never
        // race against — or accidentally catch — a row that was just
        // reassigned to the customer's cart in this same transaction.
        cartItemRepository.flush();

        if (!redundantGuestItemIds.isEmpty()) {
            cartItemRepository.deleteAllByIdInBatch(redundantGuestItemIds);
        }

        // At this point every remaining guest cart_item row has either
        // been reassigned to the customer's cart or explicitly deleted —
        // the guest cart itself is guaranteed empty, so it's safe to remove.
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
