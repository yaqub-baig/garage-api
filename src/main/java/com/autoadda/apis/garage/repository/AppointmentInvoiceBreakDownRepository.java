package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.AppointmentInvoiceBreakDown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentInvoiceBreakDownRepository extends JpaRepository<AppointmentInvoiceBreakDown, Integer> {

}
