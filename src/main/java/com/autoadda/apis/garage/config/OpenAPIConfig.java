package com.autoadda.apis.garage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${garage.service.base.url}")
    private String devUrl;

    @Value("${garage.service.base.prod.url}")
    private String prodUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server ( );
        devServer.setUrl ( devUrl );
        devServer.setDescription ( "Server URL in Development environment" );

        Server prodServer = new Server ( );
        prodServer.setUrl ( prodUrl );
        prodServer.setDescription ( "Server URL in Production environment" );
        Server qaServer = new Server ( );
        qaServer.setUrl ( "http://192.168.1.100:8020/api/v1" );
        qaServer.setDescription ( "Server URL in QA environment" );
        Contact contact = new Contact ( );
        contact.setEmail ( "yakub@yzsquare.com" );
        contact.setName ( "Yakub Mohammad" );
        contact.setUrl ( "https://www.carservices.com" );

        License mitLicense = new License ( ).name ( "License" ).url ( "https://carservices.com/licenses/" );

        Info info = new Info ( ).title ( "Garage API" ).version ( "1.0" ).contact ( contact )
                .description ( "This API exposes endpoints to manage Garages." )
                .termsOfService ( "https://www.carservices.com/terms" ).license ( mitLicense );

        return new OpenAPI ( ).info ( info ).servers ( List.of ( devServer, prodServer ) );
    }
}
