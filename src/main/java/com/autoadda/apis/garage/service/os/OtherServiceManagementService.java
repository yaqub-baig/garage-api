package com.autoadda.apis.garage.service.os;


import com.autoadda.apis.garage.entity.os.OtherService;
import com.autoadda.apis.garage.request.os.OtherServiceRequest;
import com.autoadda.apis.garage.response.os.OsResponse;
import com.autoadda.apis.garage.util.Status;

import java.util.List;
import java.util.Optional;

public interface OtherServiceManagementService {

    /**
     * Create a new other service
     */
    OtherService createOtherService(OtherServiceRequest otherServiceRequest,
                                    String autoAddaId
    );

    /**
     * Get all other services
     */
    List<OtherService> getAllOtherServices();

    /**
     * Get all other services by owner ID
     */
    List<OtherService> getAllOtherServicesByOwnerId(String autoAddaId);

    /**
     * Get other service by ID
     */
    Optional<OtherService> getOtherServiceById(Integer id);

    /**
     * Update an existing other service
     */
    OtherService updateOtherService(Integer id, OtherServiceRequest request);

    /**
     * Delete an other service
     */
    void deleteOtherService(Integer id);

    /**
     * Find other services by brand name
     */
    List<OtherService> findOtherServicesByBrandName(String name);

    /**
     * Find other services by service type
     */
    List<OtherService> findOtherServicesByServiceType(String serviceType);

    /**
     * Find other services by status
     */
    List<OtherService> findOtherServicesByStatus(Status status);


    /**
     * Find other service by email
     */
    Optional<OtherService> findOtherServiceByEmail(String email);

    /**
     * Find other service by contact number
     */
    Optional<OtherService> findOtherServiceByContact(String contact);

    List<OsResponse> searchOtherServices( String city, String zip, String garageName, List<Integer> availableServiceIds);
}
