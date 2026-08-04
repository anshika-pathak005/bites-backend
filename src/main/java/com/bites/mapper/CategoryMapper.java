package com.bites.mapper;

import com.bites.dto.request.CategoryRequest;
import com.bites.dto.response.CategoryResponse;
import com.bites.entity.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

// Converts between Category entity and its request/response DTOs so the service layer doesn't do manual field copying.
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    Category toEntity(CategoryRequest request);

    // Applies only the non-null fields from the request onto an existing Category, leaving other fields untouched.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
