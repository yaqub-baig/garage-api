package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GaragePendingActions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GaragePendingActionsRepository extends JpaRepository<GaragePendingActions, Integer> {

    GaragePendingActions findByGarageId(Integer garageId);

    List<GaragePendingActions> findByLicenseUpdateIndAndTechUpdateIndAndOwnerUpdateIndAndEmailVerifyIndAndSearchInd(String lcnInd, String techInd, String ownInd, String emInd, String srInd);

    List<GaragePendingActions> findByLicenseUpdateIndAndTechUpdateIndAndOwnerUpdateIndAndEmailVerifyIndAndServiceUpdateIndAndSearchInd(
            String lcnInd, String techInd, String ownInd, String emInd, String serInd, String srInd);

    List<GaragePendingActions> findByLicenseUpdateIndAndTechUpdateIndAndEmailVerifyIndAndServiceUpdateIndAndSearchInd(
            String lcnInd, String techInd, String emInd, String serInd, String srInd);
}
