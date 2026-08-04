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
    // Lombok's isXxx() getter on the entity gets JavaBean-stripped to
    // property "xxx", but a record's isXxx component keeps its literal
    // name — without these, MapStruct silently leaves every one of these
    // flags as false instead of failing loudly.
    @Mapping(source = "veg", target = "isVeg")
    @Mapping(source = "available", target = "isAvailable")
    @Mapping(source = "bestSeller", target = "isBestSeller")
    @Mapping(source = "todaysSpecial", target = "isTodaysSpecial")
    MenuItemResponse toResponse(MenuItem menuItem);

    // category itself is set separately in the service (needs a lookup by categoryId)
    // MenuItem has @Builder, and Lombok's builder methods use the literal
    // field name (isVeg(...), not veg(...)) — these explicit mappings pin
    // that down so it doesn't silently regress if the builder is removed.
    @Mapping(target = "category", ignore = true)
    @Mapping(source = "isVeg", target = "isVeg")
    @Mapping(source = "isAvailable", target = "isAvailable")
    @Mapping(source = "isBestSeller", target = "isBestSeller")
    @Mapping(source = "isTodaysSpecial", target = "isTodaysSpecial")
    MenuItem toEntity(MenuItemRequest request);

    // Applies only the non-null fields from the request onto an existing MenuItem; category is left untouched here too.
    // Note: unlike toEntity() above, this updates an EXISTING instance via
    // its setters (not the Lombok builder), and MapStruct resolves setter
    // targets using the JavaBean-stripped name ("veg"), not the literal
    // field name — hence the different target= value from toEntity().
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    @Mapping(source = "isVeg", target = "veg")
    @Mapping(source = "isAvailable", target = "available")
    @Mapping(source = "isBestSeller", target = "bestSeller")
    @Mapping(source = "isTodaysSpecial", target = "todaysSpecial")
    // id/createdAt/updatedAt are DB-managed (see AuditableEntity) — a
    // request should never be able to set them, so they're explicitly
    // ignored here rather than left as an unmapped-target warning.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(MenuItemRequest request, @MappingTarget MenuItem menuItem);
}
