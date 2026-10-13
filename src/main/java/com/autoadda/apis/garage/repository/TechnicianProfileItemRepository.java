package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.TechnicianProfileItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * Shared queries for the list sections of the technician CV profile.
 */
@NoRepositoryBean
public interface TechnicianProfileItemRepository<E extends TechnicianProfileItem> extends JpaRepository<E, Integer> {

    List<E> findByTechnicianIdOrderByDisplayOrderAsc(Integer technicianId);

    long countByTechnicianId(Integer technicianId);
}
