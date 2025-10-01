package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.CompanyOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyOwnerRepository extends JpaRepository<CompanyOwner, Integer> {
    Optional<CompanyOwner> findByCompanyName(String companyName);

    Optional<CompanyOwner> findByEmail(String email);

    Optional<CompanyOwner> findByMobileNumber(String mobileNumber);

}
