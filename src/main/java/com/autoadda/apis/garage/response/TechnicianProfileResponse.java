package com.autoadda.apis.garage.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianProfileResponse {

    private Integer technicianId;
    private PersonalDetailsResponse personal;
    private List<QualificationResponse> qualifications;
    private List<CertificationResponse> certifications;
    private TechnicianSkillsStepResponse skills;
    private WorkHistoryResponse employment;
    private List<LanguageResponse> languages;
    private List<DocumentResponse> documents;
    private ReferencesStepResponse references;
    private List<SocialLinkResponse> socialLinks;
    private ProfileCompletionResponse completion;
}
