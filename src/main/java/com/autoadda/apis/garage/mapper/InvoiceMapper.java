package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentInvoice;
import com.autoadda.apis.garage.entity.AppointmentInvoiceBreakDown;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.request.AppointmentInvoiceRequest;
import com.autoadda.apis.garage.request.ServiceInvoice;
import com.autoadda.apis.garage.util.DateHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {DateHelper.class})
public interface InvoiceMapper {

    // ✅ Map AppointmentInvoiceRequest to AppointmentInvoice
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", source = "appointment")
    @Mapping(target = "garage", source = "garage")
    @Mapping(target = "insert_tms", expression = "java(DateHelper.dbDate())")
    @Mapping(target = "status", constant = "GENERATED")
    @Mapping(target = "netAmount", expression = "java(calculateNetAmount(request))")
    @Mapping(target = "totalAmount", expression = "java(calculateNetAmount(request) + request.getTaxAmount())")
    @Mapping(target = "taxId", source = "request.taxId")
    @Mapping(target = "comments", source = "request.comments")
    AppointmentInvoice toEntity(AppointmentInvoiceRequest request, Garage garage, AppointmentDetails appointment);

    // ✅ Wrapper for semantic clarity
    default AppointmentInvoice toInvoice(AppointmentInvoiceRequest request, Garage garage, AppointmentDetails appointment) {
        return toEntity ( request, garage, appointment );
    }

    // ✅ Convert individual ServiceInvoice to AppointmentInvoiceBreakDown
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "insert_tms", expression = "java(DateHelper.dbDate())")
    @Mapping(target = "invoiceId", source = "invoice.id")
    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "garageId", source = "garage.id")
    @Mapping(target = "netAmount", source = "service.amount")
    @Mapping(target = "totalAmount", source = "service.amount")
    @Mapping(target = "service", source = "service.service")
    @Mapping(target = "comments", source = "service.comments")
    AppointmentInvoiceBreakDown toBreakdown(ServiceInvoice service, AppointmentInvoice invoice, Garage garage, AppointmentDetails appointment);

    // ✅ Map all service lines into breakdowns
    default List<AppointmentInvoiceBreakDown> toBreakdownList(AppointmentInvoiceRequest request, AppointmentInvoice invoice, Garage garage, AppointmentDetails appointment) {
        return request.getService ( ).stream ( )
                .map ( service -> toBreakdown ( service, invoice, garage, appointment ) )
                .collect ( Collectors.toList ( ) );
    }

    // ✅ Helper method to calculate net amount
    default Double calculateNetAmount(AppointmentInvoiceRequest request) {
        return request.getService ( ).stream ( )
                .mapToDouble ( ServiceInvoice::getAmount )
                .sum ( );
    }
}
// This mapper interface uses MapStruct to convert between request objects and entity objects for appointment invoices.