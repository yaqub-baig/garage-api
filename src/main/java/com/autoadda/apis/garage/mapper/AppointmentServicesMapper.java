package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.request.AppointmentServiceRequest;
import com.autoadda.apis.garage.response.AppointmentServiceResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor

public class AppointmentServicesMapper {
   public AppointmentServiceResponse entityToResponse(AppointmentServices service) {
       return AppointmentServiceResponse.builder()
               .appointmentId(service.getAppointmentId())
               .id(service.getId())
               .status(service.getStatus())
               .technicianId(service.getTechnician() != null ? service.getTechnician().getId() : null)
               .serviceId(service.getService() != null ? service.getService().getId() : null)
               .build();
   }
    public AppointmentServices requestToEntity(AppointmentServiceRequest request) {

        return AppointmentServices.builder ( )
                .status (  request.getStatus ())
                .build ( );
    }

}
