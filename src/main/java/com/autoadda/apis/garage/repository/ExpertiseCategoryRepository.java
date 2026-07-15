package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.ExpertiseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpertiseCategoryRepository extends JpaRepository<ExpertiseCategory, Integer> {

    Optional<ExpertiseCategory> findByNameIgnoreCase(String name);

    List<ExpertiseCategory> findByIsActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}
