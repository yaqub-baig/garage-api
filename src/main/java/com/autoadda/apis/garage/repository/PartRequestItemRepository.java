package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.PartRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRequestItemRepository extends JpaRepository<PartRequestItem, Integer> {

    List<PartRequestItem> findByPartRequestId(Integer partRequestId);
}
