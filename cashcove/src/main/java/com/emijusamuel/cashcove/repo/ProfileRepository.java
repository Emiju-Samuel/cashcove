package com.emijusamuel.cashcove.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emijusamuel.cashcove.entity.ProfileEntity;

public interface ProfileRepository extends JpaRepository< ProfileEntity, Long >{

    // select * from thbl_profiles where email = ?;
    Optional<ProfileEntity>findByEmail(String email);

}
