package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.AppointmentInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentInvoiceRepository extends JpaRepository<AppointmentInvoice, Integer> {

    AppointmentInvoice findByAppointmentId(Integer appointmentId);

}
