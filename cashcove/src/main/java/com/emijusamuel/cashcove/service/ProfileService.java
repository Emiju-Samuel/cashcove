package com.emijusamuel.cashcove.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.emijusamuel.cashcove.dto.AuthDTO;
import com.emijusamuel.cashcove.dto.ProfileDTO;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.repo.ProfileRepository;
import com.emijusamuel.cashcove.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ProfileDTO registerProfile(ProfileDTO profileDTO){
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);
        // send activation email
        String activationLink = "http://localhost:8080/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your CashCove Account";
        String body = "Click on the following link to activate your account: " + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO){
        return ProfileEntity.builder()
        .id(profileDTO.getId())
        .fullName(profileDTO.getFullName())
        .email(profileDTO.getEmail())
        .password(passwordEncoder.encode(profileDTO.getPassword()))
        .profileImageUrl(profileDTO.getProfileImageUrl())
        .createdAt(profileDTO.getCreatedAt())
        .updatedAt(profileDTO.getUpdatedAt())
        .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity){
        return ProfileDTO.builder()
        .id(profileEntity.getId())
        .fullName(profileEntity.getFullName())
        .email(profileEntity.getEmail())
        .profileImageUrl(profileEntity.getProfileImageUrl())
        .createdAt(profileEntity.getCreatedAt())
        .updatedAt(profileEntity.getUpdatedAt())
        .build();
    }

    public boolean activateProfile(String activationToken){
        return profileRepository.findByActivationToken(activationToken)
        .map(profile -> {
            profile.setIsActive(true);
            profileRepository.save(profile);
            return true;
        })
        .orElse(false);
    }

    public boolean isAccountActive(String email){
        return profileRepository.findByEmail(email)
        .map(ProfileEntity::getIsActive)
        .orElse(false);
    }

    public ProfileEntity getCurrentProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
        .orElseThrow(()-> new UsernameNotFoundException("Profile not found email:" + authentication.getName()));
    }

    public ProfileDTO getPublicProfile(String email){
        ProfileEntity currentUser = null;
        if(email == null){
            currentUser = getCurrentProfile();
        }else{
            currentUser = profileRepository.findByEmail(email)
            .orElseThrow(()-> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return ProfileDTO.builder()
        .id(currentUser.getId())
        .fullName(currentUser.getFullName())
        .email(currentUser.getEmail())
        .profileImageUrl(currentUser.getProfileImageUrl())
        .createdAt(currentUser.getCreatedAt())
        .updatedAt(currentUser.getUpdatedAt())
        .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            // Generate JWT Token
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                "token", token,
                "user", getPublicProfile(authDTO.getEmail())
            );
        }catch(Exception e){
            throw new RuntimeException("Invalid email or password");
        }
    }

}
