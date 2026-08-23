package com.bites.service.impl;

import com.bites.dto.response.MenuItemResponse;
import com.bites.entity.Order;
import com.bites.mapper.MenuItemMapper;
import com.bites.repository.MenuItemRepository;
import com.bites.repository.OrderRepository;
import com.bites.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper menuItemMapper;

    @Override
    public List<MenuItemResponse> recommendationsForCustomer(Long customerId) {
        return orderRepository.findFirstByCustomer_IdOrderByPlacedAtDesc(customerId)
                .map(this::recommendationsBasedOn)
                .orElseGet(List::of);
    }

    // Internal helper: pulls the category ids and item ids out of the given
    // (most recent) order, then asks the repository for other available
    // items from those same categories, excluding what's already been ordered.
    private List<MenuItemResponse> recommendationsBasedOn(Order lastOrder) {
        List<Long> categoryIds = lastOrder.getOrderItems().stream()
                .map(item -> item.getMenuItem().getCategory().getId())
                .distinct()
                .toList();

        List<Long> alreadyOrderedItemIds = lastOrder.getOrderItems().stream()
                .map(item -> item.getMenuItem().getId())
                .toList();

        if (categoryIds.isEmpty()) {
            return List.of();
        }

        return menuItemRepository
                .findTop6ByCategoryIdInAndIsAvailableTrueAndIdNotInOrderByIsBestSellerDescIdDesc(
                        categoryIds, alreadyOrderedItemIds)
                .stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }
}
