package com.bites.repository;

import com.bites.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

// Singleton table — service layer always fetches id=1 (or the first row),
// never exposes create/delete for this entity.
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
