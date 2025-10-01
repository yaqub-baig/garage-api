package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.VehicleSaleInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleSaleInvoiceRepository extends JpaRepository<VehicleSaleInvoice, Integer> {
    // Custom query methods can be added here if needed
    // For example, to find by vehicle sale ID or status
    List<VehicleSaleInvoice> findByVehicleSaleId(Integer vehicleSaleId);

}
