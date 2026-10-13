package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLinkRequest implements ProfileItemRequest {

    private Integer id;
    private SocialPlatform platform;
    private String url;
    private String handle;
}
