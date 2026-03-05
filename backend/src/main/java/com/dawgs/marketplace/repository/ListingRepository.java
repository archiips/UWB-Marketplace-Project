package com.dawgs.marketplace.repository;

import com.dawgs.marketplace.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
}
