package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferralRepository extends JpaRepository<Referral, Integer> {
    Optional<Referral> findByReferralCode(String code);
}
