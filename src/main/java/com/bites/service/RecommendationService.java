package com.bites.service;

import com.bites.dto.response.MenuItemResponse;

import java.util.List;

// "You might also like" — a login-only perk (see the plan discussed before
// this module was built): recommends menu items based on what a customer
// ordered last time. Deliberately NOT available to guests — there's no
// reliable "last order" to base it on without a real account, and it's one
// of the differentiators that makes logging in worth it.
public interface RecommendationService {

    // Returns up to a handful of available menu items from the same
    // categories as this customer's most recent order, excluding items
    // already in that order. Empty if the customer has never ordered yet.
    List<MenuItemResponse> recommendationsForCustomer(Long customerId);
}
