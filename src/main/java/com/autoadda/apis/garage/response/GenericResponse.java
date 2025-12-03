package com.autoadda.apis.garage.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenericResponse {
    public static final String SUCCESS = "success";
    private String status;
    private String code;
    private String message;
    private Object body;

    public static GenericResponse makeSuccessResponse(Object body) {
        return GenericResponse.builder ( )
                .status (SUCCESS)
                .code ( "200" )
                .message (SUCCESS)
                .body ( body )
                .build ( );
    }

    public static GenericResponse makeSuccessResponse(String message, Object body) {
        return GenericResponse.builder ( )
                .status (SUCCESS)
                .code ( "200" )
                .message ( message )
                .body ( body )
                .build ( );
    }

    public static GenericResponse failed(String s, Object o) {
        return GenericResponse.builder ( )
                .status (SUCCESS)
                .code ( "200" )
                .message ( s )
                .body ( o )
                .build ( );
    }
}
