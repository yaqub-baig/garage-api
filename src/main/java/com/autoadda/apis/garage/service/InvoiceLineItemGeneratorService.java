package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.request.InvoiceLineItemRequest;
import com.autoadda.apis.garage.util.JobCardStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceLineItemGeneratorService {

    /**
     * Generates line items from a WorkOrder based on completed JobCards
     *
     * @param workOrder the WorkOrder to generate line items from
     * @return list of InvoiceLineItemRequest objects
     */
    public List<InvoiceLineItemRequest> generateLineItemsFromWorkOrder(WorkOrder workOrder) {
        List<InvoiceLineItemRequest> lineItems = new ArrayList<> ( );

        // Filter only completed job cards
        List<JobCard> completedJobCards = workOrder.getJobCards ( ).stream ( )
                .filter ( jc -> jc.getStatus ( ) == JobCardStatus.WORK_COMPLETED ||
                        jc.getStatus ( ) == JobCardStatus.QUALITY_CHECK_PASSED ||
                        jc.getStatus ( ) == JobCardStatus.COMPLETED )
                .toList ( );

        if ( completedJobCards.isEmpty ( ) ) {
            log.warn ( "No completed job cards found for WorkOrder ID: {}", workOrder.getId ( ) );
            return lineItems;
        }

        // Generate service line items from job cards
        for (JobCard jobCard : completedJobCards) {
            AppointmentServices appointmentService = jobCard.getService ( );
            if ( appointmentService != null && appointmentService.getService ( ) != null ) {
                AvailableService availableService = appointmentService.getService ( ).getAvailableService ( );

                InvoiceLineItemRequest serviceItem = InvoiceLineItemRequest.builder ( )
                        .description ( availableService.getName ( ) )
                        .quantity ( 1 )
                        .price ( appointmentService.getService ( ).getPrice ( ) )
                        .category ( "SERVICE" )
                        .serviceId ( availableService.getId ( ) )
                        .notes ( "JobCard ID: " + jobCard.getId ( ) )
                        .build ( );

                lineItems.add ( serviceItem );
            }
        }

        // Add labor charges from each appointment service
        for (JobCard jobCard : completedJobCards) {
            AppointmentServices appointmentService = jobCard.getService ( );
            if ( appointmentService != null && appointmentService.getLabourPrice ( ) != null && appointmentService.getLabourPrice ( ) > 0 ) {
                InvoiceLineItemRequest laborItem = InvoiceLineItemRequest.builder ( )
                        .description ( "Labor - " + (appointmentService.getService ( ) != null &&
                                appointmentService.getService ( ).getAvailableService ( ) != null ?
                                appointmentService.getService ( ).getAvailableService ( ).getName ( ) : "Service") )
                        .quantity ( 1 )
                        .price ( appointmentService.getLabourPrice ( ) )
                        .category ( "LABOR" )
                        .serviceId ( appointmentService.getService ( ) != null &&
                                appointmentService.getService ( ).getAvailableService ( ) != null ?
                                appointmentService.getService ( ).getAvailableService ( ).getId ( ) : null )
                        .notes ( "JobCard ID: " + jobCard.getId ( ) )
                        .build ( );

                lineItems.add ( laborItem );
            }
        }

        // TODO: Add parts/materials if you have that data structure
        // addPartsLineItems(workOrder, lineItems);

        return lineItems;
    }


    /**
     * Generates standard line items that can be customized
     */
    public List<InvoiceLineItemRequest> generateStandardLineItems() {
        List<InvoiceLineItemRequest> standardItems = new ArrayList<> ( );

        // Common service items
        standardItems.add ( InvoiceLineItemRequest.builder ( )
                .description ( "Diagnostic Fee" )
                .quantity ( 1 )
                .price ( 25.0 )
                .category ( "SERVICE" )
                .build ( ) );

        standardItems.add ( InvoiceLineItemRequest.builder ( )
                .description ( "Shop Supplies" )
                .quantity ( 1 )
                .price ( 15.0 )
                .category ( "MATERIALS" )
                .build ( ) );

        return standardItems;
    }
}