package com.dawgs.marketplace.repository;

import com.dawgs.marketplace.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByOwnerId(String ownerId);
    List<Listing> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
