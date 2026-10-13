package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLinkResponse {

    private Integer id;
    private Integer displayOrder;
    private SocialPlatform platform;
    private String url;
    private String handle;
}
