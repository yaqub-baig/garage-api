package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.repository.GarageProcurementRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.PartRequestItemRepository;
import com.autoadda.apis.garage.repository.PartRequestRepository;
import com.autoadda.apis.garage.request.GarageProcurementRequest;
import com.autoadda.apis.garage.request.MarketplaceProcurementRequest;
import com.autoadda.apis.garage.request.MarketplaceProcurementRequest.Stage;
import com.autoadda.apis.garage.response.GaragePurchasingAccessResponse;
import com.autoadda.apis.garage.response.PartPurchaseContextResponse;
import com.autoadda.apis.garage.response.VehicleDetailResponse;
import com.autoadda.apis.garage.service.PartRequestService;
import com.autoadda.apis.garage.service.PartsPurchasingService;
import com.autoadda.apis.garage.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartsPurchasingServiceTest {

    @Mock private GarageRepository garageRepository;
    @Mock private PartRequestItemRepository itemRepository;
    @Mock private PartRequestRepository partRequestRepository;
    @Mock private GarageProcurementRepository procurementRepository;
    @Mock private PartRequestService partRequestService;
    @Mock private VehicleClient vehicleClient;
    @InjectMocks private PartsPurchasingService service;

    private Garage garage;
    private PartRequest request;
    private PartRequestItem item;

    @BeforeEach
    void setUp() {
        garage = new Garage();
        garage.setId(3);
        garage.setName("Speedy Garage");
        garage.setMobileNumber("0501234567");
        garage.setAccountStatus(GarageVerificationStatus.APPROVED);
        garage.setActive(true);
        garage.setGarageLocation(Location.builder().streetAddress1("Plot 12").area("Al Quoz").city("Dubai").gmap("https://maps/x").build());
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(70);
        JobCard jobCard = JobCard.builder().id(50).garage(garage).workOrder(workOrder).build();
        request = PartRequest.builder().id(20).jobCard(jobCard).vehicleId(900).status(PartRequestStatus.GARAGE_WILL_PROCURE).build();
        item = PartRequestItem.builder().id(11).partRequest(request).partName("Brake pads").partNumber("BP-1").quantity(2)
                .procurementStatus(ItemProcurementStatus.PENDING_PROCUREMENT).build();
        when(garageRepository.findById(3)).thenReturn(Optional.of(garage));
        when(itemRepository.findById(11)).thenReturn(Optional.of(item));
        when(procurementRepository.findByPartRequestItemId(11)).thenReturn(Optional.empty());
        when(vehicleClient.getVehicleDetailsByIdResponse(900))
                .thenReturn(VehicleDetailResponse.builder().brand("Toyota").model("Camry").year("2019").build());
    }

    private static MarketplaceProcurementRequest step(Stage stage, long orderItemId) {
        return MarketplaceProcurementRequest.builder().stage(stage).orderId(5).orderItemId(orderItemId)
                .supplierName("Al Noor Parts").supplierContact("0509999999").unitPrice(120.0)
                .condition(PartCondition.GENUINE).build();
    }

    @Test
    void approvedGarageIsReturnedAsBuyerWithDeliveryAddress() {
        GaragePurchasingAccessResponse access = service.purchasingAccess(3);
        assertEquals("Speedy Garage", access.getName());
        assertEquals("0501234567", access.getPhone());
        assertEquals("Plot 12, Al Quoz, Dubai", access.getAddress());
        assertEquals("https://maps/x", access.getMapLink());
    }

    @Test
    void unapprovedOrInactiveGarageCannotBuy() {
        garage.setAccountStatus(GarageVerificationStatus.PENDING);
        assertThrows(ConflictException.class, () -> service.purchasingAccess(3));
        garage.setAccountStatus(GarageVerificationStatus.APPROVED);
        garage.setActive(false);
        assertThrows(ConflictException.class, () -> service.purchasingAccess(3));
    }

    @Test
    void contextHasJobAndVehicle() {
        PartPurchaseContextResponse context = service.context(11);
        assertEquals(3, context.getGarageId());
        assertEquals(70, context.getWorkOrderId());
        assertEquals(50, context.getJobCardId());
        assertEquals("Toyota", context.getBrand());
        assertEquals("Camry", context.getModel());
        assertEquals("2019", context.getYear());
        assertTrue(context.isPurchasable());
    }

    @Test
    void contextStillWorksWhenVehicleApiFails() {
        when(vehicleClient.getVehicleDetailsByIdResponse(900)).thenThrow(new RuntimeException("down"));
        PartPurchaseContextResponse context = service.context(11);
        assertNull(context.getBrand());
        assertEquals(50, context.getJobCardId());
    }

    @Test
    void notPurchasableUntilCustomerLetsTheGarageBuy() {
        request.setStatus(PartRequestStatus.PENDING_CUSTOMER_RESPONSE);
        assertFalse(service.context(11).isPurchasable());
        assertThrows(ConflictException.class, () -> service.marketplaceUpdate(11, step(Stage.ORDERED, 81L)));
    }

    @Test
    void orderedMarksTheItemAndRequestAndIsIdempotent() {
        service.marketplaceUpdate(11, step(Stage.ORDERED, 81L));
        assertEquals(ItemProcurementStatus.ORDERED, item.getProcurementStatus());
        assertEquals(81L, item.getMarketplaceOrderItemId());
        assertEquals("Al Noor Parts", item.getMarketplaceSupplierName());
        assertEquals(PartRequestStatus.GARAGE_PROCURING, request.getStatus());

        service.marketplaceUpdate(11, step(Stage.ORDERED, 81L)); // repeat: fine
        assertThrows(ConflictException.class, () -> service.marketplaceUpdate(11, step(Stage.ORDERED, 82L)));
    }

    @Test
    void cancelledPutsOnlyTheMatchingOrderBackToPending() {
        service.marketplaceUpdate(11, step(Stage.ORDERED, 81L));
        service.marketplaceUpdate(11, step(Stage.CANCELLED, 99L)); // another order item: ignored
        assertEquals(ItemProcurementStatus.ORDERED, item.getProcurementStatus());
        service.marketplaceUpdate(11, step(Stage.CANCELLED, 81L));
        assertEquals(ItemProcurementStatus.PENDING_PROCUREMENT, item.getProcurementStatus());
        assertNull(item.getMarketplaceOrderItemId());
    }

    @Test
    void receivedCreatesTheProcurementLikeAManualOne() {
        service.marketplaceUpdate(11, step(Stage.ORDERED, 81L));
        GarageProcurement created = GarageProcurement.builder().id(1).partRequestItem(item).build();
        when(procurementRepository.findByPartRequestItemId(11)).thenReturn(Optional.empty(), Optional.of(created));

        service.marketplaceUpdate(11, step(Stage.RECEIVED, 81L));

        ArgumentCaptor<GarageProcurementRequest> captor = ArgumentCaptor.forClass(GarageProcurementRequest.class);
        verify(partRequestService).addGarageProcurement(captor.capture());
        assertEquals("Al Noor Parts", captor.getValue().getSupplierName());
        assertEquals(120.0, captor.getValue().getPurchasePrice());
        assertEquals(PartCondition.GENUINE, captor.getValue().getPartCondition());
        assertEquals("AutoAdda order #5", captor.getValue().getReceiptNumber());
        assertEquals(81L, created.getMarketplaceOrderItemId());
        assertNotNull(item.getReceivedAt());
    }

    @Test
    void receivedTwiceForTheSameOrderItemDoesNothingTheSecondTime() {
        GarageProcurement done = GarageProcurement.builder().id(1).marketplaceOrderItemId(81L).build();
        when(procurementRepository.findByPartRequestItemId(11)).thenReturn(Optional.of(done));
        service.marketplaceUpdate(11, step(Stage.RECEIVED, 81L));
        verify(partRequestService, never()).addGarageProcurement(any());

        assertThrows(ConflictException.class, () -> service.marketplaceUpdate(11, step(Stage.RECEIVED, 82L)));
    }
}
