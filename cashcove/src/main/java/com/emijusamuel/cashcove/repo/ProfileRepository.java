package com.emijusamuel.cashcove.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emijusamuel.cashcove.entity.ProfileEntity;

public interface ProfileRepository extends JpaRepository< ProfileEntity, Long >{

    // select * from tbl_profiles where email = ?;
    Optional<ProfileEntity>findByEmail(String email);

    // select * from tbl_profiles where activationToken = ?;
    Optional<ProfileEntity> findByActivationToken(String activationToken);

}
