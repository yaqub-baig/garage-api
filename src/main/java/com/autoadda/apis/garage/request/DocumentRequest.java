package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.TechnicianDocumentType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * File is uploaded first via POST /mediafiles/upload; send the returned MediaFile id as fileId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest implements ProfileItemRequest {

    private Integer id;
    private TechnicianDocumentType documentType;
    private String documentNumber;
    private LocalDate expiryDate;
    private Long fileId;
}
