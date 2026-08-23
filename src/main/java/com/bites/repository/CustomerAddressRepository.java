package com.bites.repository;

import com.bites.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Data access for a guest's or a logged-in customer's saved delivery addresses.
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    // Used to list all saved addresses for a guest, default address first —
    // powers the address book screen and the checkout address picker.
    // customerId IS NULL excludes any address that has since been merged
    // into a customer account, so a stale guest cookie can never see it again.
    List<CustomerAddress> findByGuestIdAndCustomerIdIsNullOrderByIsDefaultDesc(String guestId);
    // Used when a new/edited address is marked default, to unset the flag
    // on every other address this guest has first.
    List<CustomerAddress> findByGuestIdAndCustomerIdIsNullAndIsDefaultTrue(String guestId);
    // Same two queries as above, but for a logged-in customer instead of a guest.
    List<CustomerAddress> findByCustomerIdOrderByIsDefaultDesc(Long customerId);
    List<CustomerAddress> findByCustomerIdAndIsDefaultTrue(Long customerId);
}
