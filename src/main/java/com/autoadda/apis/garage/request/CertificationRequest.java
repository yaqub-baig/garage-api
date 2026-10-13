package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.CertificationType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document is uploaded first via POST /mediafiles/upload; send the returned MediaFile id as documentFileId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRequest implements ProfileItemRequest {

    private Integer id;
    private CertificationType certificationType;
    private String name;
    private String issuingOrganization;
    private String credentialId;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Long documentFileId;
}
