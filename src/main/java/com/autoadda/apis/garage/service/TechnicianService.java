package com.autoadda.apis.garage.service;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.GarageTechnician;
import com.autoadda.apis.garage.repository.GarageTechnicianRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TechnicianService {
    private final GarageTechnicianRepository technicianRepo;

    public GarageTechnician getTechnician(Integer technicianId) {
        return technicianRepo.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
    }
}
