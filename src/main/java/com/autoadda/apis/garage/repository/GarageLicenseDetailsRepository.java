package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageLicenseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageLicenseDetailsRepository extends JpaRepository<GarageLicenseDetails, Integer> {

    GarageLicenseDetails findByGarageId(Integer garageId);

}
