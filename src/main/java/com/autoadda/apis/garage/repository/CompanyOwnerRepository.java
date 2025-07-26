package com.autoadda.apis.garage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.CompanyOwner;

@Repository
public interface CompanyOwnerRepository extends JpaRepository<CompanyOwner, Integer> {
	Optional<CompanyOwner> findByCompanyName(String companyName);

	Optional<CompanyOwner> findByEmail(String email);

	Optional<CompanyOwner> findByPhone(String mobileNumber);

}
