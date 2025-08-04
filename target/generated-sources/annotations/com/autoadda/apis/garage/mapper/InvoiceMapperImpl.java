package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentInvoice;
import com.autoadda.apis.garage.entity.AppointmentInvoiceBreakDown;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.request.AppointmentInvoiceRequest;
import com.autoadda.apis.garage.request.ServiceInvoice;
import com.autoadda.apis.garage.util.DateHelper;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-03T11:25:45-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.v20250526-2018, environment: Java 23.0.2 (Eclipse Adoptium)"
)
@Component
public class InvoiceMapperImpl implements InvoiceMapper {

    @Override
    public AppointmentInvoice toEntity(AppointmentInvoiceRequest request, Garage garage, AppointmentDetails appointment) {
        if ( request == null && garage == null && appointment == null ) {
            return null;
        }

        AppointmentInvoice appointmentInvoice = new AppointmentInvoice();

        if ( request != null ) {
            appointmentInvoice.setTaxId( request.getTaxId() );
            appointmentInvoice.setComments( request.getComments() );
            appointmentInvoice.setTaxAmount( request.getTaxAmount() );
        }
        appointmentInvoice.setGarage( garage );
        appointmentInvoice.setAppointment( appointment );
        appointmentInvoice.setInsert_tms( DateHelper.dbDate() );
        appointmentInvoice.setStatus( "GENERATED" );
        appointmentInvoice.setNetAmount( calculateNetAmount(request) );
        appointmentInvoice.setTotalAmount( calculateNetAmount(request) + request.getTaxAmount() );

        return appointmentInvoice;
    }

    @Override
    public AppointmentInvoiceBreakDown toBreakdown(ServiceInvoice service, AppointmentInvoice invoice, Garage garage, AppointmentDetails appointment) {
        if ( service == null && invoice == null && garage == null && appointment == null ) {
            return null;
        }

        AppointmentInvoiceBreakDown appointmentInvoiceBreakDown = new AppointmentInvoiceBreakDown();

        if ( service != null ) {
            appointmentInvoiceBreakDown.setNetAmount( service.getAmount() );
            appointmentInvoiceBreakDown.setTotalAmount( service.getAmount() );
            appointmentInvoiceBreakDown.setService( service.getService() );
            appointmentInvoiceBreakDown.setComments( service.getComments() );
        }
        if ( invoice != null ) {
            appointmentInvoiceBreakDown.setInvoiceId( invoice.getId() );
            appointmentInvoiceBreakDown.setTaxAmount( invoice.getTaxAmount() );
        }
        if ( garage != null ) {
            appointmentInvoiceBreakDown.setGarageId( garage.getId() );
        }
        if ( appointment != null ) {
            appointmentInvoiceBreakDown.setAppointmentId( appointment.getId() );
        }
        appointmentInvoiceBreakDown.setInsert_tms( DateHelper.dbDate() );

        return appointmentInvoiceBreakDown;
    }
}
