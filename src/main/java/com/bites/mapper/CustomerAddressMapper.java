package com.bites.mapper;

import com.bites.dto.request.CustomerAddressRequest;
import com.bites.dto.response.CustomerAddressResponse;
import com.bites.entity.CustomerAddress;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerAddressMapper {

    // deliveryArea.id and deliveryArea.name come from the linked DeliveryArea
    // entity, not directly from CustomerAddress — MapStruct needs to be told
    // explicitly to reach through the relationship to grab them.
    @Mapping(source = "deliveryArea.id", target = "deliveryAreaId")
    @Mapping(source = "deliveryArea.name", target = "deliveryAreaName")
    CustomerAddressResponse toResponse(CustomerAddress customerAddress);

    // deliveryArea itself is left out here because the service must first
    // look up the actual DeliveryArea entity by id (to confirm it exists and
    // is active) before it can be attached — a plain mapper cannot do a
    // database lookup on its own.
    @Mapping(target = "deliveryArea", ignore = true)
    @Mapping(target = "customer", ignore = true)
    CustomerAddress toEntity(CustomerAddressRequest request);
}
