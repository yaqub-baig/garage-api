package com.service.garage.apis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.CommunicationPreference;

@Repository
public interface CommunicationPreferenceRepository extends JpaRepository<CommunicationPreference, Integer> {
	Optional<CommunicationPreference> findByCustomerId(Integer customerId);

	Optional<CommunicationPreference> findByIdAndCustomerId(Integer id, Integer customerId);

	Optional<CommunicationPreference> findByCustomerIdAndStatus(Integer customerId, String status);

}
