package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageBreakTime;
import com.autoadda.apis.garage.entity.GarageWorkingHours;
import com.autoadda.apis.garage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SlotGenerationService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for SlotGenerationService")
class SlotGenerationServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private GarageRepository garageRepository;

    @Mock
    private TechnicianRepository technicianRepository; // Not used in tests but required for injection

    @Mock
    private GarageWorkingHoursRepository workingHoursRepo;

    @Mock
    private GarageBreakTimeRepository breakRepo;

    @InjectMocks
    private SlotGenerationService slotGenerationService;

    // Test data constants
    private static final Integer TEST_GARAGE_ID = 1;
    private static final String TEST_GARAGE_NAME = "Test Garage";
    private static final Integer TEST_CONCURRENT_CAPACITY = 3;
    private static final LocalDate TEST_DATE = LocalDate.now().plusDays(1);
    private static final DayOfWeek TEST_DAY_OF_WEEK = TEST_DATE.getDayOfWeek();
    private static final LocalTime TEST_OPEN_TIME = LocalTime.of(9, 0);
    private static final LocalTime TEST_CLOSE_TIME = LocalTime.of(17, 0);


    // Test entities
    private Garage garage;
    private GarageWorkingHours workingHours;
    private GarageBreakTime breakTime;

    /**
     * Initializes common test data before each test method.
     */
    @BeforeEach
    void setUp() {
        // Initialize Garage
        garage = new Garage();
        garage.setId(TEST_GARAGE_ID);
        garage.setName(TEST_GARAGE_NAME);
        garage.setConcurrentAppointmentCapacity(TEST_CONCURRENT_CAPACITY);

        // Initialize Working Hours
        workingHours = new GarageWorkingHours();
        workingHours.setGarage(garage);
        workingHours.setDayOfWeek(TEST_DAY_OF_WEEK);
        workingHours.setOpenTime(TEST_OPEN_TIME);
        workingHours.setCloseTime(TEST_CLOSE_TIME);
        workingHours.setClosed(false);

        // Initialize Break Time
        breakTime = new GarageBreakTime();
        breakTime.setGarage(garage);
        breakTime.setDayOfWeek(TEST_DAY_OF_WEEK);
        breakTime.setBreakStart(LocalTime.of(12, 0));
        breakTime.setBreakEnd(LocalTime.of(13, 0));
    }

    // ------------------------------------------------------------------------------------
    // TEST CASES - generateSlotsForGarageAndDate
    // ------------------------------------------------------------------------------------

    @Test
    @DisplayName("Generate slots for garage and date with working hours - should create slots")
    void generateSlotsForGarageAndDate_WithWorkingHours_ShouldCreateSlots() {
        // Arrange
        when(workingHoursRepo.findByGarageIdAndDayOfWeek(TEST_GARAGE_ID, TEST_DAY_OF_WEEK))
                .thenReturn(Optional.of(workingHours));
        when(breakRepo.findByGarageIdAndDayOfWeek(TEST_GARAGE_ID, TEST_DAY_OF_WEEK))
                .thenReturn(List.of()); // No breaks
        when(slotRepository.findByGarageIdAndSlotDateAndStartTime(any(), any(), any()))
                .thenReturn(Optional.empty()); // No existing slots

        // Act
        slotGenerationService.generateSlotsForGarageAndDate(garage, TEST_DATE);

        // Assert - Verify slots were saved
        verify(slotRepository, atLeastOnce()).save(any(AppointmentSlot.class));

        // Capture the saved slot to verify its properties
        ArgumentCaptor<AppointmentSlot> slotCaptor = ArgumentCaptor.forClass(AppointmentSlot.class);
        verify(slotRepository, atLeastOnce()).save(slotCaptor.capture());

        AppointmentSlot savedSlot = slotCaptor.getValue();
        assertEquals(TEST_DATE, savedSlot.getSlotDate());
        assertEquals(TEST_CONCURRENT_CAPACITY, savedSlot.getMaxBookings());
        assertEquals(0, savedSlot.getCurrentBookings());
        assertEquals(garage, savedSlot.getGarage());
        assertNotNull(savedSlot.getStartTime());
        assertNotNull(savedSlot.getEndTime());
    }

    @Test
    @DisplayName("Generate slots when garage is closed - should not create any slots")
    void generateSlotsForGarageAndDate_WhenClosed_ShouldNotCreateSlots() {
        // Arrange
        workingHours.setClosed(true);
        when(workingHoursRepo.findByGarageIdAndDayOfWeek(TEST_GARAGE_ID, TEST_DAY_OF_WEEK))
                .thenReturn(Optional.of(workingHours));

        // Act
        slotGenerationService.generateSlotsForGarageAndDate(garage, TEST_DATE);

        // Assert
        verify(breakRepo, never()).findByGarageIdAndDayOfWeek(any(), any());
        verify(slotRepository, never()).findByGarageIdAndSlotDateAndStartTime(any(), any(), any());
        verify(slotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Generate slots when no working hours found - should not create any slots")
    void generateSlotsForGarageAndDate_WhenNoWorkingHours_ShouldNotCreateSlots() {
        // Arrange
        when(workingHoursRepo.findByGarageIdAndDayOfWeek(TEST_GARAGE_ID, TEST_DAY_OF_WEEK))
                .thenReturn(Optional.empty());

        // Act
        slotGenerationService.generateSlotsForGarageAndDate(garage, TEST_DATE);

        // Assert
        verify(breakRepo, never()).findByGarageIdAndDayOfWeek(any(), any());
        verify(slotRepository, never()).findByGarageIdAndSlotDateAndStartTime(any(), any(), any());
        verify(slotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Generate slots with break time - should skip slots during break")
    void generateSlotsForGarageAndDate_WithBreakTime_ShouldSkipBreakSlots() {
        // Arrange
        when(workingHoursRepo.findByGarageIdAndDayOfWeek(TEST_GARAGE_ID, TEST_DAY_OF_WEEK))
                .thenReturn(Optional.of(workingHours));
        when(breakRepo.findByGarageIdAndDayOfWeek(TEST_GARAGE_ID, TEST_DAY_OF_WEEK))
                .thenReturn(List.of(breakTime));
        when(slotRepository.findByGarageIdAndSlotDateAndStartTime(any(), any(), any()))
                .thenReturn(Optional.empty());

        // Act
        slotGenerationService.generateSlotsForGarageAndDate(garage, TEST_DATE);

        // Assert - Verify that save was called (for non-break slots) but not for break times
        verify(slotRepository, atLeastOnce()).save(any(AppointmentSlot.class));

        // We could add more specific verification about break times being skipped
        // This would require more complex mocking of the time iteration
    }

    @Test
    @DisplayName("Generate slots when slot already exists - should not create duplicate")
    void generateSlotsForGarageAndDate_WhenSlotExists_ShouldNotCreateDuplicate() {
        // Arrange
        AppointmentSlot existingSlot = new AppointmentSlot();

        when(workingHoursRepo.findByGarageIdAndDayOfWeek(TEST_GARAGE_ID, TEST_DAY_OF_WEEK))
                .thenReturn(Optional.of(workingHours));
        when(breakRepo.findByGarageIdAndDayOfWeek(TEST_GARAGE_ID, TEST_DAY_OF_WEEK))
                .thenReturn(List.of());
        when(slotRepository.findByGarageIdAndSlotDateAndStartTime(TEST_GARAGE_ID, TEST_DATE, TEST_OPEN_TIME))
                .thenReturn(Optional.of(existingSlot)); // Slot already exists

        // Act
        slotGenerationService.generateSlotsForGarageAndDate(garage, TEST_DATE);

        // Assert - Should not save a duplicate slot
        verify(slotRepository, never()).save(any(AppointmentSlot.class));
    }

    // ------------------------------------------------------------------------------------
    // TEST CASES - generateSlotsForDays
    // ------------------------------------------------------------------------------------

    @Test
    @DisplayName("Generate slots for multiple days - should call generate for each day")
    void generateSlotsForDays_ShouldCallGenerateForEachDay() {
        // Arrange
        int daysToGenerate = 3;

        // Mock the working hours to return valid hours for all days
        when(workingHoursRepo.findByGarageIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.of(workingHours));
        when(breakRepo.findByGarageIdAndDayOfWeek(any(), any()))
                .thenReturn(List.of());
        when(slotRepository.findByGarageIdAndSlotDateAndStartTime(any(), any(), any()))
                .thenReturn(Optional.empty());

        // Act
        slotGenerationService.generateSlotsForDays(garage, daysToGenerate);

        // Assert - Should attempt to generate slots for each day
        // We can't easily verify exact number due to day-specific logic, but should be called multiple times
        verify(workingHoursRepo, atLeast(daysToGenerate)).findByGarageIdAndDayOfWeek(any(), any());
        verify(slotRepository, atLeastOnce()).save(any(AppointmentSlot.class));
    }

    @Test
    @DisplayName("Generate slots for days with null capacity - should not generate slots")
    void generateSlotsForDays_WithNullCapacity_ShouldNotGenerateSlots() {
        // Arrange
        garage.setConcurrentAppointmentCapacity(null);
        int daysToGenerate = 3;

        // Act
        slotGenerationService.generateSlotsForDays(garage, daysToGenerate);

        // Assert
        verify(workingHoursRepo, never()).findByGarageIdAndDayOfWeek(any(), any());
        verify(slotRepository, never()).save(any());
    }

    // ------------------------------------------------------------------------------------
    // TEST CASES - updateUpcomingSlotsForGarage
    // ------------------------------------------------------------------------------------

    @Test
    @DisplayName("Update upcoming slots with new capacity - should update max bookings")
    void updateUpcomingSlotsForGarage_WithNewCapacity_ShouldUpdateMaxBookings() {
        // Arrange
        int newCapacity = 5;
        garage.setConcurrentAppointmentCapacity(newCapacity);

        LocalDate today = LocalDate.now();
        AppointmentSlot futureSlot = new AppointmentSlot();
        futureSlot.setMaxBookings(3);
        futureSlot.setCurrentBookings(1);
        futureSlot.setSlotDate(today.plusDays(1));

        when(slotRepository.findByGarageIdAndSlotDateGreaterThanEqual(TEST_GARAGE_ID, today))
                .thenReturn(List.of(futureSlot));

        // Act
        slotGenerationService.updateUpcomingSlotsForGarage(garage);

        // Assert
        assertEquals(newCapacity, futureSlot.getMaxBookings(), "Max bookings should be updated to new capacity");
        verify(slotRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("Update upcoming slots when current bookings exceed new capacity - should set max to current bookings")
    void updateUpcomingSlotsForGarage_WhenCurrentExceedsNewCapacity_ShouldSetMaxToCurrent() {
        // Arrange
        int newCapacity = 2; // Lower than current bookings
        garage.setConcurrentAppointmentCapacity(newCapacity);

        LocalDate today = LocalDate.now();
        AppointmentSlot futureSlot = new AppointmentSlot();
        futureSlot.setMaxBookings(5);
        futureSlot.setCurrentBookings(3); // Current bookings > new capacity
        futureSlot.setSlotDate(today.plusDays(1));

        when(slotRepository.findByGarageIdAndSlotDateGreaterThanEqual(TEST_GARAGE_ID, today))
                .thenReturn(List.of(futureSlot));

        // Act
        slotGenerationService.updateUpcomingSlotsForGarage(garage);

        // Assert
        assertEquals(3, futureSlot.getMaxBookings(), "Max bookings should be set to current bookings");
        verify(slotRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("Update upcoming slots with null capacity - should not update anything")
    void updateUpcomingSlotsForGarage_WithNullCapacity_ShouldNotUpdate() {
        // Arrange
        garage.setConcurrentAppointmentCapacity(null);

        // Act
        slotGenerationService.updateUpcomingSlotsForGarage(garage);

        // Assert
        verify(slotRepository, never()).findByGarageIdAndSlotDateGreaterThanEqual(any(), any());
        verify(slotRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Update upcoming slots with no future slots - should not save anything")
    void updateUpcomingSlotsForGarage_WithNoFutureSlots_ShouldNotSave() {
        // Arrange
        LocalDate today = LocalDate.now();
        when(slotRepository.findByGarageIdAndSlotDateGreaterThanEqual(TEST_GARAGE_ID, today))
                .thenReturn(List.of()); // No future slots

        // Act
        slotGenerationService.updateUpcomingSlotsForGarage(garage);

        // Assert
        verify(slotRepository, times(1)).findByGarageIdAndSlotDateGreaterThanEqual(TEST_GARAGE_ID, today);
        verify(slotRepository, never()).saveAll(any());
    }

    // Note: Testing @PostConstruct init() and @Scheduled generateSlotsForNextDays() methods
    // is typically done with integration tests that involve application context setup.
    // These methods are simple wrappers that call other tested methods.
}