package com.bites.service;

import com.bites.entity.DeliveryChargeSlab;
import com.bites.exception.BadRequestException;
import com.bites.repository.DeliveryChargeSlabRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/*
 * Shared by DeliveryChargeSlabServiceImpl (whenever a slab is
 * created/updated/deleted) and RestaurantServiceImpl (whenever the max
 * delivery radius itself changes) — both need the same answer to "do the
 * active slabs fully, contiguously cover 0..maxRadiusKm with no gaps or
 * overlaps?", just triggered from different admin screens. Centralizing it
 * here means an admin can't get into an inconsistent state no matter which
 * screen they touch first.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCoverageValidator {

    private final DeliveryChargeSlabRepository slabRepository;

    // Throws BadRequestException with a specific, actionable message if the
    // active slabs don't form one continuous chain from 0km up to
    // maxRadiusKm. Called BEFORE saving whatever change triggered the
    // check, so an inconsistent state is rejected outright, not saved and
    // discovered later when a real customer's checkout fails.
    public void validateCoverage(BigDecimal maxRadiusKm) {
        List<DeliveryChargeSlab> activeSlabs = slabRepository.findByIsActiveTrueOrderByMinDistanceKmAsc();

        if (activeSlabs.isEmpty()) {
            // Not blocked outright — an admin might be deliberately
            // deactivating all slabs mid-reconfiguration — but every
            // ONLINE/COD checkout will fail until at least one is added
            // back, so this is worth a loud warning either way.
            log.warn("No active delivery charge slabs configured — checkout will fail until at least one is added");
            return;
        }

        DeliveryChargeSlab first = activeSlabs.get(0);
        if (first.getMinDistanceKm().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException(
                    "Delivery charge slabs must start from 0km — the lowest active slab currently starts at "
                            + first.getMinDistanceKm() + "km");
        }

        for (int i = 0; i < activeSlabs.size() - 1; i++) {
            DeliveryChargeSlab current = activeSlabs.get(i);
            DeliveryChargeSlab next = activeSlabs.get(i + 1);
            int comparison = current.getMaxDistanceKm().compareTo(next.getMinDistanceKm());

            if (comparison < 0) {
                throw new BadRequestException(
                        "There's a gap in delivery pricing between " + current.getMaxDistanceKm() + "km and "
                                + next.getMinDistanceKm() + "km — add a slab covering that range before saving");
            }
            if (comparison > 0) {
                throw new BadRequestException(
                        "Delivery charge slabs overlap between " + next.getMinDistanceKm() + "km and "
                                + current.getMaxDistanceKm() + "km — adjust one of them so they meet exactly, not overlap");
            }
        }

        DeliveryChargeSlab last = activeSlabs.get(activeSlabs.size() - 1);
        if (last.getMaxDistanceKm().compareTo(maxRadiusKm) < 0) {
            throw new BadRequestException(
                    "Delivery charge slabs only cover up to " + last.getMaxDistanceKm()
                            + "km, but the delivery radius is " + maxRadiusKm
                            + "km — extend the last slab or add another one covering up to " + maxRadiusKm + "km");
        }
    }
}
