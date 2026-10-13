package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.AvailableServiceMapper;
import com.autoadda.apis.garage.mapper.TechnicianExpertiseMapper;
import com.autoadda.apis.garage.mapper.TechnicianProfileMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.*;
import com.autoadda.apis.garage.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.autoadda.apis.garage.service.TechnicianService.TECHNICIAN_NOT_FOUND_WITH_ID;

/**
 * Step-wise technician CV profile: personal details, qualifications, certifications, skills,
 * employment, languages, documents, references/emergency contact and social links.
 * <p>
 * Nothing is mandatory. Personal/emergency-contact fields left null are unchanged.
 * List steps replace the whole list: items with an id are updated, items without an id are created,
 * stored items missing from the request are deleted; a null list leaves the step unchanged.
 * Every save recalculates the technician's profile completion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianProfileService {

    private final TechnicianRepository technicianRepository;
    private final TechnicianProfileRepository profileRepository;
    private final TechnicianQualificationRepository qualificationRepository;
    private final TechnicianCertificationRepository certificationRepository;
    private final TechnicianSkillRepository skillRepository;
    private final TechnicianEmploymentRepository employmentRepository;
    private final TechnicianLanguageRepository languageRepository;
    private final TechnicianDocumentRepository documentRepository;
    private final TechnicianReferenceRepository referenceRepository;
    private final TechnicianSocialLinkRepository socialLinkRepository;
    private final MediaFileRepository mediaFileRepository;
    private final AvailableServiceRepository availableServiceRepository;
    private final TechnicianService technicianService;
    private final TechnicianProfileMapper profileMapper;
    private final AvailableServiceMapper availableServiceMapper;
    private final TechnicianExpertiseMapper technicianExpertiseMapper;
    private final TechnicianProfileCompletionCalculator completionCalculator;
    private final EmploymentGapDetector employmentGapDetector;

    // ---------- Read ----------

    @Transactional
    public TechnicianProfileResponse getProfile(Integer technicianId) {
        Technician technician = findTechnician(technicianId);
        TechnicianProfile profile = profileRepository.findByTechnicianId(technicianId).orElse(null);
        return TechnicianProfileResponse.builder()
                .technicianId(technicianId)
                .personal(profileMapper.toPersonalResponse(technician, profile))
                .qualifications(map(qualificationRepository.findByTechnicianIdOrderByDisplayOrderAsc(technicianId), profileMapper::toResponse))
                .certifications(map(certificationRepository.findByTechnicianIdOrderByDisplayOrderAsc(technicianId), profileMapper::toResponse))
                .skills(skillsResponse(technician))
                .employment(workHistoryResponse(employmentRepository.findByTechnicianIdOrderByDisplayOrderAsc(technicianId)))
                .languages(map(languageRepository.findByTechnicianIdOrderByDisplayOrderAsc(technicianId), profileMapper::toResponse))
                .documents(map(documentRepository.findByTechnicianIdOrderByDisplayOrderAsc(technicianId), profileMapper::toResponse))
                .references(referencesResponse(technicianId, profile))
                .socialLinks(map(socialLinkRepository.findByTechnicianIdOrderByDisplayOrderAsc(technicianId), profileMapper::toResponse))
                .completion(completionCalculator.refresh(technician))
                .build();
    }

    @Transactional
    public ProfileCompletionResponse getCompletion(Integer technicianId) {
        return completionCalculator.refresh(findTechnician(technicianId));
    }

    // ---------- Step 1: personal details ----------

    @Transactional
    public ProfileStepSaveResponse<PersonalDetailsResponse> savePersonalDetails(Integer technicianId, PersonalDetailsRequest request) {
        Technician technician = findTechnician(technicianId);
        TechnicianProfile profile = getOrCreateProfile(technician);
        if (request != null) {
            updateMobileNumber(technician, request.getMobileNumber());
            profileMapper.applyPersonalToTechnician(technician, request);
            profileMapper.applyPersonalToProfile(profile, request);
            profileRepository.save(profile);
        }
        ProfileCompletionResponse completion = saveWithCompletion(technician);
        return stepResponse(profileMapper.toPersonalResponse(technician, profile), completion);
    }

    // ---------- Steps 2, 3, 5, 6, 7, 9: list sections ----------

    @Transactional
    public ProfileStepSaveResponse<List<QualificationResponse>> saveQualifications(Integer technicianId, List<QualificationRequest> requests) {
        Technician technician = findTechnician(technicianId);
        List<TechnicianQualification> items = syncList(technician, qualificationRepository, requests, TechnicianQualification::new,
                (entity, request) -> profileMapper.apply(entity, request, resolveMediaFile(request.getCertificateFileId())));
        return stepResponse(map(items, profileMapper::toResponse), saveWithCompletion(technician));
    }

    @Transactional
    public ProfileStepSaveResponse<List<CertificationResponse>> saveCertifications(Integer technicianId, List<CertificationRequest> requests) {
        Technician technician = findTechnician(technicianId);
        List<TechnicianCertification> items = syncList(technician, certificationRepository, requests, TechnicianCertification::new,
                (entity, request) -> profileMapper.apply(entity, request, resolveMediaFile(request.getDocumentFileId())));
        return stepResponse(map(items, profileMapper::toResponse), saveWithCompletion(technician));
    }

    @Transactional
    public ProfileStepSaveResponse<WorkHistoryResponse> saveEmployment(Integer technicianId, List<EmploymentRequest> requests) {
        Technician technician = findTechnician(technicianId);
        List<TechnicianEmployment> items = syncList(technician, employmentRepository, requests, TechnicianEmployment::new, profileMapper::apply);
        return stepResponse(workHistoryResponse(items), saveWithCompletion(technician));
    }

    @Transactional
    public ProfileStepSaveResponse<List<LanguageResponse>> saveLanguages(Integer technicianId, List<LanguageRequest> requests) {
        Technician technician = findTechnician(technicianId);
        List<TechnicianLanguage> items = syncList(technician, languageRepository, requests, TechnicianLanguage::new, profileMapper::apply);
        return stepResponse(map(items, profileMapper::toResponse), saveWithCompletion(technician));
    }

    @Transactional
    public ProfileStepSaveResponse<List<DocumentResponse>> saveDocuments(Integer technicianId, List<DocumentRequest> requests) {
        Technician technician = findTechnician(technicianId);
        List<TechnicianDocument> items = syncList(technician, documentRepository, requests, TechnicianDocument::new,
                (entity, request) -> profileMapper.apply(entity, request, resolveMediaFile(request.getFileId())));
        return stepResponse(map(items, profileMapper::toResponse), saveWithCompletion(technician));
    }

    @Transactional
    public ProfileStepSaveResponse<List<SocialLinkResponse>> saveSocialLinks(Integer technicianId, List<SocialLinkRequest> requests) {
        Technician technician = findTechnician(technicianId);
        List<TechnicianSocialLink> items = syncList(technician, socialLinkRepository, requests, TechnicianSocialLink::new, profileMapper::apply);
        return stepResponse(map(items, profileMapper::toResponse), saveWithCompletion(technician));
    }

    // ---------- Step 4: technical skills ----------

    @Transactional
    public ProfileStepSaveResponse<TechnicianSkillsStepResponse> saveSkills(Integer technicianId, TechnicianSkillsStepRequest request) {
        Technician technician = findTechnician(technicianId);
        if (request != null) {
            if (request.getSkillSetIds() != null) {
                technician.getSkillSet().clear();
                technician.getSkillSet().addAll(availableServiceRepository.findAllById(request.getSkillSetIds()));
            }
            if (request.getExpertise() != null) {
                // Entries without a category or level cannot be stored, so they are skipped rather than rejected
                List<TechnicianExpertiseRequest> expertise = request.getExpertise().stream()
                        .filter(Objects::nonNull)
                        .filter(e -> e.getExpertiseCategoryId() != null && e.getExpertiseLevel() != null)
                        .toList();
                technicianService.replaceExpertise(technician, expertise);
            }
            syncList(technician, skillRepository, request.getSkills(), TechnicianSkill::new, profileMapper::apply);
        }
        ProfileCompletionResponse completion = saveWithCompletion(technician);
        return stepResponse(skillsResponse(technician), completion);
    }

    // ---------- Step 8: references & emergency contact ----------

    @Transactional
    public ProfileStepSaveResponse<ReferencesStepResponse> saveReferences(Integer technicianId, ReferencesStepRequest request) {
        Technician technician = findTechnician(technicianId);
        TechnicianProfile profile = getOrCreateProfile(technician);
        if (request != null) {
            profileMapper.applyEmergencyContact(profile, request);
            profileRepository.save(profile);
            syncList(technician, referenceRepository, request.getReferences(), TechnicianReference::new, profileMapper::apply);
        }
        ProfileCompletionResponse completion = saveWithCompletion(technician);
        return stepResponse(referencesResponse(technicianId, profile), completion);
    }

    // ---------- Helpers ----------

    private Technician findTechnician(Integer technicianId) {
        return technicianRepository.findById(technicianId)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException(TECHNICIAN_NOT_FOUND_WITH_ID + technicianId));
    }

    private TechnicianProfile getOrCreateProfile(Technician technician) {
        return profileRepository.findByTechnicianId(technician.getId())
                .orElseGet(() -> TechnicianProfile.builder().technician(technician).build());
    }

    private void updateMobileNumber(Technician technician, String mobileNumber) {
        if (mobileNumber == null || mobileNumber.equals(technician.getMobileNumber())) {
            return;
        }
        technicianRepository.findByMobileNumber(mobileNumber)
                .filter(other -> !other.getId().equals(technician.getId()))
                .ifPresent(other -> {
                    throw new AlreadyExistsException("Technician with this phone number already exists");
                });
        technician.setMobileNumber(mobileNumber);
    }

    private ProfileCompletionResponse saveWithCompletion(Technician technician) {
        ProfileCompletionResponse completion = completionCalculator.recalculate(technician);
        try {
            technicianRepository.saveAndFlush(technician);
        } catch (DataIntegrityViolationException e) {
            // DB unique constraint on mobile_number is the source of truth (concurrent updates)
            throw new AlreadyExistsException("Technician with this phone number already exists");
        }
        return completion;
    }

    private MediaFile resolveMediaFile(Long mediaFileId) {
        if (mediaFileId == null) {
            return null;
        }
        return mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new IllegalArgumentException("Media file not found with id: " + mediaFileId));
    }

    /**
     * Replace-all sync of one list section. Returns the saved items in request order.
     * A null request list leaves the section unchanged.
     */
    private <E extends TechnicianProfileItem, R extends ProfileItemRequest> List<E> syncList(
            Technician technician, TechnicianProfileItemRepository<E> repository, List<R> requests,
            Supplier<E> factory, BiConsumer<E, R> applier) {

        List<E> existing = repository.findByTechnicianIdOrderByDisplayOrderAsc(technician.getId());
        if (requests == null) {
            return existing;
        }

        Map<Integer, E> remaining = new LinkedHashMap<>();
        existing.forEach(item -> remaining.put(item.getId(), item));

        List<E> result = new ArrayList<>();
        int order = 0;
        for (R request : requests) {
            if (request == null) {
                continue;
            }
            E entity;
            if (request.getId() == null) {
                entity = factory.get();
                entity.setTechnician(technician);
            } else {
                entity = remaining.remove(request.getId());
                if (entity == null) {
                    throw new IllegalArgumentException("Item " + request.getId()
                            + " does not belong to technician " + technician.getId() + " or is duplicated in the request");
                }
            }
            applier.accept(entity, request);
            entity.setDisplayOrder(order++);
            result.add(entity);
        }

        repository.deleteAll(remaining.values());
        return repository.saveAll(result);
    }

    private TechnicianSkillsStepResponse skillsResponse(Technician technician) {
        return TechnicianSkillsStepResponse.builder()
                .skillSet(map(technician.getSkillSet(), availableServiceMapper::toResponse))
                .expertise(map(technician.getExpertise(), technicianExpertiseMapper::toResponse))
                .skills(map(skillRepository.findByTechnicianIdOrderByDisplayOrderAsc(technician.getId()), profileMapper::toResponse))
                .build();
    }

    private WorkHistoryResponse workHistoryResponse(List<TechnicianEmployment> entries) {
        return WorkHistoryResponse.builder()
                .entries(map(entries, profileMapper::toResponse))
                .unexplainedGaps(employmentGapDetector.findUnexplainedGaps(entries))
                .gapThresholdMonths(employmentGapDetector.getThresholdMonths())
                .build();
    }

    private ReferencesStepResponse referencesResponse(Integer technicianId, TechnicianProfile profile) {
        return ReferencesStepResponse.builder()
                .emergencyContactName(profile != null ? profile.getEmergencyContactName() : null)
                .emergencyContactRelation(profile != null ? profile.getEmergencyContactRelation() : null)
                .emergencyContactPhone(profile != null ? profile.getEmergencyContactPhone() : null)
                .references(map(referenceRepository.findByTechnicianIdOrderByDisplayOrderAsc(technicianId), profileMapper::toResponse))
                .build();
    }

    private static <T> ProfileStepSaveResponse<T> stepResponse(T data, ProfileCompletionResponse completion) {
        return ProfileStepSaveResponse.<T>builder().data(data).completion(completion).build();
    }

    private static <S, T> List<T> map(Collection<S> items, Function<S, T> mapper) {
        return items == null ? Collections.emptyList() : items.stream().map(mapper).toList();
    }
}
