package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.LanguageProficiency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageRequest implements ProfileItemRequest {

    private Integer id;
    private String language;
    private LanguageProficiency proficiency;
}
