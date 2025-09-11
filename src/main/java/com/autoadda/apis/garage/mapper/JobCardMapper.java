package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.response.JobCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobCardMapper {


    public JobCardResponse entityToResponse(JobCard jobCard) {
        return JobCardResponse.builder ( )
                                .serviceStartTime ( jobCard.getServiceStartTime ( ) != null ? jobCard.getServiceStartTime ( ) : null )
                                .serviceEndTime ( jobCard.getServiceEndTime ( ) != null ? jobCard.getServiceEndTime ( ) : null )
                                .workOrderId ( jobCard.getWorkOrder ( ) != null ? jobCard.getWorkOrder ( ).getId ( ) : null )
                                .id ( jobCard.getId ( ) )
                                .technicianId ( jobCard.getTechnician ( ) != null ? jobCard.getTechnician ( ).getId ( ) : null )
                                .status ( jobCard.getStatus ( ).name ( ) )
                              .build ( );
    }
}
