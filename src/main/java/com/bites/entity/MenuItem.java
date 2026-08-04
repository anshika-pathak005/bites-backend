package com.bites.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// Represents one dish/item on the menu that customers can order. Belongs to
// one Category.
@Entity
@Table(name = "menu_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MenuItem extends AuditableEntity {

    // The category this item is listed under.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_item_category"))
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    // Current selling price of the item.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // URL of the item's photo.
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // True if the dish is vegetarian.
    @Column(name = "is_veg", nullable = false)
    private boolean isVeg;

    // False hides the item from the menu (e.g. sold out) without deleting it.
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    // True highlights the item as a best seller on the menu.
    @Column(name = "is_best_seller", nullable = false)
    private boolean isBestSeller;

    // True highlights the item as today's special on the menu.
    @Column(name = "is_todays_special", nullable = false)
    private boolean isTodaysSpecial;
}
