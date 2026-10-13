package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.TechnicianDocumentType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Integer id;
    private Integer displayOrder;
    private TechnicianDocumentType documentType;
    private String documentNumber;
    private LocalDate expiryDate;
    private Long fileId;
    private String fileUrl;
    private String fileName;
}
