package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsServiceRepository extends JpaRepository<OsService, Integer> {

    //find by id



    List<OsService> findByOtherServiceId(Integer otherServiceId);

    List<OsService> findByIsActive(Boolean isActive);

    List<OsService> findByIsActiveAndOtherServiceId(Boolean isActive, Integer otherServiceId);

    List<OsService> findByAvailableService_NameContainingIgnoreCase(String name);

    List<OsService> findByAvailableService_NameContainingIgnoreCaseAndIsActive(String name, Boolean isActive);

    List<OsService> findByAvailableService_NameContainingIgnoreCaseAndIsActiveAndOtherServiceId(String name,
                                                                                               Boolean isActive, Integer otherServiceId);

    List<OsService> findByAvailableService_Id(Integer serviceId);

    List<OsService> findByOtherService_IdAndIsActive(Integer otherServiceId, Boolean isActive);

    List<OsService> findByOtherService_IdAndAvailableService_Id(Integer otherServiceId, Integer serviceId);
}
