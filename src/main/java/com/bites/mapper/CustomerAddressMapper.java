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
    // Lombok's isDefault() getter on the entity gets JavaBean-stripped to
    // property "default", but CustomerAddressResponse's isDefault record
    // component keeps its literal name — without this, MapStruct silently
    // leaves isDefault as false instead of failing loudly.
    @Mapping(source = "default", target = "isDefault")
    CustomerAddressResponse toResponse(CustomerAddress customerAddress);

    // deliveryArea itself is left out here because the service must first
    // look up the actual DeliveryArea entity by id (to confirm it exists and
    // is active) before it can be attached — a plain mapper cannot do a
    // database lookup on its own.
    @Mapping(target = "deliveryArea", ignore = true)
    @Mapping(target = "customer", ignore = true)
    // CustomerAddress has @Builder, and Lombok's builder method uses the
    // literal field name (isDefault(...), not default(...)) — pinned
    // explicitly here so it doesn't silently regress if the builder is removed.
    @Mapping(source = "isDefault", target = "isDefault")
    CustomerAddress toEntity(CustomerAddressRequest request);
}
