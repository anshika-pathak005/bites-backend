package com.bites.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// Represents one saved delivery address belonging to EITHER a guest,
// identified purely by guestId (see GuestIdentityFilter — an HttpOnly
// cookie, not a login), OR a logged-in customer, once customer login
// exists. Pricing is derived from latitude/longitude via
// DeliveryPricingService, not any zone reference.
@Entity
@Table(name = "customer_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class CustomerAddress extends AuditableEntity {

    // Which guest (browser) this address was originally saved under —
    // resolved from the HttpOnly cookie, never from anything the client
    // sends. Stays NOT NULL (and keeps its original value) even after this
    // address is merged into a customer account below, purely as a history
    // trail — once customerId is set, ownership checks use customerId only
    // (see AddressServiceImpl.findOwnedAddress), this old guestId can no
    // longer access it.
    @Column(name = "guest_id", nullable = false, length = 64)
    private String guestId;

    // The customer this address belongs to, once they're logged in — set
    // either directly (addAddress while logged in) or via GuestMergeService
    // when a guest with saved addresses logs in/registers. Null means this
    // address is still guest-owned (see guestId above).
    @Column(name = "customer_id")
    private Long customerId;

    // Short name for the address, e.g. "Home", "Work".
    @Column(length = 50)
    private String label;

    @Column(name = "full_address", nullable = false, length = 500)
    private String fullAddress;

    @Column(length = 255)
    private String landmark;

    // Postal/PIN code — optional (older rows saved before this field existed
    // won't have one), shown alongside the full address on delivery slips.
    @Column(length = 10)
    private String pincode;

    // Latitude/longitude of this address, either from the browser's
    // Geolocation API (current-location flow) or from geocoding the typed
    // address (manual-entry flow). Nullable so old addresses saved before
    // this field existed don't break; the frontend must always send both
    // when creating/editing an address going forward (see
    // CustomerAddressRequest validation) — this is what
    // DeliveryPricingService uses to validate the configured delivery radius and, at
    // checkout, to work out the delivery charge.
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    // True if this is the address to use by default at checkout.
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}
