/**
 * 
 */
package com.service.garage.apis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.Model;

@Repository
public interface ModelRepository extends JpaRepository<Model, Integer> {
}
