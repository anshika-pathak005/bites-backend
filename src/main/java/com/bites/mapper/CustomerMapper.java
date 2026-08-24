package com.bites.mapper;

import com.bites.dto.response.CustomerResponse;
import com.bites.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Maps the plain Customer fields across; ordersCount/totalSpent/lastOrderAt
// aren't on the entity (they're computed from the orders table), so they're
// left at MapStruct's defaults here and filled in afterwards by
// CustomerServiceImpl using OrderRepository's aggregate query.
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "ordersCount", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    @Mapping(target = "lastOrderAt", ignore = true)
    @Mapping(target = "joinedAt", source = "createdAt")
    CustomerResponse toResponse(Customer customer);
}
