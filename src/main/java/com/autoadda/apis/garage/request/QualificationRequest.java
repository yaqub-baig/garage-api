package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.QualificationType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Certificate file is uploaded first via POST /mediafiles/upload; send the returned MediaFile id as certificateFileId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualificationRequest implements ProfileItemRequest {

    private Integer id;
    private QualificationType qualificationType;
    private String title;
    private String fieldOfStudy;
    private String institution;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String grade;
    private Long certificateFileId;
}
