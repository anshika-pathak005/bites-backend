package com.bites.service.impl;

import com.bites.entity.DeliveryChargeSlab;
import com.bites.entity.Restaurant;
import com.bites.exception.AddressOutOfDeliveryRangeException;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.repository.DeliveryChargeSlabRepository;
import com.bites.repository.RestaurantRepository;
import com.bites.service.DeliveryPricingResult;
import com.bites.service.DeliveryPricingService;
import com.bites.util.HaversineUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryPricingServiceImpl implements DeliveryPricingService {

    // The restaurant's maximum delivery radius, in kilometers. Any address
    // further than this from the restaurant's coordinates is rejected —
    // matches the highest configured slab's max, but enforced independently
    // here so an admin fat-fingering the slabs can't silently widen the
    // actual delivery radius.
    private static final double MAX_DELIVERY_RADIUS_KM = 8.0;

    // Used to read the restaurant's own coordinates — the fixed point every
    // address's delivery distance is measured against.
    private final RestaurantRepository restaurantRepository;

    // Used to look up the configured distance -> charge slabs.
    private final DeliveryChargeSlabRepository slabRepository;

    @Override
    public double validateWithinDeliveryRadius(BigDecimal latitude, BigDecimal longitude) {
        double distanceKm = distanceFromRestaurant(latitude, longitude);
        rejectIfOutOfRange(distanceKm);
        return distanceKm;
    }

    @Override
    public DeliveryPricingResult priceDelivery(BigDecimal latitude, BigDecimal longitude) {
        // Step 1:
        // Work out how far away this point is, and reject it outright if
        // it's outside the delivery radius — never price an order we can't
        // actually deliver.
        double distanceKm = distanceFromRestaurant(latitude, longitude);
        rejectIfOutOfRange(distanceKm);

        // Step 2:
        // Find the configured slab this distance falls into and use its
        // flat fee as the delivery charge.
        BigDecimal charge = resolveCharge(distanceKm);
        log.debug("Delivery priced: distanceKm={}, charge={}", distanceKm, charge);

        return new DeliveryPricingResult(distanceKm, charge);
    }

    // Internal helper: Haversine distance between the restaurant's fixed
    // coordinates and the given point. Fails loudly (not silently) if the
    // restaurant's coordinates haven't been configured yet — unlike the
    // old zone-based system, distance is now the ONLY thing pricing is
    // based on, so we can't just skip the check here.
    private double distanceFromRestaurant(BigDecimal latitude, BigDecimal longitude) {
        Restaurant restaurant = restaurantRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant settings row is missing — check the V1__init.sql seed data"));

        if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            log.warn("Restaurant location coordinates not configured in settings");
            throw new BadRequestException(
                    "Restaurant location is not configured yet — an admin must set it under Restaurant Settings before addresses can be validated");
        }

        return HaversineUtil.distanceInKm(restaurant.getLatitude(), restaurant.getLongitude(), latitude, longitude);
    }

    // Internal helper: the actual 8km cutoff check, with a message the
    // frontend can show directly to the guest.
    private void rejectIfOutOfRange(double distanceKm) {
        if (distanceKm > MAX_DELIVERY_RADIUS_KM) {
            log.warn("Delivery address rejected: distance {}km exceeds max delivery radius {}km", distanceKm, MAX_DELIVERY_RADIUS_KM);
            throw new AddressOutOfDeliveryRangeException(String.format(
                    "This address is %.1fkm away, which is outside our %.0fkm delivery radius",
                    distanceKm, MAX_DELIVERY_RADIUS_KM));
        }
    }

    // Internal helper: finds the first active slab whose [min, max] range
    // contains this distance. Slabs are fetched ordered by minDistanceKm
    // ascending, so at an exact boundary (e.g. precisely 2.00km) the lower
    // slab wins deterministically.
    private BigDecimal resolveCharge(double distanceKm) {
        List<DeliveryChargeSlab> slabs = slabRepository.findByIsActiveTrueOrderByMinDistanceKmAsc();

        for (DeliveryChargeSlab slab : slabs) {
            if (distanceKm >= slab.getMinDistanceKm().doubleValue()
                    && distanceKm <= slab.getMaxDistanceKm().doubleValue()) {
                return slab.getCharge();
            }
        }

        // Every distance up to MAX_DELIVERY_RADIUS_KM should be covered by
        // some active slab — reaching here means the admin's slab
        // configuration has a gap, which is a setup problem, not something
        // the guest did wrong.
        log.warn("No delivery pricing configured for distance {}km", distanceKm);
        throw new BadRequestException(
                "No delivery pricing is configured for a " + distanceKm + "km delivery — contact support");
    }
}
