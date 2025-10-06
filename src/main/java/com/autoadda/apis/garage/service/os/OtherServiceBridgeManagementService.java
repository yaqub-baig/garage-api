package com.autoadda.apis.garage.service.os;


import com.autoadda.apis.garage.entity.os.OtherServiceBridge;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.os.OtherServiceBridgeRepository;
import com.autoadda.apis.garage.repository.os.OtherServiceRepository;
import com.autoadda.apis.garage.repository.os.OsAvailableServiceRepository;
import com.autoadda.apis.garage.request.GarageServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtherServiceBridgeManagementService {
    private final OtherServiceRepository otherServiceRepository;
    private final OsAvailableServiceRepository osAvailableServiceRepository;
    private final OtherServiceBridgeRepository otherServiceBridgeRepository;

    private OtherServiceBridge mapToEntity(GarageServiceRequest service) {
        OtherServiceBridge otherServiceBridge = new OtherServiceBridge ( );
        otherServiceBridge.setIsActive ( service.isActive ( ) );
        otherServiceBridge.setOtherServiceGarage ( otherServiceRepository.findById ( service.getGarageId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) ) );
        otherServiceBridge.setAvailableService ( osAvailableServiceRepository.findById ( service.getServiceId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Service not found" ) ) );
        return otherServiceBridge;
    }


    public OtherServiceBridge createOtherServiceBridge(GarageServiceRequest garageService) {
        return otherServiceBridgeRepository.save ( mapToEntity ( garageService ) );
    }

    public Optional<OtherServiceBridge> getOtherServiceBridgeById(Integer id) {
        return otherServiceBridgeRepository.findById ( id );
    }

    public OtherServiceBridge updateOtherServiceBridge(Integer id, OtherServiceBridge updated) {
        return otherServiceBridgeRepository.findById ( id ).map ( existing -> {
            existing.setIsActive ( updated.getIsActive ( ) );
            existing.setAvailableService ( updated.getAvailableService ( ) );
            existing.setOtherServiceGarage ( updated.getOtherServiceGarage ( ) );
            return otherServiceBridgeRepository.save ( existing );
        } ).orElseThrow ( () -> new RuntimeException ( "Other service not found with ID: " + id ) );
    }

    public void deleteOtherServiceBridge(Integer id) {
        otherServiceBridgeRepository.deleteById ( id );
    }

    public List<OtherServiceBridge> getAllOtherServiceBridges() {
        return otherServiceBridgeRepository.findAll ( );
    }

    public List<OtherServiceBridge> getByOtherServiceId(Integer garageId) {
        return otherServiceBridgeRepository.findByOtherServiceGarageId ( garageId );
    }
}
