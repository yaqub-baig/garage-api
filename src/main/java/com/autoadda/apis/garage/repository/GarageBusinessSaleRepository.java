package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageBusinessSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GarageBusinessSaleRepository extends JpaRepository<GarageBusinessSale, Integer> {

    Optional<GarageBusinessSale> findByListingId(Integer listingId);
}
