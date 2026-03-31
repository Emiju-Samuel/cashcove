package com.emijusamuel.cashcove.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ResetPassword {

    private String newPassword;

    private String otp;

    private String email;

}
