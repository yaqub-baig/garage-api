package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.LanguageProficiency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageResponse {

    private Integer id;
    private Integer displayOrder;
    private String language;
    private LanguageProficiency proficiency;
}
