package com.autoadda.apis.garage.repository.os;


import com.autoadda.apis.garage.entity.os.OtherServiceBridge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtherServiceBridgeRepository extends JpaRepository<OtherServiceBridge, Integer> {
    List<OtherServiceBridge> findByOtherServiceGarageId(Integer garageId);
    List<OtherServiceBridge> findByOtherServiceGarageIdAndAvailableServiceId(Integer garageId ,Integer availableServiceId);
}
