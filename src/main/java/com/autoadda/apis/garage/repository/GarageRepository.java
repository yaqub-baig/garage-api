package com.autoadda.apis.garage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.util.Status;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Integer> {

	// Optional: Get garage with its services (via GarageService bridge)
	@Query("SELECT g FROM Garage g LEFT JOIN FETCH g.services WHERE g.id = :garageId")
	Optional<Garage> findWithServicesById(@Param("garageId") Integer garageId);

	// Optional: Get all garages with their services
	@Query("SELECT g FROM Garage g LEFT JOIN FETCH g.services")
	List<Garage> findAllWithServices();

	// Optional: Find all garages that offer a specific available service
	@Query("SELECT DISTINCT g FROM Garage g JOIN g.services gs WHERE gs.availableService.id = :serviceId")
	List<Garage> findGaragesByAvailableServiceId(@Param("serviceId") Integer serviceId);

	// Optional: Find garages by legal name, ignoring case
	@Query("SELECT g FROM Garage g WHERE LOWER(g.garageBrandName) LIKE LOWER(CONCAT('%', :legalName, '%'))")
	List<Garage> findByGarageBrandNameIgnoreCase(@Param("legalName") String legalName);

	List<Garage> findByGarageBrandNameContaining(String legalName);

	List<Garage> findByGarageBrandNameContainingIgnoreCase(String legalName);

	List<Garage> findByNameContainingIgnoreCase(String garageName);

	List<Garage> findByAccountStatus(Status status);

	List<Garage> findByChainId(Integer chainId);

	List<Garage> findByGarageType(String garageType);

	Optional<Garage> findByEmail(String emailId);

	Optional<Garage> findByPhoneNumber(String contactNumber);

	Optional<Garage> findById(Integer id);

	Optional<Garage> findByGarageBrandName(String legalName);

	Optional<Garage> findByWebSite(String webSite);

	Optional<Garage> findByName(String garageName);

	Optional<Garage> findByIdAndAccountStatus(Integer id, Status status);

	Optional<Garage> findByIdAndChainId(Integer id, Integer chainId);

	Optional<Garage> findByIdAndGarageType(Integer id, String garageType);

	Optional<Garage> findByIdAndGarageBrandName(Integer id, String legalName);

	Optional<Garage> findByIdAndWebSite(Integer id, String webSite);

	Optional<Garage> findByIdAndEmail(Integer id, String emailId);

	Optional<Garage> findByIdAndPhoneNumber(Integer id, String contactNumber);

	Optional<Garage> findByIdAndName(Integer id, String garageName);

	Optional<Garage> findByIdAndAccountStatusAndChainId(Integer id, Status status, Integer chainId);

	Optional<Garage> findByIdAndAccountStatusAndGarageType(Integer id, Status status, String garageType);

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
}
