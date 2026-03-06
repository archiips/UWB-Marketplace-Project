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

    // GET /api/listings?search=query — public
    @GetMapping
    public List<Listing> getAllListings(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return listingRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        }
        return listingRepository.findAll();
    }

    // GET /api/my-listings — requires JWT
    @GetMapping("/my-listings")
    public List<Listing> getMyListings(Authentication auth) {
        return listingRepository.findByOwnerId((String) auth.getPrincipal());
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

    // PUT /api/listings/{id} — requires JWT, owner only (mark sold, update fields)
    @PutMapping("/{id}")
    public ResponseEntity<Listing> updateListing(@PathVariable UUID id,
                                                  @RequestBody Listing patch,
                                                  Authentication auth) {
        return listingRepository.findById(id)
                .<ResponseEntity<Listing>>map(listing -> {
                    if (!listing.getOwnerId().equals(auth.getPrincipal())) {
                        return ResponseEntity.<Listing>status(403).build();
                    }
                    if (patch.getTitle() != null) listing.setTitle(patch.getTitle());
                    if (patch.getDescription() != null) listing.setDescription(patch.getDescription());
                    if (patch.getPrice() > 0) listing.setPrice(patch.getPrice());
                    listing.setSold(patch.isSold());
                    return ResponseEntity.ok(listingRepository.save(listing));
                })
                .orElse(ResponseEntity.<Listing>notFound().build());
    }

    // DELETE /api/listings/{id} — requires JWT, owner only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable UUID id, Authentication auth) {
        return listingRepository.findById(id)
                .<ResponseEntity<Void>>map(listing -> {
                    if (!listing.getOwnerId().equals(auth.getPrincipal())) {
                        return ResponseEntity.<Void>status(403).build();
                    }
                    listingRepository.deleteById(id);
                    return ResponseEntity.<Void>noContent().build();
                })
                .orElse(ResponseEntity.<Void>notFound().build());
    }
}
