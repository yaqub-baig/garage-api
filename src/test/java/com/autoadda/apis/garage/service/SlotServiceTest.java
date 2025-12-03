package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.SlotMapper;
import com.autoadda.apis.garage.repository.SlotRepository;
import com.autoadda.apis.garage.request.AppointmentSlotRequest;
import com.autoadda.apis.garage.response.AppointmentSlotResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SlotService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for SlotService")
class SlotServiceTest {

    // Test data constants
    private static final Integer TEST_SLOT_ID = 1;
    private static final Integer TEST_GARAGE_ID = 10;
    private static final LocalTime TEST_START_TIME = LocalTime.of ( 9, 0 );
    private static final LocalTime TEST_END_TIME = LocalTime.of ( 10, 0 );
    private static final LocalDate TEST_DATE = LocalDate.now ( ).plusDays ( 1 );
    private static final Integer TEST_MAX_BOOKINGS = 5;
    private static final Integer TEST_CURRENT_BOOKINGS = 2;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private SlotMapper slotMapper;
    @InjectMocks
    private SlotService slotService;
    // Test entities and DTOs
    private AppointmentSlot slot;
    private AppointmentSlotRequest slotRequest;
    private AppointmentSlotResponse slotResponse;

    /**
     * Initializes common test data before each test method.
     */
    @BeforeEach
    void setUp() {
        // Initialize the main Slot entity
        slot = new AppointmentSlot ( );
        slot.setId ( TEST_SLOT_ID );
        slot.setStartTime ( TEST_START_TIME );
        slot.setEndTime ( TEST_END_TIME );
        slot.setMaxBookings ( TEST_MAX_BOOKINGS );
        slot.setCurrentBookings ( TEST_CURRENT_BOOKINGS );

        slot.setSlotDate ( TEST_DATE );

        // Initialize Request DTO
        slotRequest = new AppointmentSlotRequest ( );
        slotRequest.setStartTime ( TEST_START_TIME );
        slotRequest.setEndTime ( TEST_END_TIME );
        slotRequest.setMaxBookings ( TEST_MAX_BOOKINGS );
        slotRequest.setCurrentBookings ( TEST_CURRENT_BOOKINGS );

        // Initialize Response DTO
        slotResponse = new AppointmentSlotResponse ( );
        slotResponse.setId ( TEST_SLOT_ID );
        slotResponse.setStartTime ( TEST_START_TIME );
        slotResponse.setEndTime ( TEST_END_TIME );
        slotResponse.setMaxBookings ( TEST_MAX_BOOKINGS );
        slotResponse.setCurrentBookings ( TEST_CURRENT_BOOKINGS );
    }

    // ------------------------------------------------------------------------------------
    // TEST CASES - CREATE & READ
    // ------------------------------------------------------------------------------------

    @Test
    @DisplayName("Create slot with valid request - should save and return response")
    void createSlot_WithValidRequest_ShouldSaveAndReturnResponse() {
        // Arrange
        when ( slotRepository.save ( any ( AppointmentSlot.class ) ) ).thenReturn ( slot );
        when ( slotMapper.toResponse ( slot ) ).thenReturn ( slotResponse );

        // Act
        AppointmentSlotResponse result = slotService.createSlot ( slotRequest );

        // Assert
        assertNotNull ( result, "The created slot response should not be null" );
        assertEquals ( TEST_SLOT_ID, result.getId ( ), "The slot ID should match" );

        verify ( slotRepository, times ( 1 ) ).save ( any ( AppointmentSlot.class ) );
        verify ( slotMapper, times ( 1 ) ).toResponse ( slot );
    }

    @Test
    @DisplayName("Get all slots - should return list of slot responses")
    void getAllSlots_ShouldReturnListOfResponses() {
        // Arrange
        when ( slotRepository.findAll ( ) ).thenReturn ( List.of ( slot ) );
        when ( slotMapper.toResponse ( slot ) ).thenReturn ( slotResponse );

        // Act
        List<AppointmentSlotResponse> result = slotService.getAllSlots ( );

        // Assert
        assertNotNull ( result, "Returned list should not be null" );
        assertEquals ( 1, result.size ( ), "List should contain one slot" );
        assertEquals ( TEST_SLOT_ID, result.get ( 0 ).getId ( ), "Slot ID should match" );

        verify ( slotRepository, times ( 1 ) ).findAll ( );
        verify ( slotMapper, times ( 1 ) ).toResponse ( slot );
    }

    @Test
    @DisplayName("Get slot by valid ID - should return slot response")
    void getSlotById_WithValidId_ShouldReturnResponse() {
        // Arrange
        when ( slotRepository.findById ( TEST_SLOT_ID ) ).thenReturn ( Optional.of ( slot ) );
        when ( slotMapper.toResponse ( slot ) ).thenReturn ( slotResponse );

        // Act
        AppointmentSlotResponse result = slotService.getSlotById ( TEST_SLOT_ID );

        // Assert
        assertNotNull ( result, "Returned slot should not be null" );
        assertEquals ( TEST_SLOT_ID, result.getId ( ), "Slot ID should match" );

        verify ( slotRepository, times ( 1 ) ).findById ( TEST_SLOT_ID );
        verify ( slotMapper, times ( 1 ) ).toResponse ( slot );
    }

    @Test
    @DisplayName("Get slot by invalid ID - should throw ResourceNotFoundException")
    void getSlotById_WithInvalidId_ShouldThrowException() {
        // Arrange
        Integer invalidId = 999;
        when ( slotRepository.findById ( invalidId ) ).thenReturn ( Optional.empty ( ) );

        // Act & Assert
        ResourceNotFoundException exception = assertThrows (
                ResourceNotFoundException.class,
                () -> slotService.getSlotById ( invalidId ),
                "Expected getSlotById() to throw ResourceNotFoundException"
        );

        assertTrue ( exception.getMessage ( ).contains ( "slot not found" ) );

        verify ( slotRepository, times ( 1 ) ).findById ( invalidId );
        verify ( slotMapper, never ( ) ).toResponse ( any ( ) );
    }

    // ------------------------------------------------------------------------------------
    // TEST CASES - UPDATE
    // ------------------------------------------------------------------------------------

    @Test
    @DisplayName("Update slot with valid data - should update and return response")
    void updateSlot_WithValidData_ShouldUpdateAndReturnResponse() {
        // Arrange
        AppointmentSlotRequest updateRequest = new AppointmentSlotRequest ( );
        updateRequest.setStartTime ( LocalTime.of ( 10, 0 ) ); // New time
        updateRequest.setMaxBookings ( 10 ); // New max bookings

        when ( slotRepository.findById ( TEST_SLOT_ID ) ).thenReturn ( Optional.of ( slot ) );
        when ( slotRepository.save ( slot ) ).thenReturn ( slot );
        when ( slotMapper.toResponse ( slot ) ).thenReturn ( slotResponse );

        // Act
        AppointmentSlotResponse result = slotService.updateSlot ( TEST_SLOT_ID, updateRequest );

        // Assert
        assertNotNull ( result );
        assertEquals ( LocalTime.of ( 10, 0 ), slot.getStartTime ( ), "Start time should be updated" );
        assertEquals ( 10, slot.getMaxBookings ( ), "Max bookings should be updated" );
        // End time should remain unchanged as it was not in the update request
        assertEquals ( TEST_END_TIME, slot.getEndTime ( ), "End time should be preserved" );

        verify ( slotRepository, times ( 1 ) ).findById ( TEST_SLOT_ID );
        verify ( slotRepository, times ( 1 ) ).save ( slot );
        verify ( slotMapper, times ( 1 ) ).toResponse ( slot );
    }

    @Test
    @DisplayName("Update non-existent slot - should throw Exception")
    void updateSlot_WithInvalidId_ShouldThrowException() {
        // Arrange
        Integer invalidId = 999;
        when ( slotRepository.findById ( invalidId ) ).thenReturn ( Optional.empty ( ) );

        // Act & Assert
        assertThrows (
                RuntimeException.class, // Your current code throws the default exception from orElseThrow()
                () -> slotService.updateSlot ( invalidId, slotRequest )
        );

        verify ( slotRepository, times ( 1 ) ).findById ( invalidId );
        verify ( slotRepository, never ( ) ).save ( any ( ) );
        verify ( slotMapper, never ( ) ).toResponse ( any ( ) );
    }

    // ------------------------------------------------------------------------------------
    // TEST CASES - DELETE
    // ------------------------------------------------------------------------------------

    @Test
    @DisplayName("Delete slot by ID - should call repository delete")
    void deleteSlot_WithValidId_ShouldCallDelete() {
        // Arrange - No need to stub deleteById, it's a void method

        // Act
        slotService.deleteSlot ( TEST_SLOT_ID );

        // Assert - Verify the interaction
        verify ( slotRepository, times ( 1 ) ).deleteById ( TEST_SLOT_ID );
    }

    // ------------------------------------------------------------------------------------
    // TEST CASES - AVAILABILITY & BOOKINGS
    // ------------------------------------------------------------------------------------

    @Test
    @DisplayName("Get available slots by time range - should return available slots")
    void getAvailableSlots_ByTimeRange_ShouldReturnAvailableSlots() {
        // Arrange
        LocalTime startTime = LocalTime.of ( 8, 0 );
        LocalTime endTime = LocalTime.of ( 12, 0 );

        when ( slotRepository.findAvailableSlotsBetween ( startTime, endTime ) ).thenReturn ( List.of ( slot ) );
        when ( slotMapper.toResponse ( slot ) ).thenReturn ( slotResponse );

        // Act
        List<AppointmentSlotResponse> result = slotService.getAvailableSlots ( startTime, endTime );

        // Assert
        assertNotNull ( result );
        assertEquals ( 1, result.size ( ) );
        assertEquals ( TEST_SLOT_ID, result.get ( 0 ).getId ( ) );

        verify ( slotRepository, times ( 1 ) ).findAvailableSlotsBetween ( startTime, endTime );
        verify ( slotMapper, times ( 1 ) ).toResponse ( slot );
    }

    @Test
    @DisplayName("Get available slots by garage ID - should return available slots")
    void getAvailableSlots_ByGarageId_ShouldReturnAvailableSlots() {
        // Arrange
        when ( slotRepository.findAvailableSlotsByGarageId ( TEST_GARAGE_ID ) ).thenReturn ( List.of ( slot ) );
        when ( slotMapper.toResponse ( slot ) ).thenReturn ( slotResponse );

        // Act
        List<AppointmentSlotResponse> result = slotService.getAvailableSlots ( TEST_GARAGE_ID );

        // Assert
        assertNotNull ( result );
        assertEquals ( 1, result.size ( ) );
        assertEquals ( TEST_SLOT_ID, result.get ( 0 ).getId ( ) );

        verify ( slotRepository, times ( 1 ) ).findAvailableSlotsByGarageId ( TEST_GARAGE_ID );
        verify ( slotMapper, times ( 1 ) ).toResponse ( slot );
    }

    @Test
    @DisplayName("Increment bookings on available slot - should update current bookings")
    void incrementSlotBookings_WithAvailableSlot_ShouldIncrementBookings() {
        // Arrange
        slot.setCurrentBookings ( 2 ); // Not yet full
        when ( slotRepository.findById ( TEST_SLOT_ID ) ).thenReturn ( Optional.of ( slot ) );
        when ( slotRepository.save ( slot ) ).thenReturn ( slot );

        // Act
        slotService.incrementSlotBookings ( TEST_SLOT_ID );

        // Assert
        assertEquals ( 3, slot.getCurrentBookings ( ), "Current bookings should be incremented by 1" );

        verify ( slotRepository, times ( 1 ) ).findById ( TEST_SLOT_ID );
        verify ( slotRepository, times ( 1 ) ).save ( slot );
    }

    @Test
    @DisplayName("Increment bookings on full slot - should throw IllegalStateException")
    void incrementSlotBookings_WithFullSlot_ShouldThrowException() {
        // Arrange
        slot.setCurrentBookings ( slot.getMaxBookings ( ) ); // Make it full
        when ( slotRepository.findById ( TEST_SLOT_ID ) ).thenReturn ( Optional.of ( slot ) );

        // Act & Assert
        IllegalStateException exception = assertThrows (
                IllegalStateException.class,
                () -> slotService.incrementSlotBookings ( TEST_SLOT_ID )
        );

        assertEquals ( "Slot is fully booked", exception.getMessage ( ) );
        assertEquals ( TEST_MAX_BOOKINGS, slot.getCurrentBookings ( ), "Bookings should not change" );

        verify ( slotRepository, times ( 1 ) ).findById ( TEST_SLOT_ID );
        verify ( slotRepository, never ( ) ).save ( any ( ) );
    }

    @Test
    @DisplayName("Decrement bookings on slot with bookings - should decrement current bookings")
    void decrementSlotBookings_WithPositiveBookings_ShouldDecrementBookings() {
        // Arrange
        slot.setCurrentBookings ( 3 );
        when ( slotRepository.findById ( TEST_SLOT_ID ) ).thenReturn ( Optional.of ( slot ) );
        when ( slotRepository.save ( slot ) ).thenReturn ( slot );

        // Act
        slotService.decrementSlotBookings ( TEST_SLOT_ID );

        // Assert
        assertEquals ( 2, slot.getCurrentBookings ( ), "Current bookings should be decremented by 1" );

        verify ( slotRepository, times ( 1 ) ).findById ( TEST_SLOT_ID );
        verify ( slotRepository, times ( 1 ) ).save ( slot );
    }

    @Test
    @DisplayName("Decrement bookings on slot with zero bookings - should not change bookings")
    void decrementSlotBookings_WithZeroBookings_ShouldNotChangeBookings() {
        // Arrange
        slot.setCurrentBookings ( 0 );
        when ( slotRepository.findById ( TEST_SLOT_ID ) ).thenReturn ( Optional.of ( slot ) );

        // Act
        slotService.decrementSlotBookings ( TEST_SLOT_ID );

        // Assert
        assertEquals ( 0, slot.getCurrentBookings ( ), "Current bookings should remain zero" );

        verify ( slotRepository, times ( 1 ) ).findById ( TEST_SLOT_ID );
        verify ( slotRepository, never ( ) ).save ( any ( ) ); // Should not save if bookings are already 0
    }

    @Test
    @DisplayName("Find available slot by garage ID and time - should return slot")
    void findAvailableSlotByGarageIdAndTime_WithValidData_ShouldReturnSlot() {
        // Arrange
        LocalTime searchTime = LocalTime.of ( 9, 30 ); // Within the slot's time range
        when ( slotRepository.findByGarageIdAndStartTime ( TEST_GARAGE_ID, TEST_DATE, searchTime ) )
                .thenReturn ( Optional.of ( slot ) );

        // Act
        AppointmentSlot result = slotService.findAvailableSlotByGarageIdAndTime ( TEST_GARAGE_ID, TEST_DATE, searchTime );

        // Assert
        assertNotNull ( result );
        assertEquals ( TEST_SLOT_ID, result.getId ( ) );

        verify ( slotRepository, times ( 1 ) )
                .findByGarageIdAndStartTime ( TEST_GARAGE_ID, TEST_DATE, searchTime );
    }

    @Test
    @DisplayName("Find available slot by non-existent criteria - should throw ResourceNotFoundException")
    void findAvailableSlotByGarageIdAndTime_WithInvalidData_ShouldThrowException() {
        // Arrange
        LocalDate invalidDate = LocalDate.now ( ).minusDays ( 1 ); // Past date
        LocalTime invalidTime = LocalTime.of ( 23, 0 ); // Late time

        when ( slotRepository.findByGarageIdAndStartTime ( TEST_GARAGE_ID, invalidDate, invalidTime ) )
                .thenReturn ( Optional.empty ( ) );

        // Act & Assert
        ResourceNotFoundException exception = assertThrows (
                ResourceNotFoundException.class,
                () -> slotService.findAvailableSlotByGarageIdAndTime ( TEST_GARAGE_ID, invalidDate, invalidTime )
        );

        assertTrue ( exception.getMessage ( ).contains ( "slot not found" ) );

        verify ( slotRepository, times ( 1 ) )
                .findByGarageIdAndStartTime ( TEST_GARAGE_ID, invalidDate, invalidTime );
    }

    // Note: Testing @Scheduled method markPastSlotsAsUnavailable is more complex
    // as it involves time and is usually tested with integration tests.
    // Similarly, getMonthlyAvailability involves complex data grouping and is best
    // tested with an integration test or a @DataJpaTest for the repository.
}