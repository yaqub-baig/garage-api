package com.autoadda.apis.garage.response.os;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OsGenericResponse {
    public static final String SUCCESS = "success";
    private String status;
    private String code;
    private String message;
    private Object body;

    public static OsGenericResponse makeSuccessResponse(Object body) {
        return OsGenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(SUCCESS)
                .body(body)
                .build();
    }

    public static OsGenericResponse makeSuccessResponse(String message, Object body) {
        return OsGenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(message)
                .body(body)
                .build();
    }
}
