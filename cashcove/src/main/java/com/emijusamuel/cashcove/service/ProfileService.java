package com.emijusamuel.cashcove.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
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
        String htmlBody = buildActivationEmailHTML(newProfile.getFullName(), activationLink);
        emailService.sendHtmlEmail(newProfile.getEmail(), subject, htmlBody);
        return toDTO(newProfile);
    }

    private String buildActivationEmailHTML(String fullName, String activationLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }" +
                "        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 20px; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: 600; }" +
                "        .content { padding: 40px 30px; text-align: center; }" +
                "        .content p { color: #333; font-size: 16px; line-height: 1.6; margin: 15px 0; }" +
                "        .welcome-text { font-size: 18px; color: #667eea; font-weight: 600; }" +
                "        .button-container { margin: 30px 0; }" +
                "        .activation-btn { display: inline-block; padding: 14px 40px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: 600; transition: transform 0.2s, box-shadow 0.2s; }" +
                "        .activation-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4); }" +
                "        .footer { background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eee; }" +
                "        .footer p { color: #666; font-size: 12px; margin: 5px 0; }" +
                "        .link-text { color: #667eea; word-break: break-all; font-size: 12px; margin-top: 15px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h1>Welcome to CashCove!</h1>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <p class=\"welcome-text\">Hello " + fullName + ",</p>" +
                "            <p>Thank you for signing up for CashCove. Your account has been created, but it needs to be activated to get started.</p>" +
                "            <p>Click the button below to activate your account:</p>" +
                "            <div class=\"button-container\">" +
                "                <a href=\"" + activationLink + "\" class=\"activation-btn\">Activate Account</a>" +
                "            </div>" +
                "            <p style=\"color: #999; font-size: 14px; margin-top: 25px;\">Or copy and paste this link in your browser:</p>" +
                "            <p class=\"link-text\">" + activationLink + "</p>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>© 2026 CashCove. All rights reserved.</p>" +
                "            <p>If you didn't create this account, please ignore this email.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
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


    // @Override
    public void sendResetOtp(String email){
        ProfileEntity currentUser = profileRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Profile not found: "+email));

        // Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // calculate expiry time (current time + 10minutes in milliseconds)
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);

        // update the profile/user
        currentUser.setResetOtp(otp);
        currentUser.setResetOtpExpiredAt(expiryTime);

        // save into database
        profileRepository.save(currentUser);

        try{
            // TODO: send the reset otp email
            emailService.sendResetOtpEmail(currentUser.getEmail(), otp);
        }catch(Exception ex){
            ex.printStackTrace();
            System.err.println("Email sending failed: " + ex.getMessage());
            throw new RuntimeException("Unable to send email: " + ex.getMessage(), ex);
        }

    }



    // @Override
    public void resetPassword(String email, String otp, String newPassword){
        ProfileEntity currentUser = profileRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));

        if(currentUser.getResetOtp() == null || !currentUser.getResetOtp().equals(otp)){
            throw new RuntimeException("Invalid OTP");
        }

        if(currentUser.getResetOtpExpiredAt() < System.currentTimeMillis()){
            throw new RuntimeException("OTP expired");
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        currentUser.setResetOtp(null);
        currentUser.setResetOtpExpiredAt(0L);

        profileRepository.save(currentUser);
    }

}
