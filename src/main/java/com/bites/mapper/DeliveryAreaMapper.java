package com.bites.mapper;

import com.bites.dto.request.DeliveryAreaRequest;
import com.bites.dto.response.DeliveryAreaResponse;
import com.bites.entity.DeliveryArea;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

// Converts between DeliveryArea entity and its request/response DTOs so the service layer doesn't do manual field copying.
@Mapper(componentModel = "spring")
public interface DeliveryAreaMapper {

    DeliveryAreaResponse toResponse(DeliveryArea deliveryArea);

    DeliveryArea toEntity(DeliveryAreaRequest request);

    // Applies only the non-null fields from the request onto an existing DeliveryArea, leaving other fields untouched.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(DeliveryAreaRequest request, @MappingTarget DeliveryArea deliveryArea);
}
