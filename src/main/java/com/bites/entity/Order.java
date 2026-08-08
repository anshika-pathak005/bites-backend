package com.bites.entity;

import com.bites.entity.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Table is "orders" (ORDER is a reserved word in MySQL); the class stays
 * "Order" for readability in Java code.
 *
 * customer_name / customer_phone / delivery_address / landmark / pincode /
 * delivery_charge / distance_km are all SNAPSHOTS taken at checkout time —
 * never re-derived from the live Customer/CustomerAddress rows, so order
 * history (and past pricing) stays accurate even if the customer edits or
 * deletes that address later, or an admin changes the delivery pricing slabs.
 */
@Entity
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(name = "uq_orders_order_number", columnNames = "order_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Human-readable order number shown to the customer (e.g. on receipts),
    // separate from the internal id.
    @Column(name = "order_number", nullable = false, length = 30)
    private String orderNumber;

    // The customer (admin-reporting record, keyed by phone) who placed this
    // order — see CustomerService. NOT used for ownership/access control.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_customer"))
    private Customer customer;

    // Which guest (browser) placed this order — powers "GET /api/orders"
    // (list everything for this browser). Tracking/cancelling a SINGLE
    // order by number does not check this field; order number alone is the
    // bearer credential there (see OrderController) — guestId is only used
    // for the "show me all my orders automatically" list view.
    @Column(name = "guest_id", length = 64)
    private String guestId;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    @Column(length = 255)
    private String landmark;

    @Column(length = 10)
    private String pincode;

    // Straight-line distance (km) from the restaurant to the delivery
    // address at the moment this order was placed — what deliveryCharge
    // below was actually priced from (see DeliveryPricingService). Kept as
    // its own column mainly for admin/support visibility and pricing-dispute
    // audits, rather than re-deriving it from lat/lng every time.
    @Column(name = "distance_km", precision = 6, scale = 3)
    private BigDecimal distanceKm;

    // Delivery fee for this order (snapshot, see class comment above) —
    // resolved from the configured DeliveryChargeSlab matching distanceKm
    // at checkout time, not from a DeliveryArea zone.
    @Column(name = "delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    // Sum of the order items' subtotals, before delivery charge.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Final amount charged to the customer: subtotal + deliveryCharge.
    @Column(name = "grand_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal grandTotal;

    // Current stage of the order in its lifecycle (see OrderStatus).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // The dishes included in this order.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // When the order was placed; set automatically and never changes.
    @CreationTimestamp
    @Column(name = "placed_at", nullable = false, updatable = false)
    private LocalDateTime placedAt;

    // When the order was last updated (e.g. status change); set automatically.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}
