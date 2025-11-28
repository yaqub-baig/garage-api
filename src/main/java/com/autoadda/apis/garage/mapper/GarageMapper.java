package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.response.GarageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class GarageMapper {
    private final GarageServiceMapper garageServiceMapper;

    public GarageResponse entityToResponse(Garage garage) {
        GarageResponse garageResponse = new GarageResponse ( );
        garageResponse.setId ( garage.getId ( ) != null ? garage.getId ( ) : null );
        garageResponse.setGarageBrandName ( garage.getGarageBrandName ( ) != null ? garage.getGarageBrandName ( ) : null );
        garageResponse.setGarageArabicName ( garage.getGarageArabicName ( ) != null ? garage.getGarageArabicName ( ) : null );
        garageResponse.setName ( garage.getName ( ) != null ? garage.getName ( ) : null );
        garageResponse.setTradeLicenceNumber ( garage.getTradeLicenceNumber ( ) != null ? garage.getTradeLicenceNumber ( ) : null );
        garageResponse.setOpeningTime ( garage.getOpeningTime ( ) != null ? garage.getOpeningTime ( ) : null );
        garageResponse.setClosingTime ( garage.getClosingTime ( ) != null ? garage.getClosingTime ( ) : null );
        garageResponse.setWorkingDays ( garage.getWorkingDays ( ) != null ? garage.getWorkingDays ( ) : null );
        garageResponse.setTrnNumber ( garage.getTrnNumber ( ) != null ? garage.getTrnNumber ( ) : null );
        garageResponse.setWebsite( garage.getWebsite( ) != null ? garage.getWebsite( ) : null );
        garageResponse.setEmail ( garage.getEmail ( ) != null ? garage.getEmail ( ) : null );
        garageResponse.setPhoneNumber ( garage.getPhoneNumber ( ) != null ? garage.getPhoneNumber ( ) : null );
        garageResponse.setMobileNumber ( garage.getMobileNumber ( ) != null ? garage.getMobileNumber ( ) : null );
        garageResponse.setGarageLogo ( garage.getGarageLogo ( ) != null ? garage.getGarageLogo ( ) : null );
        garageResponse.setAccountStatus ( garage.getAccountStatus ( ) != null ? garage.getAccountStatus ( ) : null );
        garageResponse.setCreatedAt ( garage.getCreatedAt ( ) != null ? garage.getCreatedAt ( ) : null );
        garageResponse.setBrands ( garage.getBrands ( ) != null ? garage.getBrands ( ) : null );
        garageResponse.setModels ( garage.getModels ( ) != null ? garage.getModels ( ) : null );
        garageResponse.setGarageOwnerId ( garage.getGarageOwner ( ) != null ? garage.getGarageOwner ( ) : null );
        garageResponse.setIsActive ( garage.isActive ( ) );
        garageResponse.setConcurrentAppointmentCapacity ( garage.getConcurrentAppointmentCapacity ( ) != null ? garage.getConcurrentAppointmentCapacity ( ) : null );
        garageResponse.setServices ( garage.getServices ( ) != null ? garage.getServices ( ).stream ( ).map ( garageServiceMapper::entityToResponse ).toList ( ) : null );
        garageResponse.setGarageImages ( garage.getGarageImages ( ) != null ? garage.getGarageImages ( ) : null );
        garageResponse.setTradeLicenseAttachment ( garage.getTradeLicenseAttachment ( ) != null ? garage.getTradeLicenseAttachment ( ) : null );
        garageResponse.setVatCertificate ( garage.getVatCertificate ( ) != null ? garage.getVatCertificate ( ) : null );
        garageResponse.setLocation ( garage.getGarageLocation ( ) != null ? garage.getGarageLocation ( ) : null );
        garageResponse.setBreakStartTime ( garage.getBreakStartTime ( ) != null ? garage.getBreakStartTime ( ) : null );
        garageResponse.setBreakEndTime ( garage.getBreakEndTime ( ) != null ? garage.getBreakEndTime ( ) : null );
        return garageResponse;
    }

}
