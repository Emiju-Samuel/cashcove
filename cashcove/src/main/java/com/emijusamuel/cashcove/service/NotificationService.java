package com.emijusamuel.cashcove.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.emijusamuel.cashcove.dto.ExpenseDTO;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.repo.ProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${cashcove.frontend.url}")
    private String frontendUrl;

    // @Scheduled(cron = "0 * * * * *", zone = "UTC")
    // @Scheduled(cron = "0 0 6 * * *", zone = "UTC")
    public void sendDailyIncomeExpenseReminder(){
        log.info("Job started: sendDailyIncomeExpenseReminder");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for(ProfileEntity profile : profiles){
            String htmlBody = buildDailyReminderEmail(profile.getFullName());
            emailService.sendHtmlEmail(profile.getEmail(), "Daily reminder: Add your income and expenses", htmlBody);
        }
        log.info("job completed: sendDailyIncomeExpenseRemider");
    }

    // @Scheduled(cron = "0 0 23 * * *", zone = "UTC")
    public void sendDailyExpenseSummary(){
        log.info("Job started: sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles){
            List<ExpenseDTO> todaysExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());
            if(!todaysExpenses.isEmpty()){
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse:collapse;width:100%'>");
                table.append("<tr style='background-color:#f2f2f2;'><th style='border:1px solid #ddd;padding:8px;'>Name</th><th style='border:1px solid #ddd;padding:8px;'>Amount</th><th style='border:1px solid #ddd; padding:8px;'>Category</th><th style='border:1px solid #ddd; padding:8px;'>Date</th></tr>");
                int i = 1;
                for(ExpenseDTO expense : todaysExpenses){
                    table.append("<tr>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(i++).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getName()).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getAmount()).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A").append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getDate()).append("</td>");
                    table.append("</tr>");
                }
                table.append("</table>");
                String body = "Hi " + profile.getFullName()+" ,<br><br> Here is a summary of your expenses for today: <br><br> " + table + " <br><br> Best regards, CashCove";
                emailService.sendEmail(profile.getEmail(), "Your daily Expense summary", body);
            }
        }
        log.info("job completed: sendDailyExpenseSummary()");
    }

    /**
     * Build attractive HTML email for daily reminder
     */
    private String buildDailyReminderEmail(String fullName) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <style>\n" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }\n" +
                "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden; }\n" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; padding: 40px 20px; text-align: center; }\n" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: 600; }\n" +
                "        .content { padding: 40px 30px; }\n" +
                "        .greeting { font-size: 16px; color: #333333; margin-bottom: 20px; line-height: 1.6; }\n" +
                "        .reminder-section { background-color: #f0f7ff; border-left: 4px solid #667eea; padding: 20px; border-radius: 5px; margin: 25px 0; }\n" +
                "        .reminder-section h2 { margin: 0 0 10px 0; color: #667eea; font-size: 18px; }\n" +
                "        .reminder-section p { margin: 0; color: #555555; line-height: 1.6; }\n" +
                "        .cta-button { display: inline-block; padding: 14px 32px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; text-decoration: none; border-radius: 5px; font-weight: 600; margin: 25px 0; transition: transform 0.2s, box-shadow 0.2s; }\n" +
                "        .cta-button:hover { transform: translateY(-2px); box-shadow: 0 6px 12px rgba(102, 126, 234, 0.4); }\n" +
                "        .benefits { margin: 30px 0; }\n" +
                "        .benefit-item { display: flex; margin: 15px 0; align-items: center; }\n" +
                "        .benefit-icon { font-size: 20px; margin-right: 12px; }\n" +
                "        .benefit-text { color: #555555; font-size: 14px; line-height: 1.5; }\n" +
                "        .footer { background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eeeeee; }\n" +
                "        .footer p { margin: 5px 0; font-size: 12px; color: #888888; }\n" +
                "        .footer a { color: #667eea; text-decoration: none; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='email-container'>\n" +
                "        <div class='header'>\n" +
                "            <h1>💰 CashCove</h1>\n" +
                "            <p style='margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;'>Your Personal Money Manager</p>\n" +
                "        </div>\n" +
                "        <div class='content'>\n" +
                "            <div class='greeting'>\n" +
                "                <p>Hello <strong>" + fullName + "</strong>,</p>\n" +
                "                <p>We hope you're having a great day! 😊</p>\n" +
                "            </div>\n" +
                "            <div class='reminder-section'>\n" +
                "                <h2>📝 Daily Reminder</h2>\n" +
                "                <p>This is a friendly reminder to add your income and expenses for today in CashCove. Keeping your financial records up-to-date helps you stay in control of your money!</p>\n" +
                "            </div>\n" +
                "            <div style='text-align: center;'>\n" +
                "                <a href='" + frontendUrl + "' class='cta-button'>📊 Go to Money Manager</a>\n" +
                "            </div>\n" +
                "            <div class='benefits'>\n" +
                "                <h3 style='color: #333333; margin-bottom: 15px;'>Why update regularly?</h3>\n" +
                "                <div class='benefit-item'>\n" +
                "                    <span class='benefit-icon'>📈</span>\n" +
                "                    <span class='benefit-text'><strong>Track Progress:</strong> Monitor your spending patterns and financial goals</span>\n" +
                "                </div>\n" +
                "                <div class='benefit-item'>\n" +
                "                    <span class='benefit-icon'>🏦</span>\n" +
                "                    <span class='benefit-text'><strong>Better Insights:</strong> Get accurate financial summaries and analytics</span>\n" +
                "                </div>\n" +
                "                <div class='benefit-item'>\n" +
                "                    <span class='benefit-icon'>💡</span>\n" +
                "                    <span class='benefit-text'><strong>Smart Decisions:</strong> Make informed financial decisions based on real data</span>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div class='footer'>\n" +
                "            <p><strong>CashCove Team</strong></p>\n" +
                "            <p>Managing your finances has never been easier</p>\n" +
                "            <p style='margin-top: 15px; font-size: 11px; color: #aaaaaa;'>This is an automated reminder from CashCove. Please do not reply to this email.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
