package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.entity.TechnicianLanguage;
import com.autoadda.apis.garage.entity.TechnicianProfile;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.TechnicianProfileMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.EmploymentRequest;
import com.autoadda.apis.garage.request.LanguageRequest;
import com.autoadda.apis.garage.request.PersonalDetailsRequest;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.util.CareerBreakReason;
import com.autoadda.apis.garage.util.EmploymentEntryType;
import com.autoadda.apis.garage.util.LanguageProficiency;
import com.autoadda.apis.garage.util.ProfileCompletionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianProfileServiceTest {

    @Mock private TechnicianRepository technicianRepository;
    @Mock private TechnicianProfileRepository profileRepository;
    @Mock private TechnicianLanguageRepository languageRepository;
    @Mock private TechnicianProfileCompletionCalculator completionCalculator;
    @Mock private TechnicianEmploymentRepository employmentRepository;
    @Spy private TechnicianProfileMapper profileMapper = new TechnicianProfileMapper();
    @Spy private EmploymentGapDetector employmentGapDetector = new EmploymentGapDetector(6);

    @InjectMocks
    private TechnicianProfileService service;

    private Technician technician;
    private ProfileCompletionResponse completion;

    @BeforeEach
    void setUp() {
        technician = Technician.builder()
                .id(1)
                .firstName("John")
                .mobileNumber("+971500000000")
                .isDeleted(false)
                .build();
        completion = ProfileCompletionResponse.builder().percent(11).status(ProfileCompletionStatus.IN_PROGRESS).build();
        lenient().when(technicianRepository.findById(1)).thenReturn(Optional.of(technician));
        lenient().when(completionCalculator.recalculate(technician)).thenReturn(completion);
    }

    @Test
    void savePersonalDetails_emptyRequest_keepsExistingValues() {
        when(profileRepository.findByTechnicianId(1)).thenReturn(Optional.empty());

        ProfileStepSaveResponse<PersonalDetailsResponse> result =
                service.savePersonalDetails(1, new PersonalDetailsRequest());

        assertEquals("John", result.getData().getFirstName());
        assertEquals("+971500000000", result.getData().getMobileNumber());
        assertSame(completion, result.getCompletion());
        verify(profileRepository).save(any(TechnicianProfile.class));
        verify(technicianRepository).saveAndFlush(technician);
    }

    @Test
    void savePersonalDetails_appliesOnlyNonNullFields() {
        TechnicianProfile existing = TechnicianProfile.builder().technician(technician).nationality("Indian").build();
        when(profileRepository.findByTechnicianId(1)).thenReturn(Optional.of(existing));

        PersonalDetailsRequest request = PersonalDetailsRequest.builder().lastName("Doe").country("UAE").build();
        ProfileStepSaveResponse<PersonalDetailsResponse> result = service.savePersonalDetails(1, request);

        assertEquals("John", result.getData().getFirstName());
        assertEquals("Doe", result.getData().getLastName());
        assertEquals("Indian", result.getData().getNationality());
        assertEquals("UAE", result.getData().getCountry());
    }

    @Test
    void savePersonalDetails_mobileTakenByAnotherTechnician_throwsConflict() {
        when(profileRepository.findByTechnicianId(1)).thenReturn(Optional.empty());
        Technician other = Technician.builder().id(2).mobileNumber("+971522222222").build();
        when(technicianRepository.findByMobileNumber("+971522222222")).thenReturn(Optional.of(other));

        PersonalDetailsRequest request = PersonalDetailsRequest.builder().mobileNumber("+971522222222").build();

        assertThrows(AlreadyExistsException.class, () -> service.savePersonalDetails(1, request));
        verify(technicianRepository, never()).saveAndFlush(any());
    }

    @Test
    void saveLanguages_createsUpdatesAndDeletes() {
        TechnicianLanguage arabic = language(10, "Arabic");
        TechnicianLanguage hindi = language(11, "Hindi");
        when(languageRepository.findByTechnicianIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(arabic, hindi));
        when(languageRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LanguageRequest> requests = List.of(
                LanguageRequest.builder().language("English").build(),
                LanguageRequest.builder().id(10).language("Arabic").proficiency(LanguageProficiency.FLUENT).build());

        ProfileStepSaveResponse<List<LanguageResponse>> result = service.saveLanguages(1, requests);

        List<LanguageResponse> saved = result.getData();
        assertEquals(2, saved.size());
        assertEquals("English", saved.get(0).getLanguage());
        assertEquals(0, saved.get(0).getDisplayOrder());
        assertEquals(10, saved.get(1).getId());
        assertEquals(LanguageProficiency.FLUENT, saved.get(1).getProficiency());
        assertEquals(1, saved.get(1).getDisplayOrder());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<TechnicianLanguage>> deleted = ArgumentCaptor.forClass(Iterable.class);
        verify(languageRepository).deleteAll(deleted.capture());
        assertEquals(List.of(hindi), toList(deleted.getValue()));
        assertSame(completion, result.getCompletion());
    }

    @Test
    void saveLanguages_emptyItem_isSavedWithoutValidationErrors() {
        when(languageRepository.findByTechnicianIdOrderByDisplayOrderAsc(1)).thenReturn(Collections.emptyList());
        when(languageRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        ProfileStepSaveResponse<List<LanguageResponse>> result = service.saveLanguages(1, List.of(new LanguageRequest()));

        assertEquals(1, result.getData().size());
        assertNull(result.getData().get(0).getLanguage());
    }

    @Test
    void saveLanguages_emptyList_clearsStep() {
        TechnicianLanguage arabic = language(10, "Arabic");
        when(languageRepository.findByTechnicianIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(arabic));
        when(languageRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        ProfileStepSaveResponse<List<LanguageResponse>> result = service.saveLanguages(1, List.of());

        assertTrue(result.getData().isEmpty());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<TechnicianLanguage>> deleted = ArgumentCaptor.forClass(Iterable.class);
        verify(languageRepository).deleteAll(deleted.capture());
        assertEquals(List.of(arabic), toList(deleted.getValue()));
    }

    @Test
    void saveLanguages_nullList_leavesStepUnchanged() {
        TechnicianLanguage arabic = language(10, "Arabic");
        when(languageRepository.findByTechnicianIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(arabic));

        ProfileStepSaveResponse<List<LanguageResponse>> result = service.saveLanguages(1, null);

        assertEquals(1, result.getData().size());
        verify(languageRepository, never()).deleteAll(any());
        verify(languageRepository, never()).saveAll(any());
    }

    @Test
    void saveLanguages_itemOfAnotherTechnician_isRejected() {
        when(languageRepository.findByTechnicianIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(language(10, "Arabic")));

        List<LanguageRequest> requests = List.of(LanguageRequest.builder().id(99).language("French").build());

        assertThrows(IllegalArgumentException.class, () -> service.saveLanguages(1, requests));
        verify(languageRepository, never()).deleteAll(any());
        verify(languageRepository, never()).saveAll(any());
    }

    @Test
    void saveEmployment_careerBreakExplainsGap() {
        when(employmentRepository.findByTechnicianIdOrderByDisplayOrderAsc(1)).thenReturn(Collections.emptyList());
        when(employmentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        EmploymentRequest oldJob = EmploymentRequest.builder().employerName("Al Futtaim")
                .startDate(LocalDate.of(2015, 1, 1)).endDate(LocalDate.of(2019, 12, 31)).build();
        EmploymentRequest currentJob = EmploymentRequest.builder().employerName("AutoAdda Garage")
                .startDate(LocalDate.of(2021, 1, 1)).isCurrent(true).build();
        EmploymentRequest careerBreak = EmploymentRequest.builder().entryType(EmploymentEntryType.CAREER_BREAK)
                .breakReason(CareerBreakReason.VISA_PROCESSING).breakDescription("Moved to UAE, waiting for work visa")
                .startDate(LocalDate.of(2020, 1, 1)).endDate(LocalDate.of(2020, 12, 31)).build();

        // Without the break the year is an unexplained gap
        WorkHistoryResponse withoutBreak = service.saveEmployment(1, List.of(oldJob, currentJob)).getData();
        assertEquals(1, withoutBreak.getUnexplainedGaps().size());
        assertEquals(12, withoutBreak.getUnexplainedGaps().get(0).getMonths());
        assertEquals(EmploymentEntryType.EMPLOYMENT, withoutBreak.getEntries().get(0).getEntryType());

        // With the break the gap is explained
        WorkHistoryResponse withBreak = service.saveEmployment(1, List.of(oldJob, careerBreak, currentJob)).getData();
        assertTrue(withBreak.getUnexplainedGaps().isEmpty());
        EmploymentResponse savedBreak = withBreak.getEntries().get(1);
        assertEquals(EmploymentEntryType.CAREER_BREAK, savedBreak.getEntryType());
        assertEquals(CareerBreakReason.VISA_PROCESSING, savedBreak.getBreakReason());
        assertEquals("Moved to UAE, waiting for work visa", savedBreak.getBreakDescription());
        assertEquals(6, withBreak.getGapThresholdMonths());
    }

    @Test
    void deletedTechnician_isNotFound() {
        technician.setIsDeleted(true);

        assertThrows(ResourceNotFoundException.class, () -> service.saveLanguages(1, List.of()));
    }

    private TechnicianLanguage language(Integer id, String name) {
        TechnicianLanguage language = new TechnicianLanguage();
        language.setId(id);
        language.setTechnician(technician);
        language.setLanguage(name);
        return language;
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
