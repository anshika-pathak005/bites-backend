package com.bites.mapper;

import com.bites.dto.request.RestaurantSettingsRequest;
import com.bites.dto.response.RestaurantSettingsResponse;
import com.bites.entity.Restaurant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

// Almost every field on Restaurant matches its DTOs directly — the one
// exception is isOpen, which needs an explicit @Mapping (see below).
@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    // Lombok's isOpen() getter on the entity gets JavaBean-stripped to
    // property "open", but RestaurantSettingsResponse's isOpen record
    // component keeps its literal name — without this, MapStruct silently
    // leaves isOpen as false instead of failing loudly.
    @Mapping(source = "open", target = "isOpen")
    RestaurantSettingsResponse toResponse(Restaurant restaurant);

    // Applies only the non-null fields from the request onto the existing Restaurant settings, leaving other fields untouched.
    // Note: this updates an EXISTING instance via its setter (Restaurant has
    // no toEntity()/builder path in this mapper), and MapStruct resolves
    // setter targets using the JavaBean-stripped name ("open"), not the
    // literal field name — hence this differs from the toResponse() mapping above.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "isOpen", target = "open")
    // id/createdAt/updatedAt are DB-managed (see AuditableEntity) — a
    // request should never be able to set them, so they're explicitly
    // ignored here rather than left as an unmapped-target warning.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(RestaurantSettingsRequest request, @MappingTarget Restaurant restaurant);
}
