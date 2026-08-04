package com.bites.mapper;

import com.bites.dto.request.RestaurantSettingsRequest;
import com.bites.dto.response.RestaurantSettingsResponse;
import com.bites.entity.Restaurant;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

// A plain, direct field-to-field mapping — every field on Restaurant has a
// matching field on the DTOs, so MapStruct can generate this entirely on its
// own without any manual @Mapping hints.
@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    RestaurantSettingsResponse toResponse(Restaurant restaurant);

    // Applies only the non-null fields from the request onto the existing Restaurant settings, leaving other fields untouched.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(RestaurantSettingsRequest request, @MappingTarget Restaurant restaurant);
}
