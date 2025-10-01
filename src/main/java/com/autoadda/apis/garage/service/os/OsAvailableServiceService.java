package com.autoadda.apis.garage.service.os;

import com.autoadda.apis.garage.entity.os.OsAvailableService;
import com.autoadda.apis.garage.repository.os.OsAvailableServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OsAvailableServiceService {

    private final OsAvailableServiceRepository repository;

    public OsAvailableServiceService(OsAvailableServiceRepository repository) {
        this.repository = repository;
    }

    // Get all
    public List<OsAvailableService> getAll() {
        return repository.findAll();
    }

    // Find by id
    public OsAvailableService getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
    }

    // Create
    public OsAvailableService create(OsAvailableService newService) {
        return repository.save(newService);
    }

    // Update
    public OsAvailableService update(Integer id, OsAvailableService updatedService) {
        OsAvailableService existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));

        existing.setName(updatedService.getName());
        existing.setDescription(updatedService.getDescription());

        return repository.save(existing);
    }
}
