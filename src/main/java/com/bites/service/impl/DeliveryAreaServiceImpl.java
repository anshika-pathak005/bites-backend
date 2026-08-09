package com.bites.service.impl;

import com.bites.dto.request.DeliveryAreaRequest;
import com.bites.dto.response.DeliveryAreaResponse;
import com.bites.entity.DeliveryArea;
import com.bites.exception.ResourceNotFoundException;
import com.bites.mapper.DeliveryAreaMapper;
import com.bites.repository.DeliveryAreaRepository;
import com.bites.service.DeliveryAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Handles the areas the restaurant delivers to — each one has its own
// delivery charge. Covers listing them for customers, listing/managing them
// for admins, and soft-deleting ones no longer served.
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryAreaServiceImpl implements DeliveryAreaService {

    // Used to load and save DeliveryArea rows.
    private final DeliveryAreaRepository deliveryAreaRepository;

    // Converts DeliveryArea entity <-> the request/response DTOs.
    private final DeliveryAreaMapper deliveryAreaMapper;

    // Returns only the delivery areas currently serviceable — this is what
    // customers are allowed to pick from at checkout.
    @Override
    public List<DeliveryAreaResponse> getActiveAreas() {
        return deliveryAreaRepository.findByIsActiveTrue().stream()
                .map(deliveryAreaMapper::toResponse)
                .toList();
    }

    // Returns every delivery area, active or not, for the admin management screen.
    @Override
    public List<DeliveryAreaResponse> getAllForAdmin() {
        return deliveryAreaRepository.findAll().stream()
                .map(deliveryAreaMapper::toResponse)
                .toList();
    }

    // Fetches a single delivery area by id.
    @Override
    public DeliveryAreaResponse getById(Long id) {
        return deliveryAreaMapper.toResponse(findEntity(id));
    }

    // Creates a brand new delivery area from the admin's request.
    @Override
    @Transactional
    public DeliveryAreaResponse create(DeliveryAreaRequest request) {
        DeliveryArea deliveryArea = deliveryAreaMapper.toEntity(request);
        DeliveryArea saved = deliveryAreaRepository.save(deliveryArea);
        log.info("Delivery area created: id={}, name={}, charge={}", saved.getId(), saved.getName(), saved.getDeliveryCharge());
        return deliveryAreaMapper.toResponse(saved);
    }

    // Updates an existing delivery area's fields with whatever the admin submitted.
    @Override
    @Transactional
    public DeliveryAreaResponse update(Long id, DeliveryAreaRequest request) {
        DeliveryArea deliveryArea = findEntity(id);
        deliveryAreaMapper.updateEntityFromRequest(request, deliveryArea);
        DeliveryArea saved = deliveryAreaRepository.save(deliveryArea);
        log.info("Delivery area updated: id={}, name={}", id, saved.getName());
        return deliveryAreaMapper.toResponse(saved);
    }

    // Deactivates a delivery area instead of actually deleting it.
    @Override
    @Transactional
    public void delete(Long id) {
        DeliveryArea deliveryArea = findEntity(id);
        // Soft delete: existing customer addresses and past orders reference this area.
        deliveryArea.setActive(false);
        deliveryAreaRepository.save(deliveryArea);
        log.info("Delivery area deactivated: id={}", id);
    }

    // Internal helper: fetches a DeliveryArea entity by id, or fails clearly if it doesn't exist.
    private DeliveryArea findEntity(Long id) {
        return deliveryAreaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery area not found: " + id));
    }
}
