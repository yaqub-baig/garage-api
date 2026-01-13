package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GarageRatingRepository extends JpaRepository<GarageRating, Integer> {

    Page<GarageRating> findByGarageIdAndIsVisibleTrue(Integer garageId, Pageable pageable);

    Page<GarageRating> findByGarageIdAndRatingAndIsVisibleTrue(Integer garageId, Integer rating, Pageable pageable);

    Optional<GarageRating> findByWorkOrderId(Integer workOrderId);

    List<GarageRating> findByCustomerId(Integer customerId);

    boolean existsByWorkOrderId(Integer workOrderId);

    long countByGarageIdAndIsVisibleTrue(Integer garageId);

    @Query("SELECT AVG(r.rating) FROM GarageRating r WHERE r.garage.id = :garageId AND r.isVisible = true")
    Double calculateAverageRatingByGarageId(@Param("garageId") Integer garageId);

    @Query("SELECT r.rating, COUNT(r) FROM GarageRating r WHERE r.garage.id = :garageId AND r.isVisible = true GROUP BY r.rating")
    List<Object[]> getRatingDistributionByGarageId(@Param("garageId") Integer garageId);

    List<GarageRating> findByGarageIdAndIsVisibleTrue(Integer garageId);
}
