package com.bites.mapper;

import com.bites.dto.request.DeliveryChargeSlabRequest;
import com.bites.dto.response.DeliveryChargeSlabResponse;
import com.bites.entity.DeliveryChargeSlab;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

// Converts between DeliveryChargeSlab entity and its request/response DTOs.
@Mapper(componentModel = "spring")
public interface DeliveryChargeSlabMapper {

    // Lombok's isActive() getter on the entity gets JavaBean-stripped to
    // property "active", but DeliveryChargeSlabResponse's isActive record
    // component keeps its literal name — without this, MapStruct silently
    // leaves isActive as false instead of failing loudly.
    @Mapping(source = "active", target = "isActive")
    DeliveryChargeSlabResponse toResponse(DeliveryChargeSlab slab);

    // DeliveryChargeSlab has @Builder, and Lombok's builder method uses the
    // literal field name (isActive(...), not active(...)) — pinned
    // explicitly here so it doesn't silently regress if the builder is removed.
    @Mapping(source = "isActive", target = "isActive")
    DeliveryChargeSlab toEntity(DeliveryChargeSlabRequest request);

    // Applies only the non-null fields from the request onto an existing
    // slab, leaving other fields untouched.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "isActive", target = "active")
    // id/createdAt/updatedAt are DB-managed (see AuditableEntity) — a
    // request should never be able to set them, so they're explicitly
    // ignored here rather than left as an unmapped-target warning.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(DeliveryChargeSlabRequest request, @MappingTarget DeliveryChargeSlab slab);
}
