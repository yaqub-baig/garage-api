package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageBusinessSaleInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GarageBusinessSaleInvoiceRepository extends JpaRepository<GarageBusinessSaleInvoice, Integer> {

    Optional<GarageBusinessSaleInvoice> findBySaleId(Integer saleId);
}
