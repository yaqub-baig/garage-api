package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.entity.TechnicianProfile;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.response.ProfileCompletionResponse;
import com.autoadda.apis.garage.response.ProfileStepCompletionResponse;
import com.autoadda.apis.garage.util.ProfileCompletionStatus;
import com.autoadda.apis.garage.util.ProfileStep;
import com.autoadda.apis.garage.util.ProfileStepStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

/**
 * Calculates how complete a technician's CV profile is.
 * <p>
 * Purely informational: nothing in the profile is mandatory and completion never blocks a save.
 * Each of the 9 steps carries equal weight. List steps count as complete once they have at least one item;
 * the personal and references steps count the fraction of their core fields that are filled.
 */
@Component
@RequiredArgsConstructor
public class TechnicianProfileCompletionCalculator {

    private final TechnicianProfileRepository profileRepository;
    private final TechnicianQualificationRepository qualificationRepository;
    private final TechnicianCertificationRepository certificationRepository;
    private final TechnicianSkillRepository skillRepository;
    private final TechnicianEmploymentRepository employmentRepository;
    private final TechnicianLanguageRepository languageRepository;
    private final TechnicianDocumentRepository documentRepository;
    private final TechnicianReferenceRepository referenceRepository;
    private final TechnicianSocialLinkRepository socialLinkRepository;

    /**
     * Call after the profile changed: stamps profileUpdatedAt and stores the new percent/status
     * on the technician. The caller is responsible for saving the technician.
     */
    public ProfileCompletionResponse recalculate(Technician technician) {
        technician.setProfileUpdatedAt(LocalDateTime.now());
        return refresh(technician);
    }

    /**
     * Stores the current percent/status on the technician without touching profileUpdatedAt.
     * Used on reads so technicians created before this feature get their stored values corrected.
     */
    public ProfileCompletionResponse refresh(Technician technician) {
        ProfileCompletionResponse completion = calculate(technician);
        technician.setProfileCompletionPercent(completion.getPercent());
        technician.setProfileCompletionStatus(completion.getStatus());
        return completion;
    }

    public ProfileCompletionResponse calculate(Technician technician) {
        Integer technicianId = technician.getId();
        TechnicianProfile profile = technicianId == null ? null
                : profileRepository.findByTechnicianId(technicianId).orElse(null);

        Map<ProfileStep, Double> fractions = new EnumMap<>(ProfileStep.class);
        fractions.put(ProfileStep.PERSONAL, personalFraction(technician, profile));
        fractions.put(ProfileStep.QUALIFICATIONS, hasItems(technicianId, qualificationRepository::countByTechnicianId));
        fractions.put(ProfileStep.CERTIFICATIONS, hasItems(technicianId, certificationRepository::countByTechnicianId));
        fractions.put(ProfileStep.SKILLS, skillsFraction(technician));
        // Career breaks alone do not complete the work history step
        fractions.put(ProfileStep.EMPLOYMENT, hasItems(technicianId, employmentRepository::countJobsByTechnicianId));
        fractions.put(ProfileStep.LANGUAGES, hasItems(technicianId, languageRepository::countByTechnicianId));
        fractions.put(ProfileStep.DOCUMENTS, hasItems(technicianId, documentRepository::countByTechnicianId));
        fractions.put(ProfileStep.REFERENCES, referencesFraction(technicianId, profile));
        fractions.put(ProfileStep.SOCIAL_LINKS, hasItems(technicianId, socialLinkRepository::countByTechnicianId));

        List<ProfileStepCompletionResponse> steps = new ArrayList<>();
        ProfileStep nextIncompleteStep = null;
        double total = 0;
        for (ProfileStep step : ProfileStep.values()) {
            double fraction = fractions.get(step);
            total += fraction;
            ProfileStepStatus stepStatus = toStepStatus(fraction);
            if (nextIncompleteStep == null && stepStatus != ProfileStepStatus.COMPLETED) {
                nextIncompleteStep = step;
            }
            steps.add(ProfileStepCompletionResponse.builder()
                    .step(step)
                    .order(step.getOrder())
                    .status(stepStatus)
                    .percent((int) Math.round(fraction * 100))
                    .build());
        }

        int percent = (int) Math.round(total * 100 / ProfileStep.values().length);
        return ProfileCompletionResponse.builder()
                .percent(percent)
                .status(toOverallStatus(percent))
                .steps(steps)
                .nextIncompleteStep(nextIncompleteStep)
                .updatedAt(technician.getProfileUpdatedAt())
                .build();
    }

    private double personalFraction(Technician technician, TechnicianProfile profile) {
        return fractionFilled(
                technician.getFirstName(),
                technician.getLastName(),
                technician.getMobileNumber(),
                technician.getCity(),
                profile != null ? profile.getDateOfBirth() : null,
                profile != null ? profile.getNationality() : null,
                profile != null ? profile.getProfessionalSummary() : null);
    }

    private double skillsFraction(Technician technician) {
        boolean hasCatalogueSkills = technician.getSkillSet() != null && !technician.getSkillSet().isEmpty();
        boolean hasExpertise = technician.getExpertise() != null && !technician.getExpertise().isEmpty();
        boolean hasFreeTextSkills = technician.getId() != null && skillRepository.countByTechnicianId(technician.getId()) > 0;
        return hasCatalogueSkills || hasExpertise || hasFreeTextSkills ? 1.0 : 0.0;
    }

    private double referencesFraction(Integer technicianId, TechnicianProfile profile) {
        boolean hasReferences = hasItems(technicianId, referenceRepository::countByTechnicianId) > 0;
        return fractionFilled(
                profile != null ? profile.getEmergencyContactName() : null,
                profile != null ? profile.getEmergencyContactPhone() : null,
                hasReferences ? Boolean.TRUE : null);
    }

    private double hasItems(Integer technicianId, ToLongFunction<Integer> counter) {
        return technicianId != null && counter.applyAsLong(technicianId) > 0 ? 1.0 : 0.0;
    }

    private static double fractionFilled(Object... values) {
        long filled = Stream.of(values).filter(TechnicianProfileCompletionCalculator::isFilled).count();
        return (double) filled / values.length;
    }

    private static boolean isFilled(Object value) {
        if (value instanceof String s) {
            return StringUtils.isNotBlank(s);
        }
        return value != null;
    }

    private static ProfileStepStatus toStepStatus(double fraction) {
        if (fraction <= 0) {
            return ProfileStepStatus.NOT_STARTED;
        }
        return fraction >= 1 ? ProfileStepStatus.COMPLETED : ProfileStepStatus.PARTIAL;
    }

    private static ProfileCompletionStatus toOverallStatus(int percent) {
        if (percent <= 0) {
            return ProfileCompletionStatus.NOT_STARTED;
        }
        return percent >= 100 ? ProfileCompletionStatus.COMPLETED : ProfileCompletionStatus.IN_PROGRESS;
    }
}
