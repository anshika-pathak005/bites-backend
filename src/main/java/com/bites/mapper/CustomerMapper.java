package com.bites.mapper;

import com.bites.dto.response.CustomerResponse;
import com.bites.entity.Customer;
import org.mapstruct.Mapper;

// Simple, direct field-to-field copy from the Customer entity to the
// CustomerResponse DTO — no calculations needed here, so plain MapStruct
// mapping is enough.
@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponse toResponse(Customer customer);
}
