package com.autoadda.apis.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.AppointmentInvoice;

@Repository
public interface AppointmentInvoiceRepository extends JpaRepository<AppointmentInvoice, Integer>{

	AppointmentInvoice findByAppointmentId(Integer appointmentId);

}
