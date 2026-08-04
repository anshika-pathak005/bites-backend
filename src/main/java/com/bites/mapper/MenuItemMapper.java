package com.bites.mapper;

import com.bites.dto.request.MenuItemRequest;
import com.bites.dto.response.MenuItemResponse;
import com.bites.entity.MenuItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

// Converts between MenuItem entity and its request/response DTOs, including pulling the linked category's id/name onto the response.
@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    MenuItemResponse toResponse(MenuItem menuItem);

    // category itself is set separately in the service (needs a lookup by categoryId)
    @Mapping(target = "category", ignore = true)
    MenuItem toEntity(MenuItemRequest request);

    // Applies only the non-null fields from the request onto an existing MenuItem; category is left untouched here too.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(MenuItemRequest request, @MappingTarget MenuItem menuItem);
}
