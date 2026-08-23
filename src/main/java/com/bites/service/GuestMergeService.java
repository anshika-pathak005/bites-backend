package com.bites.service;

import com.bites.entity.Customer;

// Called once, right after a guest successfully registers or logs in, to
// fold whatever they already had as a guest (cart items, saved addresses —
// both still sitting under their guestId cookie) into their new/existing
// customer account. Without this, a returning guest who registers would see
// an empty cart right after signing up, which is a confusing UX regression
// login should never cause.
public interface GuestMergeService {

    // guestId is the CURRENT browser's guest identity (from the HttpOnly
    // cookie) at the moment of login/register — always present (see
    // GuestIdentityFilter), even if this particular guest never actually
    // used the cart/saved an address.
    void merge(String guestId, Customer customer);
}
