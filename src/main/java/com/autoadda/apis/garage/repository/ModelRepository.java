/**
 *
 */
package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, Integer> {
}
