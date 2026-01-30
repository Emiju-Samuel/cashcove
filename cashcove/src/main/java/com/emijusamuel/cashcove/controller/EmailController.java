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
import com.emijusamuel.cashcove.service.ExpenseService;
import com.emijusamuel.cashcove.service.IncomeService;
import com.emijusamuel.cashcove.service.ProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final ExcelService excelService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final EmailService emailService;
    private final ProfileService profileService;


    @GetMapping("/income-excel")
    public ResponseEntity<Void> emailIncomeExcel() throws IOException, MessagingException{
        ProfileEntity profile = profileService.getCurrentProfile();
        var incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeIncomesToExcel(baos, incomes);
        String htmlTable = excelService.generateIncomesHtmlTable(incomes);
        String htmlBody = "Please find attached, your income report.<br><br>" + htmlTable;
        emailService.sendHtmlEmailWithAttachment(profile.getEmail(),
            "Your Income Report",
            htmlBody,
            baos.toByteArray(),
            "income.xlsx");
        return ResponseEntity.ok(null);
    }

    @GetMapping("/expense-excel")
    public ResponseEntity<Void> emailExpenseExcel() throws IOException, MessagingException{
        ProfileEntity profile = profileService.getCurrentProfile();
        var expenses = expenseService.getCurrentMonthExpensesForCurrentUser();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeExpensesToExcel(baos, expenses);
        String htmlTable = excelService.generateExpensesHtmlTable(expenses);
        String htmlBody = "Please find attached, your expense report.<br><br>" + htmlTable;
        emailService.sendHtmlEmailWithAttachment(profile.getEmail(),
            "Your Expense Report",
            htmlBody,
            baos.toByteArray(),
            "expense.xlsx");
        return ResponseEntity.ok(null);
    }

    

}
