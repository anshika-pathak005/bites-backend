package com.bites.controller.customer;

import com.bites.dto.response.DeliveryAreaResponse;
import com.bites.service.DeliveryAreaService;
import com.bites.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Public endpoint — no login needed. Lets the customer site show which
// areas/localities the restaurant currently delivers to.
@RestController
@RequestMapping("/api/delivery-areas")
@RequiredArgsConstructor
public class DeliveryAreaController {

    // Handles fetching delivery area data — this controller only wires the
    // HTTP request/response around it.
    private final DeliveryAreaService deliveryAreaService;

    // Returns all delivery areas that are currently active, so the customer
    // can pick their area at checkout or see if delivery is available nearby.
    @GetMapping
    public ApiResponse<List<DeliveryAreaResponse>> getActiveAreas() {
        return ApiResponse.success(deliveryAreaService.getActiveAreas());
    }
}
