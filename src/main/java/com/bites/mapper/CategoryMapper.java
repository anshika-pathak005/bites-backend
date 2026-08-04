package com.bites.mapper;

import com.bites.dto.request.CategoryRequest;
import com.bites.dto.response.CategoryResponse;
import com.bites.entity.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

// Converts between Category entity and its request/response DTOs so the service layer doesn't do manual field copying.
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // Lombok's isActive() getter on the entity gets JavaBean-stripped to
    // property "active", but CategoryResponse's isActive record component
    // keeps its literal name — without this, MapStruct silently leaves
    // isActive as false instead of failing loudly.
    @Mapping(source = "active", target = "isActive")
    CategoryResponse toResponse(Category category);

    // Category has @Builder, and Lombok's builder method uses the literal
    // field name (isActive(...), not active(...)) — pinned explicitly here
    // so it doesn't silently regress if the builder is removed.
    @Mapping(source = "isActive", target = "isActive")
    Category toEntity(CategoryRequest request);

    // Applies only the non-null fields from the request onto an existing Category, leaving other fields untouched.
    // Note: unlike toEntity() above, this updates an EXISTING instance via
    // its setter (not the Lombok builder), and MapStruct resolves setter
    // targets using the JavaBean-stripped name ("active"), not the literal
    // field name — hence the different target= value from toEntity().
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "isActive", target = "active")
    // id/createdAt/updatedAt are DB-managed (see AuditableEntity) — a
    // request should never be able to set them, so they're explicitly
    // ignored here rather than left as an unmapped-target warning.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
