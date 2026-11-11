package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.mapper.PartRequestMapper;
import com.autoadda.apis.garage.repository.PartRequestItemRepository;
import com.autoadda.apis.garage.response.PartRequestResponse;
import com.autoadda.apis.garage.response.PartStatusTimelineEntry;
import com.autoadda.apis.garage.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartRequestTimelineTest {

    @Mock private PartRequestItemRepository itemRepository;
    @Mock private VoiceNoteHelper voiceNoteHelper;
    @InjectMocks private PartRequestMapper mapper;

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 1, 9, 0);

    @Test
    void updateStatusRecordsTimeAndIgnoresRepeats() {
        PartRequest pr = PartRequest.builder().status(PartRequestStatus.PENDING_CUSTOMER_RESPONSE).build();
        pr.updateStatus(PartRequestStatus.GARAGE_WILL_PROCURE);
        LocalDateTime first = pr.getStatusTimestamps().get(PartRequestStatus.GARAGE_WILL_PROCURE);
        assertNotNull(first);
        pr.updateStatus(PartRequestStatus.GARAGE_WILL_PROCURE);
        assertSame(first, pr.getStatusTimestamps().get(PartRequestStatus.GARAGE_WILL_PROCURE));
        assertEquals(PartRequestStatus.GARAGE_WILL_PROCURE, pr.getStatus());
    }

    @Test
    void timelineMergesRequestAndPartEventsInOrderWithDurations() {
        Map<PartRequestStatus, LocalDateTime> times = new EnumMap<>(PartRequestStatus.class);
        times.put(PartRequestStatus.PENDING_CUSTOMER_RESPONSE, T0);
        times.put(PartRequestStatus.GARAGE_WILL_PROCURE, T0.plusHours(1));
        times.put(PartRequestStatus.PART_RECEIVED, T0.plusHours(5));
        PartRequest pr = PartRequest.builder().requestDate(T0).statusTimestamps(times).build();

        PartRequestItem pads = PartRequestItem.builder().id(11).partName("Brake pads").build();
        pads.setGarageProcurement(GarageProcurement.builder().supplierName("Al Futtaim").procurementDate(T0.plusHours(5)).build());
        pads.setAddedToInvoiceAt(T0.plusHours(6));

        List<PartStatusTimelineEntry> timeline = mapper.buildTimeline(pr, List.of(pads));

        assertEquals(List.of("PENDING_CUSTOMER_RESPONSE", "GARAGE_WILL_PROCURE", "PART_RECEIVED", "PART_PROCURED", "PART_ADDED_TO_INVOICE"),
                timeline.stream().map(PartStatusTimelineEntry::getEvent).toList());
        assertNull(timeline.get(0).getDurationInMinutes());
        assertEquals(60, timeline.get(1).getDurationInMinutes());
        assertEquals(0, timeline.get(3).getDurationInMinutes());
        assertEquals("ITEM", timeline.get(3).getLevel());
        assertEquals(11, timeline.get(3).getPartRequestItemId());
        assertTrue(timeline.get(3).getDescription().contains("Al Futtaim"));
    }

    @Test
    void oldRequestsFallBackToKnownDatesAndSkipUnknownSteps() {
        CustomerPartResponse choice = CustomerPartResponse.builder().choice(CustomerChoice.SELF_PROVIDE).build();
        PartRequest pr = PartRequest.builder()
                .status(PartRequestStatus.PART_RECEIVED)          // reached, but when is unknown
                .requestDate(T0).customerResponseDate(T0.plusDays(1)).customerResponse(choice)
                .statusTimestamps(new EnumMap<>(PartRequestStatus.class))
                .build();

        List<PartStatusTimelineEntry> timeline = mapper.buildTimeline(pr, List.of());

        assertEquals(List.of("PENDING_CUSTOMER_RESPONSE", "WAITING_FOR_CUSTOMER_DELIVERY"),
                timeline.stream().map(PartStatusTimelineEntry::getEvent).toList());
        assertEquals(T0.plusDays(1), timeline.get(1).getTimestamp());
    }

    @Test
    void responseCarriesItemsProcurementCustomerChoiceAndTimeline() {
        Technician tech = Technician.builder().id(3).firstName("Sara").build();
        JobCard jobCard = JobCard.builder().id(55).build();
        CustomerPartResponse choice = CustomerPartResponse.builder().id(4).choice(CustomerChoice.GARAGE_PROCURE)
                .partCondition(PartCondition.GENUINE).build();
        PartRequest pr = PartRequest.builder().id(9).jobCard(jobCard).requestedBy(tech)
                .status(PartRequestStatus.GARAGE_PROCURING).requestDate(T0).customerResponse(choice)
                .statusTimestamps(new EnumMap<>(PartRequestStatus.class)).build();
        PartRequestItem item = PartRequestItem.builder().id(11).partName("Oil filter").partNumber("OF-1").quantity(1)
                .estimatedPrice(40.0).procurementStatus(ItemProcurementStatus.PROCURED).build();
        item.setGarageProcurement(GarageProcurement.builder().id(2).supplierName("Parts Co").purchasePrice(35.0)
                .partCondition(PartCondition.GENUINE).procurementDate(T0.plusHours(2)).procuredBy(tech).build());
        when(itemRepository.findByPartRequestId(9)).thenReturn(List.of(item));

        PartRequestResponse r = mapper.toResponse(pr, null);

        assertEquals(55, r.getJobCardId());
        assertNull(r.getJobCardServiceName());   // no service on this job card: no crash
        assertEquals("Oil filter", r.getItems().get(0).getPartName());
        assertEquals("Parts Co", r.getItems().get(0).getProcurement().getSupplierName());
        assertEquals("Sara", r.getItems().get(0).getProcurement().getProcuredByUserName());
        assertEquals(T0.plusHours(2), r.getItems().get(0).getProcuredAt());
        assertEquals(CustomerChoice.GARAGE_PROCURE, r.getCustomerResponse().getChoice());
        assertEquals(2, r.getTimeline().size());
    }
}
