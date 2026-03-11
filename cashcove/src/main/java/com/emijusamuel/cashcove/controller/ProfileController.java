package com.emijusamuel.cashcove.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.emijusamuel.cashcove.dto.AuthDTO;
import com.emijusamuel.cashcove.dto.ProfileDTO;
import com.emijusamuel.cashcove.dto.ResetPassword;
import com.emijusamuel.cashcove.service.ProfileService;
import com.emijusamuel.cashcove.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final RateLimitService rateLimitService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> regsiterProfile(@RequestBody ProfileDTO profileDTO){
        ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token){
        boolean isActivated = profileService.activateProfile(token);
        if(isActivated){
            return ResponseEntity.ok("Profile activated successfully");
        } else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDTO authDTO, HttpServletRequest request){
        try{
            // Apply rate limiting for login attempts
            String userKey = "login:user:" + authDTO.getEmail().toLowerCase();
            String ipKey = "ip:" + request.getRemoteAddr();
            
            boolean userAllowed = rateLimitService.tryConsume(userKey, 1);
            boolean ipAllowed = rateLimitService.tryConsume(ipKey, 1);
            
            if (!userAllowed) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "message", "Too many login attempts. Please try again later."
                ));
            }
            
            if (!ipAllowed) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "message", "Too many login attempts from this IP. Please try again later."
                ));
            }
            
            if(!profileService.isAccountActive(authDTO.getEmail())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Account is not active. Please activate your account first."
                ));
            }
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getPublicProfile(){
        ProfileDTO profileDTO = profileService.getPublicProfile(null);
        return ResponseEntity.ok(profileDTO);
    }


    
    @PostMapping("/send-reset-otp")
    public ResponseEntity<?> sendResetOtp(@RequestParam String email, HttpServletRequest request){

        // apply per-username or per-ip bucket to limit abuse
        String userKey;
        if (email != null) {
            userKey = "is-auth:user:" + email.toLowerCase();
        } else {
            userKey = "is-auth:ip:" + request.getRemoteAddr();
        }

        boolean allowed = rateLimitService.tryConsume(userKey, 1);
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try{
            profileService.sendResetOtp(email);
            return ResponseEntity.ok().build();
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    // send the password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPassword request, HttpServletRequest requestObj){

        // apply per-username or per-ip bucket to limit abuse
        String userKey;
        String email = request.getEmail();
        if (email != null) {
            userKey = "is-auth:user:" + email.toLowerCase();
        } else {
            userKey = "is-auth:ip:" + requestObj.getRemoteAddr();
        }

        boolean allowed = rateLimitService.tryConsume(userKey, 1);
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
        }

        try{
            profileService.resetPassword(request.getEmail(), request.getOtp(), request.getNewpassword());
            return ResponseEntity.ok().build();
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
