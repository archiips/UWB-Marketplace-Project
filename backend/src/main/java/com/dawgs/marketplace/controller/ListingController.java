package com.dawgs.marketplace.controller;

import com.dawgs.marketplace.model.Listing;
import com.dawgs.marketplace.repository.ListingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingRepository listingRepository;

    public ListingController(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    // GET /api/listings — public
    @GetMapping
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    // GET /api/listings/{id} — public
    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListingById(@PathVariable UUID id) {
        return listingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/listings — requires JWT
    @PostMapping
    public Listing createListing(@RequestBody Listing listing, Authentication auth) {
        listing.setOwnerId((String) auth.getPrincipal());
        return listingRepository.save(listing);
    }

    // DELETE /api/listings/{id} — requires JWT, owner only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable UUID id, Authentication auth) {
        return listingRepository.findById(id)
                .map(listing -> {
                    if (!listing.getOwnerId().equals(auth.getPrincipal())) {
                        return ResponseEntity.<Void>status(403).build();
                    }
                    listingRepository.deleteById(id);
                    return ResponseEntity.<Void>noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
