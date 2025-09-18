package com.autoadda.apis.garage.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenericResponse {
    private String status;
    private String code;
    private String message;
    private Object body;

    public static GenericResponse success(Object body) {
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "success" )
                .body ( body )
                .build ( );
    }

    public static GenericResponse success(String message, Object body) {
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( message )
                .body ( body )
                .build ( );
    }
}
