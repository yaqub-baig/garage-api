package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsAvailableService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OsAvailableServiceRepository extends JpaRepository<OsAvailableService, Integer> {
    Optional<OsAvailableService> findByName(String name);
}
