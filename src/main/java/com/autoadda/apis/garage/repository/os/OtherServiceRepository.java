package com.autoadda.apis.garage.repository.os;


import com.autoadda.apis.garage.entity.os.OtherService;
import com.autoadda.apis.garage.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OtherServiceRepository extends JpaRepository<OtherService, Integer> {

    // Find all active other services
    List<OtherService> findByIsActiveTrue();

    // Find by company brand name (case-insensitive)
    @Query("SELECT o FROM OtherService o WHERE LOWER(o.companyBrandName) LIKE LOWER(CONCAT('%', :brandName, '%')) AND o.isActive = true")
    List<OtherService> findByCompanyBrandNameContainingIgnoreCase(@Param("brandName") String brandName);

    // Find by service type (case-insensitive)
    List<OtherService> findByServiceTypeContainingIgnoreCase(String serviceType);

    // Find by status
    List<OtherService> findByAccountStatus(Status status);

    // Find by chain ID
//    List<OtherService> findByChainId(Integer chainId);

    // Find by email
    Optional<OtherService> findByEmail(String email);

    // Find by phone number or mobile number
    @Query("SELECT o FROM OtherService o WHERE (o.phoneNumber = :contact OR o.mobileNumber = :contact) AND o.isActive = true")
    List<OtherService> findByPhoneNumberOrMobileNumber(@Param("contact") String contact);

    // Find by company owner ID
    List<OtherService> findByCompanyOwner(Integer ownerId);

    // Find by company brand name exact match (case-insensitive)
    @Query("SELECT o FROM OtherService o WHERE LOWER(o.companyBrandName) = LOWER(:brandName) AND o.isActive = true")
    Optional<OtherService> findByCompanyBrandNameIgnoreCase(@Param("brandName") String brandName);

    // Find by name (case-insensitive)
    @Query("SELECT o FROM OtherService o WHERE LOWER(o.name) = LOWER(:name) AND o.isActive = true")
    Optional<OtherService> findByNameIgnoreCase(@Param("name") String name);

    // Find by service type and active status
    List<OtherService> findByServiceTypeAndIsActiveTrue(String serviceType);

    // Find by status and active status
    List<OtherService> findByAccountStatusAndIsActiveTrue(Status status);

    // Find by chain ID and active status
//    List<OtherService> findByChainIdAndIsActiveTrue(Integer chainId);

    // Find by email and active status
    Optional<OtherService> findByEmailAndIsActiveTrue(String email);

    // Find by phone number or mobile number and active status
    @Query("SELECT o FROM OtherService o WHERE (o.phoneNumber = :contact OR o.mobileNumber = :contact) AND o.isActive = true")
    List<OtherService> findByPhoneNumberOrMobileNumberAndIsActiveTrue(@Param("contact") String contact);

    // Find by company owner ID and active status
    List<OtherService> findByCompanyOwnerAndIsActiveTrue(Integer ownerId);

    // Count references to media files
    @Query("SELECT COUNT(o) FROM OtherService o WHERE o.companyLogo.id = :mediaFileId")
    long countByCompanyLogoId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT COUNT(os) FROM OtherService os JOIN os.vatCertificate vc WHERE vc.id = :mediaFileId")
    long countByVatCertificateId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT COUNT(os) FROM OtherService os JOIN os.tradeLicenseAttachment tla WHERE tla.id = :mediaFileId")
    long countByTradeLicenseAttachmentId(@Param("mediaFileId") Long mediaFileId);

    @Query("SELECT COUNT(os) FROM OtherService os JOIN os.companyImages ci WHERE ci.id = :mediaFileId")
    long countByCompanyImagesId(@Param("mediaFileId") Long mediaFileId);

    // Find by company brand name containing and active (case-insensitive)
    @Query("SELECT o FROM OtherService o WHERE LOWER(o.companyBrandName) LIKE LOWER(CONCAT('%', :brandName, '%')) AND o.isActive = true")
    List<OtherService> findByCompanyBrandNameContainingIgnoreCaseAndIsActiveTrue(@Param("brandName") String brandName);

    Optional<OtherService> findByTradeLicenceNumber(String tradeLicenceNumber);
    @Query("SELECT DISTINCT g FROM OtherService g " +
            "LEFT JOIN FETCH g.companyLocation " +
            "LEFT JOIN FETCH g.services s " +
            "LEFT JOIN FETCH s.availableService")
    Set<OtherService> findAllWithLocationAndServices();
}
