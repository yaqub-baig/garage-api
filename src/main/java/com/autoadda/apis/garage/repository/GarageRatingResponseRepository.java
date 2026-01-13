package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageRatingResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GarageRatingResponseRepository extends JpaRepository<GarageRatingResponse, Integer> {

    Optional<GarageRatingResponse> findByGarageRatingId(Integer garageRatingId);

    boolean existsByGarageRatingId(Integer garageRatingId);
}
