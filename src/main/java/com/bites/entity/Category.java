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

// Represents a menu category (e.g. "Starters", "Desserts") used to group
// MenuItems. Has many MenuItems.
@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Category extends AuditableEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // URL of the category's display image.
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Controls the order categories are shown in on the menu (lower shows first).
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    // False hides the category from customers without deleting it.
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
