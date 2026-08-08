package com.bites.controller.customer;

import com.bites.dto.response.DeliveryQuoteResponse;
import com.bites.service.DeliveryPricingResult;
import com.bites.service.DeliveryPricingService;
import com.bites.util.ApiResponse;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

// Public — no login needed. Lets the checkout page show the delivery
// charge for a chosen/typed address BEFORE the guest actually places the
// order — same distance + slab logic checkout itself uses (see
// DeliveryPricingService), just exposed as a read-only preview here.
@RestController
@RequestMapping("/api/delivery-pricing")
@RequiredArgsConstructor
@Validated
public class DeliveryPricingController {

    private final DeliveryPricingService deliveryPricingService;

    // e.g. GET /api/delivery-pricing/quote?latitude=26.202684&longitude=82.959007
    // Returns the same AddressOutOfDeliveryRangeException-driven 400 as
    // checkout/address-save if the point is outside the 8km radius, so the
    // checkout page can show the same "too far away" message up front.
    @GetMapping("/quote")
    public ApiResponse<DeliveryQuoteResponse> quote(
            @RequestParam
            @NotNull
            @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
            BigDecimal latitude,

            @RequestParam
            @NotNull
            @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
            BigDecimal longitude
    ) {
        DeliveryPricingResult result = deliveryPricingService.priceDelivery(latitude, longitude);
        return ApiResponse.success(new DeliveryQuoteResponse(result.distanceKm(), result.charge()));
    }
}
