package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.QualificationType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualificationResponse {

    private Integer id;
    private Integer displayOrder;
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
    private String certificateFileUrl;
    private String certificateFileName;
}
