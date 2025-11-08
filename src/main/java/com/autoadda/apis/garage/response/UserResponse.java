package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
    private String mobileNumber;
    private MediaFile profileImage;
    private boolean isVerified;
}
