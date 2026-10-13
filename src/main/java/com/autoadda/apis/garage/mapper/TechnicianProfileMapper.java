package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.request.*;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.util.EmploymentEntryType;
import org.springframework.stereotype.Component;

/**
 * Maps the technician CV profile sections between entities and DTOs.
 * List items are always overwritten in full by their request (the step sends the whole item);
 * the personal step only applies non-null fields.
 */
@Component
public class TechnicianProfileMapper {

    // ---------- Personal details ----------

    public PersonalDetailsResponse toPersonalResponse(Technician technician, TechnicianProfile profile) {
        PersonalDetailsResponse.PersonalDetailsResponseBuilder builder = PersonalDetailsResponse.builder()
                .firstName(technician.getFirstName())
                .lastName(technician.getLastName())
                .emailId(technician.getEmailId())
                .mobileNumber(technician.getMobileNumber())
                .city(technician.getCity())
                .profileImg(technician.getProfileImg())
                .yearOfExperience(technician.getYearOfExperience());
        if (profile != null) {
            builder.dateOfBirth(profile.getDateOfBirth())
                    .gender(profile.getGender())
                    .nationality(profile.getNationality())
                    .maritalStatus(profile.getMaritalStatus())
                    .alternatePhone(profile.getAlternatePhone())
                    .addressLine(profile.getAddressLine())
                    .country(profile.getCountry())
                    .passportNumber(profile.getPassportNumber())
                    .passportExpiry(profile.getPassportExpiry())
                    .visaStatus(profile.getVisaStatus())
                    .visaExpiry(profile.getVisaExpiry())
                    .drivingLicenseNumber(profile.getDrivingLicenseNumber())
                    .professionalSummary(profile.getProfessionalSummary());
        }
        return builder.build();
    }

    /**
     * Applies the non-null technician-level fields. Mobile number is handled by the service
     * because it needs a uniqueness check.
     */
    public void applyPersonalToTechnician(Technician technician, PersonalDetailsRequest request) {
        if (request.getFirstName() != null)
            technician.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            technician.setLastName(request.getLastName());
        if (request.getEmailId() != null)
            technician.setEmailId(request.getEmailId());
        if (request.getCity() != null)
            technician.setCity(request.getCity());
        if (request.getProfileImg() != null)
            technician.setProfileImg(request.getProfileImg());
        if (request.getYearOfExperience() != null)
            technician.setYearOfExperience(request.getYearOfExperience());
    }

    public void applyPersonalToProfile(TechnicianProfile profile, PersonalDetailsRequest request) {
        if (request.getDateOfBirth() != null)
            profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null)
            profile.setGender(request.getGender());
        if (request.getNationality() != null)
            profile.setNationality(request.getNationality());
        if (request.getMaritalStatus() != null)
            profile.setMaritalStatus(request.getMaritalStatus());
        if (request.getAlternatePhone() != null)
            profile.setAlternatePhone(request.getAlternatePhone());
        if (request.getAddressLine() != null)
            profile.setAddressLine(request.getAddressLine());
        if (request.getCountry() != null)
            profile.setCountry(request.getCountry());
        if (request.getPassportNumber() != null)
            profile.setPassportNumber(request.getPassportNumber());
        if (request.getPassportExpiry() != null)
            profile.setPassportExpiry(request.getPassportExpiry());
        if (request.getVisaStatus() != null)
            profile.setVisaStatus(request.getVisaStatus());
        if (request.getVisaExpiry() != null)
            profile.setVisaExpiry(request.getVisaExpiry());
        if (request.getDrivingLicenseNumber() != null)
            profile.setDrivingLicenseNumber(request.getDrivingLicenseNumber());
        if (request.getProfessionalSummary() != null)
            profile.setProfessionalSummary(request.getProfessionalSummary());
    }

    public void applyEmergencyContact(TechnicianProfile profile, ReferencesStepRequest request) {
        if (request.getEmergencyContactName() != null)
            profile.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactRelation() != null)
            profile.setEmergencyContactRelation(request.getEmergencyContactRelation());
        if (request.getEmergencyContactPhone() != null)
            profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
    }

    // ---------- Qualifications ----------

    public void apply(TechnicianQualification entity, QualificationRequest request, MediaFile certificateFile) {
        entity.setQualificationType(request.getQualificationType());
        entity.setTitle(request.getTitle());
        entity.setFieldOfStudy(request.getFieldOfStudy());
        entity.setInstitution(request.getInstitution());
        entity.setCountry(request.getCountry());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setIsCurrent(request.getIsCurrent());
        entity.setGrade(request.getGrade());
        entity.setCertificateFile(certificateFile);
    }

    public QualificationResponse toResponse(TechnicianQualification entity) {
        MediaFile file = entity.getCertificateFile();
        return QualificationResponse.builder()
                .id(entity.getId())
                .displayOrder(entity.getDisplayOrder())
                .qualificationType(entity.getQualificationType())
                .title(entity.getTitle())
                .fieldOfStudy(entity.getFieldOfStudy())
                .institution(entity.getInstitution())
                .country(entity.getCountry())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isCurrent(entity.getIsCurrent())
                .grade(entity.getGrade())
                .certificateFileId(file != null ? file.getId() : null)
                .certificateFileUrl(file != null ? file.getFilePath() : null)
                .certificateFileName(file != null ? file.getFileName() : null)
                .build();
    }

    // ---------- Certifications & licenses ----------

    public void apply(TechnicianCertification entity, CertificationRequest request, MediaFile document) {
        entity.setCertificationType(request.getCertificationType());
        entity.setName(request.getName());
        entity.setIssuingOrganization(request.getIssuingOrganization());
        entity.setCredentialId(request.getCredentialId());
        entity.setIssueDate(request.getIssueDate());
        entity.setExpiryDate(request.getExpiryDate());
        entity.setDocument(document);
    }

    public CertificationResponse toResponse(TechnicianCertification entity) {
        MediaFile file = entity.getDocument();
        return CertificationResponse.builder()
                .id(entity.getId())
                .displayOrder(entity.getDisplayOrder())
                .certificationType(entity.getCertificationType())
                .name(entity.getName())
                .issuingOrganization(entity.getIssuingOrganization())
                .credentialId(entity.getCredentialId())
                .issueDate(entity.getIssueDate())
                .expiryDate(entity.getExpiryDate())
                .documentFileId(file != null ? file.getId() : null)
                .documentFileUrl(file != null ? file.getFilePath() : null)
                .documentFileName(file != null ? file.getFileName() : null)
                .build();
    }

    // ---------- Free-text skills ----------

    public void apply(TechnicianSkill entity, SkillRequest request) {
        entity.setSkillName(request.getSkillName());
        entity.setLevel(request.getLevel());
        entity.setYearsOfExperience(request.getYearsOfExperience());
    }

    public SkillResponse toResponse(TechnicianSkill entity) {
        return SkillResponse.builder()
                .id(entity.getId())
                .displayOrder(entity.getDisplayOrder())
                .skillName(entity.getSkillName())
                .level(entity.getLevel())
                .yearsOfExperience(entity.getYearsOfExperience())
                .build();
    }

    // ---------- Employment ----------

    public void apply(TechnicianEmployment entity, EmploymentRequest request) {
        entity.setEntryType(request.getEntryType() != null ? request.getEntryType() : EmploymentEntryType.EMPLOYMENT);
        entity.setBreakReason(request.getBreakReason());
        entity.setBreakDescription(request.getBreakDescription());
        entity.setEmployerName(request.getEmployerName());
        entity.setJobTitle(request.getJobTitle());
        entity.setCity(request.getCity());
        entity.setCountry(request.getCountry());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setIsCurrent(request.getIsCurrent());
        entity.setResponsibilities(request.getResponsibilities());
        entity.setReasonForLeaving(request.getReasonForLeaving());
    }

    public EmploymentResponse toResponse(TechnicianEmployment entity) {
        return EmploymentResponse.builder()
                .id(entity.getId())
                .displayOrder(entity.getDisplayOrder())
                .entryType(entity.getEntryType() != null ? entity.getEntryType() : EmploymentEntryType.EMPLOYMENT)
                .breakReason(entity.getBreakReason())
                .breakDescription(entity.getBreakDescription())
                .employerName(entity.getEmployerName())
                .jobTitle(entity.getJobTitle())
                .city(entity.getCity())
                .country(entity.getCountry())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isCurrent(entity.getIsCurrent())
                .responsibilities(entity.getResponsibilities())
                .reasonForLeaving(entity.getReasonForLeaving())
                .build();
    }

    // ---------- Languages ----------

    public void apply(TechnicianLanguage entity, LanguageRequest request) {
        entity.setLanguage(request.getLanguage());
        entity.setProficiency(request.getProficiency());
    }

    public LanguageResponse toResponse(TechnicianLanguage entity) {
        return LanguageResponse.builder()
                .id(entity.getId())
                .displayOrder(entity.getDisplayOrder())
                .language(entity.getLanguage())
                .proficiency(entity.getProficiency())
                .build();
    }

    // ---------- Documents ----------

    public void apply(TechnicianDocument entity, DocumentRequest request, MediaFile file) {
        entity.setDocumentType(request.getDocumentType());
        entity.setDocumentNumber(request.getDocumentNumber());
        entity.setExpiryDate(request.getExpiryDate());
        entity.setFile(file);
    }

    public DocumentResponse toResponse(TechnicianDocument entity) {
        MediaFile file = entity.getFile();
        return DocumentResponse.builder()
                .id(entity.getId())
                .displayOrder(entity.getDisplayOrder())
                .documentType(entity.getDocumentType())
                .documentNumber(entity.getDocumentNumber())
                .expiryDate(entity.getExpiryDate())
                .fileId(file != null ? file.getId() : null)
                .fileUrl(file != null ? file.getFilePath() : null)
                .fileName(file != null ? file.getFileName() : null)
                .build();
    }

    // ---------- References ----------

    public void apply(TechnicianReference entity, ReferenceRequest request) {
        entity.setName(request.getName());
        entity.setCompany(request.getCompany());
        entity.setDesignation(request.getDesignation());
        entity.setRelationship(request.getRelationship());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
    }

    public ReferenceResponse toResponse(TechnicianReference entity) {
        return ReferenceResponse.builder()
                .id(entity.getId())
                .displayOrder(entity.getDisplayOrder())
                .name(entity.getName())
                .company(entity.getCompany())
                .designation(entity.getDesignation())
                .relationship(entity.getRelationship())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .build();
    }

    // ---------- Social links ----------

    public void apply(TechnicianSocialLink entity, SocialLinkRequest request) {
        entity.setPlatform(request.getPlatform());
        entity.setUrl(request.getUrl());
        entity.setHandle(request.getHandle());
    }

    public SocialLinkResponse toResponse(TechnicianSocialLink entity) {
        return SocialLinkResponse.builder()
                .id(entity.getId())
                .displayOrder(entity.getDisplayOrder())
                .platform(entity.getPlatform())
                .url(entity.getUrl())
                .handle(entity.getHandle())
                .build();
    }
}
