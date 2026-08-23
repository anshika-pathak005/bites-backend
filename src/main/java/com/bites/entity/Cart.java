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

// Represents one shopping cart, belonging to EITHER a guest identified
// purely by their guestId (see GuestIdentityFilter — a server-issued
// HttpOnly cookie, not a client-supplied session id) OR a logged-in
// customer, once customer login exists — never both at once. Has many
// CartItems.
@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Cart extends AuditableEntity {

    // The guestId this cart belongs to — resolved from the HttpOnly cookie
    // on every request, never sent/trusted from the client body/params.
    // Null once this cart has been merged into a logged-in customer's own
    // cart (see GuestMergeService) — customerId is set instead.
    @Column(name = "guest_id", length = 100)
    private String guestId;

    // The customer this cart belongs to, once they're logged in. This
    // column was actually reserved all the way back when the cart table was
    // first created — carts started out customer_id-based before the guest
    // cookie identity was introduced — so no migration was needed to add
    // it, just this entity field switching it back on.
    @Column(name = "customer_id")
    private Long customerId;
}
