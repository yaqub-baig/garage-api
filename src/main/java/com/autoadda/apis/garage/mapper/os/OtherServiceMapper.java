package com.autoadda.apis.garage.mapper.os;

import com.autoadda.apis.garage.entity.os.OtherService;
import com.autoadda.apis.garage.response.os.OsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OtherServiceMapper {
    private final OsServiceMapper osServiceMapper;

    public OsResponse entityToResponse(OtherService osServices) {
        OsResponse otherServiceResponse = new OsResponse ( );
        otherServiceResponse.setId ( osServices.getId ( ) != null ? osServices.getId ( ) : null );
        otherServiceResponse.setCompanyBrandName ( osServices.getCompanyBrandName ( ) != null ? osServices.getCompanyBrandName ( ) : null );
        otherServiceResponse.setCompanyArabicName ( osServices.getCompanyArabicName ( ) != null ? osServices.getCompanyArabicName ( ) : null );
        otherServiceResponse.setName ( osServices.getName ( ) != null ? osServices.getName ( ) : null );
        otherServiceResponse.setTradeLicenceNumber ( osServices.getTradeLicenceNumber ( ) != null ? osServices.getTradeLicenceNumber ( ) : null );
        otherServiceResponse.setOpeningTime ( osServices.getOpeningTime ( ) != null ? osServices.getOpeningTime ( ) : null );
        otherServiceResponse.setClosingTime ( osServices.getClosingTime ( ) != null ? osServices.getClosingTime ( ) : null );
        otherServiceResponse.setWorkingDays ( osServices.getWorkingDays ( ) != null ? osServices.getWorkingDays ( ) : null );
        otherServiceResponse.setTrnNumber ( osServices.getTrnNumber ( ) != null ? osServices.getTrnNumber ( ) : null );
        otherServiceResponse.setWebSite ( osServices.getWebSite ( ) != null ? osServices.getWebSite ( ) : null );
        otherServiceResponse.setEmail ( osServices.getEmail ( ) != null ? osServices.getEmail ( ) : null );
        otherServiceResponse.setPhoneNumber ( osServices.getPhoneNumber ( ) != null ? osServices.getPhoneNumber ( ) : null );
        otherServiceResponse.setMobileNumber ( osServices.getMobileNumber ( ) != null ? osServices.getMobileNumber ( ) : null );
        otherServiceResponse.setCompanyLogo ( osServices.getCompanyLogo ( ) != null ? osServices.getCompanyLogo ( ) : null );
        otherServiceResponse.setAccountStatus ( osServices.getAccountStatus ( ) != null ? osServices.getAccountStatus ( ) : null );
        otherServiceResponse.setCreatedAt ( osServices.getCreatedAt ( ) != null ? osServices.getCreatedAt ( ) : null );
        otherServiceResponse.setCompanyOwnerId ( osServices.getCompanyOwner ( ) != null ? osServices.getCompanyOwner ( ) : null );
        otherServiceResponse.setIsActive ( osServices.isActive ( ) );
        otherServiceResponse.setCompanyImages ( osServices.getCompanyImages ( ) != null ? osServices.getCompanyImages ( ) : null );
        otherServiceResponse.setTradeLicenseAttachment ( osServices.getTradeLicenseAttachment ( ) != null ? osServices.getTradeLicenseAttachment ( ) : null );
        otherServiceResponse.setVatCertificate ( osServices.getVatCertificate ( ) != null ? osServices.getVatCertificate ( ) : null );
        otherServiceResponse.setCompnayLocation ( osServices.getCompanyLocation ( ) != null ? osServices.getCompanyLocation ( ) : null );
        return otherServiceResponse;
    }

}
