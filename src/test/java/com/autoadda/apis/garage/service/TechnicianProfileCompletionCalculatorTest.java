package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.entity.TechnicianProfile;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.response.ProfileCompletionResponse;
import com.autoadda.apis.garage.response.ProfileStepCompletionResponse;
import com.autoadda.apis.garage.util.ProfileCompletionStatus;
import com.autoadda.apis.garage.util.ProfileStep;
import com.autoadda.apis.garage.util.ProfileStepStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicianProfileCompletionCalculatorTest {

    @Mock private TechnicianProfileRepository profileRepository;
    @Mock private TechnicianQualificationRepository qualificationRepository;
    @Mock private TechnicianCertificationRepository certificationRepository;
    @Mock private TechnicianSkillRepository skillRepository;
    @Mock private TechnicianEmploymentRepository employmentRepository;
    @Mock private TechnicianLanguageRepository languageRepository;
    @Mock private TechnicianDocumentRepository documentRepository;
    @Mock private TechnicianReferenceRepository referenceRepository;
    @Mock private TechnicianSocialLinkRepository socialLinkRepository;

    @InjectMocks
    private TechnicianProfileCompletionCalculator calculator;

    private Technician technician;

    @BeforeEach
    void setUp() {
        technician = Technician.builder().id(1).build();
    }

    @Test
    void emptyProfile_isNotStarted() {
        ProfileCompletionResponse completion = calculator.calculate(technician);

        assertEquals(0, completion.getPercent());
        assertEquals(ProfileCompletionStatus.NOT_STARTED, completion.getStatus());
        assertEquals(ProfileStep.PERSONAL, completion.getNextIncompleteStep());
        assertEquals(9, completion.getSteps().size());
        assertTrue(completion.getSteps().stream().allMatch(s -> s.getStatus() == ProfileStepStatus.NOT_STARTED));
    }

    @Test
    void oneListStep_isInProgress() {
        when(languageRepository.countByTechnicianId(1)).thenReturn(1L);

        ProfileCompletionResponse completion = calculator.calculate(technician);

        assertEquals(11, completion.getPercent());
        assertEquals(ProfileCompletionStatus.IN_PROGRESS, completion.getStatus());
        assertEquals(ProfileStepStatus.COMPLETED, step(completion, ProfileStep.LANGUAGES).getStatus());
        assertEquals(100, step(completion, ProfileStep.LANGUAGES).getPercent());
    }

    @Test
    void partialPersonalStep_addsFractionalWeight() {
        technician.setFirstName("John");
        technician.setMobileNumber("+971500000000");

        ProfileCompletionResponse completion = calculator.calculate(technician);

        ProfileStepCompletionResponse personal = step(completion, ProfileStep.PERSONAL);
        assertEquals(ProfileStepStatus.PARTIAL, personal.getStatus());
        assertEquals(29, personal.getPercent());          // 2 of 7 core fields
        assertEquals(3, completion.getPercent());         // (2/7) / 9 steps
        assertEquals(ProfileStep.PERSONAL, completion.getNextIncompleteStep());
    }

    @Test
    void allSteps_isCompleted() {
        technician.setFirstName("John");
        technician.setLastName("Doe");
        technician.setMobileNumber("+971500000000");
        technician.setCity("Dubai");
        TechnicianProfile profile = TechnicianProfile.builder()
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality("Indian")
                .professionalSummary("Engine specialist")
                .emergencyContactName("Jane")
                .emergencyContactPhone("+971511111111")
                .build();
        when(profileRepository.findByTechnicianId(1)).thenReturn(Optional.of(profile));
        when(qualificationRepository.countByTechnicianId(1)).thenReturn(1L);
        when(certificationRepository.countByTechnicianId(1)).thenReturn(1L);
        when(skillRepository.countByTechnicianId(1)).thenReturn(1L);
        when(employmentRepository.countJobsByTechnicianId(1)).thenReturn(1L);
        when(languageRepository.countByTechnicianId(1)).thenReturn(1L);
        when(documentRepository.countByTechnicianId(1)).thenReturn(1L);
        when(referenceRepository.countByTechnicianId(1)).thenReturn(1L);
        when(socialLinkRepository.countByTechnicianId(1)).thenReturn(2L);

        ProfileCompletionResponse completion = calculator.calculate(technician);

        assertEquals(100, completion.getPercent());
        assertEquals(ProfileCompletionStatus.COMPLETED, completion.getStatus());
        assertNull(completion.getNextIncompleteStep());
    }

    @Test
    void recalculate_storesValuesAndStampsUpdatedAt() {
        when(documentRepository.countByTechnicianId(1)).thenReturn(1L);

        ProfileCompletionResponse completion = calculator.recalculate(technician);

        assertEquals(11, technician.getProfileCompletionPercent());
        assertEquals(ProfileCompletionStatus.IN_PROGRESS, technician.getProfileCompletionStatus());
        assertNotNull(technician.getProfileUpdatedAt());
        assertEquals(technician.getProfileUpdatedAt(), completion.getUpdatedAt());
    }

    @Test
    void refresh_doesNotTouchUpdatedAt() {
        LocalDateTime lastUpdate = LocalDateTime.of(2026, 1, 1, 10, 0);
        technician.setProfileUpdatedAt(lastUpdate);
        technician.setProfileCompletionPercent(50);

        calculator.refresh(technician);

        assertEquals(0, technician.getProfileCompletionPercent());
        assertEquals(lastUpdate, technician.getProfileUpdatedAt());
    }

    @Test
    void newTechnicianWithoutId_doesNotQueryLists() {
        Technician unsaved = Technician.builder().firstName("John").build();

        ProfileCompletionResponse completion = calculator.calculate(unsaved);

        assertEquals(2, completion.getPercent());         // (1/7) / 9 steps
    }

    private ProfileStepCompletionResponse step(ProfileCompletionResponse completion, ProfileStep step) {
        return completion.getSteps().stream().filter(s -> s.getStep() == step).findFirst().orElseThrow();
    }
}
