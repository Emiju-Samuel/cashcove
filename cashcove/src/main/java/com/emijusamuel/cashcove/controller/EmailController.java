package com.emijusamuel.cashcove.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.service.EmailService;
import com.emijusamuel.cashcove.service.ExcelService;
import com.emijusamuel.cashcove.service.IncomeService;
import com.emijusamuel.cashcove.service.ProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final ExcelService excelService;
    private final IncomeService incomeService;
    private final EmailService emailService;
    private final ProfileService profileService;


    @GetMapping("/income-excel")
    public ResponseEntity<Void> emailIncomeExcel() throws IOException, MessagingException{
        ProfileEntity profile = profileService.getCurrentProfile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeIncomesToExcel(baos, incomeService.getCurrentMonthIncomesForCurrentUser());
        emailService.sendEmailWithAttachment(profile.getEmail(),
            "Your Income Excel Report",
            "Please find attached, your income report",
            baos.toByteArray(),
            "income.xlsx");
        return ResponseEntity.ok(null);
    }

    

}
