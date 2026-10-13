package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.CertificationType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationResponse {

    private Integer id;
    private Integer displayOrder;
    private CertificationType certificationType;
    private String name;
    private String issuingOrganization;
    private String credentialId;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Long documentFileId;
    private String documentFileUrl;
    private String documentFileName;
}
