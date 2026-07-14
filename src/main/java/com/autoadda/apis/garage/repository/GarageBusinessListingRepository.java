package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageBusinessListing;
import com.autoadda.apis.garage.util.GarageListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarageBusinessListingRepository extends JpaRepository<GarageBusinessListing, Integer> {

    // Owner's own listings, any status
    List<GarageBusinessListing> findByGarageId(Integer garageId);

    // Public marketplace browse
    Page<GarageBusinessListing> findByStatus(GarageListingStatus status, Pageable pageable);

    // Prevent more than one active/under-negotiation listing per garage
    List<GarageBusinessListing> findByGarageIdAndStatusIn(Integer garageId, List<GarageListingStatus> statuses);

    // Search active listings by title/description with optional price range
    @Query("SELECT l FROM GarageBusinessListing l WHERE l.status = :status " +
           "AND (:searchTerm IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(l.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:minPrice IS NULL OR l.askingPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR l.askingPrice <= :maxPrice)")
    Page<GarageBusinessListing> searchListings(@Param("status") GarageListingStatus status,
                                                @Param("searchTerm") String searchTerm,
                                                @Param("minPrice") Double minPrice,
                                                @Param("maxPrice") Double maxPrice,
                                                Pageable pageable);
}
