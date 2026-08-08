package com.bites.mapper;

import com.bites.dto.request.CustomerAddressRequest;
import com.bites.dto.response.CustomerAddressResponse;
import com.bites.entity.CustomerAddress;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerAddressMapper {

    // Lombok's isDefault() getter on the entity gets JavaBean-stripped to
    // property "default", but CustomerAddressResponse's isDefault record
    // component keeps its literal name — without this, MapStruct silently
    // leaves isDefault as false instead of failing loudly.
    @Mapping(source = "default", target = "isDefault")
    CustomerAddressResponse toResponse(CustomerAddress customerAddress);

    // guestId itself is left out here because the controller resolves it
    // from the HttpOnly cookie and the service attaches it — a plain
    // mapper has no business deciding whose address this is.
    @Mapping(target = "guestId", ignore = true)
    // CustomerAddress has @Builder, and Lombok's builder method uses the
    // literal field name (isDefault(...), not default(...)) — pinned
    // explicitly here so it doesn't silently regress if the builder is removed.
    @Mapping(source = "isDefault", target = "isDefault")
    CustomerAddress toEntity(CustomerAddressRequest request);
}
