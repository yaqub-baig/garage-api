package com.autoadda.apis.garage.response.os;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OsGenericResponse {
    private String status;
    private String code;
    private String message;
    private Object body;

    public static OsGenericResponse success(Object body) {
        return OsGenericResponse.builder()
                .status("success")
                .code("200")
                .message("success")
                .body(body)
                .build();
    }

    public static OsGenericResponse success(String message, Object body) {
        return OsGenericResponse.builder()
                .status("success")
                .code("200")
                .message(message)
                .body(body)
                .build();
    }
}
