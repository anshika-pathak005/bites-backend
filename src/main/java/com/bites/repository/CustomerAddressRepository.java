package com.bites.repository;

import com.bites.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Data access for a guest's saved delivery addresses.
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    // Used to list all saved addresses for a guest, default address first —
    // powers the address book screen and the checkout address picker.
    List<CustomerAddress> findByGuestIdOrderByIsDefaultDesc(String guestId);
    // Used when a new/edited address is marked default, to unset the flag
    // on every other address this guest has first.
    List<CustomerAddress> findByGuestIdAndIsDefaultTrue(String guestId);
}
