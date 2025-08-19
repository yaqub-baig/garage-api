package com.autoadda.apis.garage.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.GarageSubscription;

@Repository
public interface GarageSubscriptionRepository extends JpaRepository<GarageSubscription, Integer> {
	List<GarageSubscription> findByGarageId(Integer garageId);

	// Additional custom query methods can be defined here if needed
	// For example, to find active subscriptions:
	List<GarageSubscription> findByGarageIdAndIsActive(Integer garageId, Boolean isActive);

	@Query("""
        SELECT gs FROM GarageSubscription gs
        WHERE gs.isActive = true
        AND (:searchTerm IS NULL OR :searchTerm = '' OR
             LOWER(gs.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
             LOWER(gs.brands) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
             LOWER(gs.garage.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        """)
	List<GarageSubscription> findByNameContainingIgnoreCase(@Param("searchTerm") String name);

	// Or to find subscriptions by price range:
	List<GarageSubscription> findByPriceBetween(Double minPrice, Double maxPrice);

	// Or to find subscriptions by duration:
	List<GarageSubscription> findByDurationMonths(Integer durationMonths);

	// Or to find subscriptions by description:
	List<GarageSubscription> findByDescriptionContainingIgnoreCase(String description);

	// Or to find subscriptions by services included:
	List<GarageSubscription> findByServicesIncludedId(Integer garageServiceId);

	@Query("SELECT DISTINCT gs FROM GarageSubscription gs " +
			"LEFT JOIN FETCH gs.garage g " +
			"LEFT JOIN FETCH g.garageLocation")
	Set<GarageSubscription> findAllWithLocationAndServices();
}