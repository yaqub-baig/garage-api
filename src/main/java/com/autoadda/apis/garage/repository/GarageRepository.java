package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.util.GarageVerificationStatus;
import com.autoadda.apis.garage.util.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Integer> {

    // Optional: Get garage with its services (via GarageService bridge)
    @Query("SELECT g FROM Garage g LEFT JOIN FETCH g.services WHERE g.id = :garageId")
    Optional<Garage> findWithServicesById(@Param("garageId") Integer garageId);

    // Optional: Get all garages with their services
    @Query("SELECT g FROM Garage g LEFT JOIN FETCH g.services")
    List<Garage> findAllWithServices();

    // Optional: Find all garages that offer a specific available service
    @Query("SELECT DISTINCT g FROM Garage g JOIN g.services gs WHERE gs.availableService.id = :serviceId AND g.accountStatus = 'APPROVED'")
    List<Garage> findGaragesByAvailableServiceId(@Param("serviceId") Integer serviceId);

    // Optional: Find garages by legal name, ignoring case
    @Query("SELECT g FROM Garage g WHERE LOWER(g.garageBrandName) LIKE LOWER(CONCAT('%', :legalName, '%'))")
    List<Garage> findByGarageBrandNameIgnoreCase(@Param("legalName") String legalName);

    List<Garage> findByGarageBrandNameContaining(String legalName);

    List<Garage> findByGarageBrandNameContainingIgnoreCase(String legalName);

    @Query("SELECT g FROM Garage g WHERE LOWER(g.garageBrandName) LIKE LOWER(CONCAT('%', :legalName, '%')) AND g.accountStatus = 'APPROVED'")
    List<Garage> findByGarageBrandNameContainingIgnoreCaseAndApproved(@Param("legalName") String legalName);

    List<Garage> findByNameContainingIgnoreCase(String garageName);

    List<Garage> findByAccountStatus(Status status);


    Optional<Garage> findByEmail(String emailId);

    Optional<Garage> findByPhoneNumber(String contactNumber);

    Optional<Garage> findById(Integer id);

    Optional<Garage> findByGarageBrandName(String legalName);

    Optional<Garage> findByWebsite(String website);

    Optional<Garage> findByName(String garageName);

    Optional<Garage> findByIdAndAccountStatus(Integer id, Status status);


    Optional<Garage> findByIdAndGarageBrandName(Integer id, String legalName);

    Optional<Garage> findByIdAndWebsite(Integer id, String webSite);

    Optional<Garage> findByIdAndEmail(Integer id, String emailId);

    Optional<Garage> findByIdAndPhoneNumber(Integer id, String contactNumber);

    Optional<Garage> findByIdAndName(Integer id, String garageName);

    Optional<Garage> findByIdAndAccountStatusAndGarageBrandName(Integer id, Status status, String legalName);

    List<Garage> findByGarageOwner(Integer ownerId);

    @Query("SELECT COUNT(g) FROM Garage g WHERE g.garageLogo.id = :mediaFileId")
    long countByGarageLogoId(@Param("mediaFileId") Long mediaFileId);

    // Check if media file is referenced in garage images
    @Query("SELECT COUNT(g) FROM Garage g JOIN g.garageImages gi WHERE gi.id = :mediaFileId")
    long countByGarageImageId(@Param("mediaFileId") Long mediaFileId);

    // Check if media file is referenced in trade license attachments
    @Query("SELECT COUNT(g) FROM Garage g JOIN g.tradeLicenseAttachment tla WHERE tla.id = :mediaFileId")
    long countByTradeLicenseId(@Param("mediaFileId") Long mediaFileId);

    // Check if media file is referenced in VAT certificates
    @Query("SELECT COUNT(g) FROM Garage g JOIN g.vatCertificate vc WHERE vc.id = :mediaFileId")
    long countByVatCertificateId(@Param("mediaFileId") Long mediaFileId);

    Optional<Garage> findByTradeLicenceNumber(String tradeLicenceNumber);

    @Query("SELECT DISTINCT g FROM Garage g " +
            "LEFT JOIN FETCH g.garageLocation " +
            "LEFT JOIN FETCH g.services s " +
            "LEFT JOIN FETCH s.availableService " +
            "WHERE g.accountStatus = 'APPROVED'")
    Set<Garage> findAllWithLocationAndServices();

    // Step 1 of 2-query pagination: fetch only IDs with SQL-level LIMIT (no collection joins)
    @Query(value = "SELECT g.id FROM Garage g LEFT JOIN g.garageLocation WHERE g.accountStatus = 'APPROVED'",
           countQuery = "SELECT COUNT(DISTINCT g) FROM Garage g WHERE g.accountStatus = 'APPROVED'")
    Page<Integer> findApprovedGarageIdsPaged(Pageable pageable);

    // Step 2 of 2-query pagination: hydrate full entities with collections for a known ID set
    @Query("SELECT DISTINCT g FROM Garage g " +
           "LEFT JOIN FETCH g.garageLocation " +
           "LEFT JOIN FETCH g.services s " +
           "LEFT JOIN FETCH s.availableService " +
           "WHERE g.id IN :ids")
    List<Garage> findByIdInWithCollections(@Param("ids") List<Integer> ids);

    // ====================================================================================
    // GARAGE VERIFICATION QUERY METHODS (for future admin features)
    // ====================================================================================

    /**
     * Find all pending garages ordered by creation date (oldest first)
     */
    @Query("SELECT g FROM Garage g WHERE g.accountStatus = 'PENDING' ORDER BY g.createdAt ASC")
    List<Garage> findAllPendingGarages();

    /**
     * Find all rejected garages ordered by rejection date (newest first)
     */
    @Query("SELECT g FROM Garage g WHERE g.accountStatus = 'REJECTED' ORDER BY g.rejectedAt DESC")
    List<Garage> findAllRejectedGarages();

    /**
     * Find garages by approver admin user ID
     */
    List<Garage> findByApprovedBy(Integer adminUserId);

    /**
     * Find garages by rejecter admin user ID
     */
    List<Garage> findByRejectedBy(Integer adminUserId);

    /**
     * Find garages by last reviewer (approved or rejected)
     */
    List<Garage> findByLastReviewedBy(Integer adminUserId);

    /**
     * Count garages by verification status
     */
    long countByAccountStatus(GarageVerificationStatus status);

    /**
     * Find pending garages older than a specific date (for monitoring stale reviews)
     */
    @Query("SELECT g FROM Garage g WHERE g.accountStatus = 'PENDING' " +
           "AND g.createdAt < :cutoffDate ORDER BY g.createdAt ASC")
    List<Garage> findPendingGaragesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find garages with multiple resubmissions (potential problem cases)
     */
    @Query("SELECT g FROM Garage g WHERE g.resubmissionCount >= :minCount ORDER BY g.resubmissionCount DESC")
    List<Garage> findGaragesWithMultipleResubmissions(@Param("minCount") int minCount);

    /**
     * Find garages by verification status (using new enum)
     */
    List<Garage> findByAccountStatus(GarageVerificationStatus status);
}
