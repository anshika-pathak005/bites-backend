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

// Represents one shopping cart, belonging to a guest identified purely by
// their guestId (see GuestIdentityFilter — a server-issued HttpOnly cookie,
// not a client-supplied session id). No login involved. Has many CartItems.
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
    @Column(name = "guest_id", length = 100)
    private String guestId;
}
