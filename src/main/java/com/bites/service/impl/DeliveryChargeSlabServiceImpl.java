package com.bites.service.impl;

import com.bites.dto.request.DeliveryChargeSlabRequest;
import com.bites.dto.response.DeliveryChargeSlabResponse;
import com.bites.entity.DeliveryChargeSlab;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.DeliveryChargeSlabMapper;
import com.bites.repository.DeliveryChargeSlabRepository;
import com.bites.service.DeliveryChargeSlabService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryChargeSlabServiceImpl implements DeliveryChargeSlabService {

    // Used to load and save DeliveryChargeSlab rows.
    private final DeliveryChargeSlabRepository slabRepository;

    // Converts DeliveryChargeSlab entity <-> the request/response DTOs.
    private final DeliveryChargeSlabMapper slabMapper;

    @Override
    public List<DeliveryChargeSlabResponse> getAllForAdmin() {
        // Lowest distance first, regardless of active flag — easiest way
        // for an admin to read the pricing ladder top to bottom.
        return slabRepository.findAll().stream()
                .sorted(Comparator.comparing(DeliveryChargeSlab::getMinDistanceKm))
                .map(slabMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DeliveryChargeSlabResponse create(DeliveryChargeSlabRequest request) {
        DeliveryChargeSlab slab = slabMapper.toEntity(request);
        return slabMapper.toResponse(slabRepository.save(slab));
    }

    @Override
    @Transactional
    public DeliveryChargeSlabResponse update(Long id, DeliveryChargeSlabRequest request) {
        DeliveryChargeSlab slab = findEntity(id);
        slabMapper.updateEntityFromRequest(request, slab);
        return slabMapper.toResponse(slabRepository.save(slab));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Hard delete is safe here — unlike DeliveryArea, nothing (orders
        // included) holds a foreign key to a slab; only its computed
        // charge/distance is ever snapshotted onto an order.
        DeliveryChargeSlab slab = findEntity(id);
        slabRepository.delete(slab);
    }

    // Internal helper: fetches a DeliveryChargeSlab entity by id, or fails clearly if it doesn't exist.
    private DeliveryChargeSlab findEntity(Long id) {
        return slabRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery charge slab not found: " + id));
    }
}
