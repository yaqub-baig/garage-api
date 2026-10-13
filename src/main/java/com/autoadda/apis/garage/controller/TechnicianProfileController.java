package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.*;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.service.TechnicianProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Step-wise technician CV profile. Every step is optional and can be saved independently;
 * each save returns the saved section together with the recalculated profile completion.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/technicians/{technicianId}/profile")
@RequiredArgsConstructor
@Slf4j
public class TechnicianProfileController {

    private final TechnicianProfileService technicianProfileService;

    /**
     * Full CV profile with completion
     * GET /technicians/{technicianId}/profile
     */
    @GetMapping
    public ResponseEntity<GenericResponse> getProfile(@PathVariable Integer technicianId) {
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Technician profile retrieved successfully", technicianProfileService.getProfile(technicianId)));
    }

    /**
     * Completion percent, overall status, per-step status and next incomplete step
     * GET /technicians/{technicianId}/profile/completion
     */
    @GetMapping("/completion")
    public ResponseEntity<GenericResponse> getCompletion(@PathVariable Integer technicianId) {
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Profile completion retrieved successfully", technicianProfileService.getCompletion(technicianId)));
    }

    /**
     * Step 1 - personal details (null fields are left unchanged)
     * PUT /technicians/{technicianId}/profile/personal
     */
    @PutMapping("/personal")
    public ResponseEntity<GenericResponse> savePersonalDetails(@PathVariable Integer technicianId,
                                                               @RequestBody(required = false) PersonalDetailsRequest request) {
        log.info("Saving personal details for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Personal details saved successfully", technicianProfileService.savePersonalDetails(technicianId, request)));
    }

    /**
     * Step 2 - qualifications (replaces the whole list)
     * PUT /technicians/{technicianId}/profile/qualifications
     */
    @PutMapping("/qualifications")
    public ResponseEntity<GenericResponse> saveQualifications(@PathVariable Integer technicianId,
                                                              @RequestBody(required = false) List<QualificationRequest> requests) {
        log.info("Saving qualifications for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Qualifications saved successfully", technicianProfileService.saveQualifications(technicianId, requests)));
    }

    /**
     * Step 3 - certifications & licenses (replaces the whole list)
     * PUT /technicians/{technicianId}/profile/certifications
     */
    @PutMapping("/certifications")
    public ResponseEntity<GenericResponse> saveCertifications(@PathVariable Integer technicianId,
                                                              @RequestBody(required = false) List<CertificationRequest> requests) {
        log.info("Saving certifications for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Certifications saved successfully", technicianProfileService.saveCertifications(technicianId, requests)));
    }

    /**
     * Step 4 - technical skills: skillSet, expertise and free-text skills
     * PUT /technicians/{technicianId}/profile/skills
     */
    @PutMapping("/skills")
    public ResponseEntity<GenericResponse> saveSkills(@PathVariable Integer technicianId,
                                                      @RequestBody(required = false) TechnicianSkillsStepRequest request) {
        log.info("Saving skills for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Skills saved successfully", technicianProfileService.saveSkills(technicianId, request)));
    }

    /**
     * Step 5 - work history: jobs and career breaks (replaces the whole list); response includes unexplained gaps
     * PUT /technicians/{technicianId}/profile/employment
     */
    @PutMapping("/employment")
    public ResponseEntity<GenericResponse> saveEmployment(@PathVariable Integer technicianId,
                                                          @RequestBody(required = false) List<EmploymentRequest> requests) {
        log.info("Saving employment history for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Employment history saved successfully", technicianProfileService.saveEmployment(technicianId, requests)));
    }

    /**
     * Step 6 - languages (replaces the whole list)
     * PUT /technicians/{technicianId}/profile/languages
     */
    @PutMapping("/languages")
    public ResponseEntity<GenericResponse> saveLanguages(@PathVariable Integer technicianId,
                                                         @RequestBody(required = false) List<LanguageRequest> requests) {
        log.info("Saving languages for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Languages saved successfully", technicianProfileService.saveLanguages(technicianId, requests)));
    }

    /**
     * Step 7 - documents (replaces the whole list; upload files first via POST /mediafiles/upload)
     * PUT /technicians/{technicianId}/profile/documents
     */
    @PutMapping("/documents")
    public ResponseEntity<GenericResponse> saveDocuments(@PathVariable Integer technicianId,
                                                         @RequestBody(required = false) List<DocumentRequest> requests) {
        log.info("Saving documents for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Documents saved successfully", technicianProfileService.saveDocuments(technicianId, requests)));
    }

    /**
     * Step 8 - references & emergency contact
     * PUT /technicians/{technicianId}/profile/references
     */
    @PutMapping("/references")
    public ResponseEntity<GenericResponse> saveReferences(@PathVariable Integer technicianId,
                                                          @RequestBody(required = false) ReferencesStepRequest request) {
        log.info("Saving references for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "References saved successfully", technicianProfileService.saveReferences(technicianId, request)));
    }

    /**
     * Step 9 - social profiles (Instagram, YouTube, ...; replaces the whole list)
     * PUT /technicians/{technicianId}/profile/social-links
     */
    @PutMapping("/social-links")
    public ResponseEntity<GenericResponse> saveSocialLinks(@PathVariable Integer technicianId,
                                                           @RequestBody(required = false) List<SocialLinkRequest> requests) {
        log.info("Saving social links for technician: {}", technicianId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Social links saved successfully", technicianProfileService.saveSocialLinks(technicianId, requests)));
    }
}
