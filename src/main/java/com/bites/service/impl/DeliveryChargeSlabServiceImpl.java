package com.bites.service.impl;

import com.bites.dto.request.DeliveryChargeSlabRequest;
import com.bites.dto.response.DeliveryChargeSlabResponse;
import com.bites.dto.response.DeliveryChargeSlabsResponse;
import com.bites.entity.DeliveryChargeSlab;
import com.bites.entity.Restaurant;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.DeliveryChargeSlabMapper;
import com.bites.repository.DeliveryChargeSlabRepository;
import com.bites.repository.RestaurantRepository;
import com.bites.service.DeliveryChargeSlabService;
import com.bites.service.DeliveryCoverageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryChargeSlabServiceImpl implements DeliveryChargeSlabService {

    // Used to load and save DeliveryChargeSlab rows.
    private final DeliveryChargeSlabRepository slabRepository;

    // Converts DeliveryChargeSlab entity <-> the request/response DTOs.
    private final DeliveryChargeSlabMapper slabMapper;

    // Used to read the restaurant's current max delivery radius, shown
    // alongside the slab list on the admin "Delivery Pricing" screen, and
    // to check individual slabs / full coverage against it.
    private final RestaurantRepository restaurantRepository;

    // Checks that the active slabs, taken together, still contiguously
    // cover 0..maxDeliveryRadiusKm after this change — shared with
    // RestaurantServiceImpl so both "sides" of this can't drift apart.
    private final DeliveryCoverageValidator coverageValidator;

    @Override
    public DeliveryChargeSlabsResponse getAllForAdmin() {
        // Lowest distance first, regardless of active flag — easiest way
        // for an admin to read the pricing ladder top to bottom.
        List<DeliveryChargeSlabResponse> slabs = slabRepository.findAll().stream()
                .sorted(Comparator.comparing(DeliveryChargeSlab::getMinDistanceKm))
                .map(slabMapper::toResponse)
                .toList();

        BigDecimal maxDeliveryRadiusKm = getMaxDeliveryRadiusKm();

        return new DeliveryChargeSlabsResponse(slabs, maxDeliveryRadiusKm);
    }

    @Override
    @Transactional
    public DeliveryChargeSlabResponse create(DeliveryChargeSlabRequest request) {
        BigDecimal maxRadiusKm = getMaxDeliveryRadiusKm();
        validateWithinMaxRadius(request, maxRadiusKm);

        DeliveryChargeSlab slab = slabMapper.toEntity(request);
        DeliveryChargeSlab saved = slabRepository.save(slab);

        // Re-check the WHOLE chain, not just this one slab — adding a slab
        // that's individually valid can still leave a gap next to it (e.g.
        // adding 10-15km while an existing slab only went up to 8km).
        coverageValidator.validateCoverage(maxRadiusKm);

        log.info("Delivery slab created: id={}, range={}-{}km, charge={}", saved.getId(), saved.getMinDistanceKm(), saved.getMaxDistanceKm(), saved.getCharge());
        return slabMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryChargeSlabResponse update(Long id, DeliveryChargeSlabRequest request) {
        BigDecimal maxRadiusKm = getMaxDeliveryRadiusKm();
        validateWithinMaxRadius(request, maxRadiusKm);

        DeliveryChargeSlab slab = findEntity(id);
        slabMapper.updateEntityFromRequest(request, slab);
        DeliveryChargeSlab saved = slabRepository.save(slab);

        coverageValidator.validateCoverage(maxRadiusKm);

        log.info("Delivery slab updated: id={}, range={}-{}km, charge={}", id, saved.getMinDistanceKm(), saved.getMaxDistanceKm(), saved.getCharge());
        return slabMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Hard delete is safe here — unlike DeliveryArea, nothing (orders
        // included) holds a foreign key to a slab; only its computed
        // charge/distance is ever snapshotted onto an order.
        DeliveryChargeSlab slab = findEntity(id);
        slabRepository.delete(slab);

        // Deleting a middle/last slab can reopen a gap that used to be
        // covered — same check as create/update, just triggered by removal
        // instead of addition.
        coverageValidator.validateCoverage(getMaxDeliveryRadiusKm());

        log.info("Delivery slab deleted: id={}", id);
    }

    // Internal helper: fetches a DeliveryChargeSlab entity by id, or fails clearly if it doesn't exist.
    private DeliveryChargeSlab findEntity(Long id) {
        return slabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery charge slab not found: " + id));
    }

    // Internal helper: rejects a single slab outright if its range doesn't
    // make sense on its own — before even considering how it fits with the
    // other slabs (see coverageValidator for that broader check).
    private void validateWithinMaxRadius(DeliveryChargeSlabRequest request, BigDecimal maxRadiusKm) {
        if (request.minDistanceKm().compareTo(request.maxDistanceKm()) >= 0) {
            throw new BadRequestException("minDistanceKm must be less than maxDistanceKm");
        }
        if (request.maxDistanceKm().compareTo(maxRadiusKm) > 0) {
            throw new BadRequestException(
                    "This slab's max distance (" + request.maxDistanceKm()
                            + "km) exceeds the restaurant's configured delivery radius of " + maxRadiusKm
                            + "km. Either lower this slab's max distance to " + maxRadiusKm
                            + "km or less, or increase the delivery radius under Restaurant Settings first.");
        }
    }

    // Internal helper: fetches the restaurant's current max delivery radius.
    private BigDecimal getMaxDeliveryRadiusKm() {
        return restaurantRepository.findAll().stream()
                .findFirst()
                .map(Restaurant::getMaxDeliveryRadiusKm)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant settings row is missing — check the V1__init.sql seed data"));
    }
}
