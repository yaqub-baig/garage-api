package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AppointmentInvoice;
import com.autoadda.apis.garage.response.AppointmentInvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentInvoiceMapper {
    private final GarageMapper garageMapper;
    private final AppointmentDetailMapper appointmentDetailMapper;

    public AppointmentInvoiceResponse fromEntity(AppointmentInvoice invoice) {
        if ( invoice == null ) {
            return null;
        }

        return AppointmentInvoiceResponse.builder ( )
                .id ( invoice.getId ( ) )
                .status ( invoice.getStatus ( ) )
                .comments ( invoice.getComments ( ) )
                .insertTms ( invoice.getInsert_tms ( ) )
                .netAmount ( invoice.getNetAmount ( ) )
                .taxAmount ( invoice.getTaxAmount ( ) )
                .totalAmount ( invoice.getTotalAmount ( ) )
                .taxId ( invoice.getTaxId ( ) )
                .garage ( garageMapper.entityToResponse ( invoice.getGarage ( ) ) )
                .appointment ( appointmentDetailMapper.entityToResponse ( invoice.getAppointment ( ) ) )
                .build ( );
    }

}
