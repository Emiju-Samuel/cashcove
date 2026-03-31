package com.emijusamuel.cashcove.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import org.springframework.core.io.ByteArrayResource;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    /**
     * Send plain text email
     */
    public void sendEmail(String to, String subject, String body ){

        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * Send HTML email with styling
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String filename) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment(filename, new ByteArrayResource(attachment));
        javaMailSender.send(message);
    }

    /**
     * Send HTML email with attachment
     */
    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlBody, byte[] attachment, String filename) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.addAttachment(filename, new ByteArrayResource(attachment));
        javaMailSender.send(message);
    }


    public void sendResetOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Password Reset OTP");

            String htmlContent = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }" +
                ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }" +
                ".header { background: linear-gradient(135deg, #FF5722 0%, #FF7043 100%); color: white; padding: 30px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 28px; font-weight: 600; }" +
                ".content { padding: 30px; }" +
                ".content p { margin: 15px 0; line-height: 1.6; color: #333; }" +
                ".otp-box { background-color: #f0f0f0; border-left: 4px solid #FF5722; padding: 20px; border-radius: 4px; text-align: center; margin: 25px 0; }" +
                ".otp-code { font-size: 32px; font-weight: bold; color: #FF5722; letter-spacing: 4px; font-family: 'Courier New', monospace; }" +
                ".warning { background-color: #fff3cd; border: 1px solid #ffc107; color: #856404; padding: 12px; border-radius: 4px; font-size: 14px; margin: 20px 0; }" +
                ".footer { background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eee; font-size: 14px; color: #666; }" +
                ".footer p { margin: 8px 0; }" +
                ".divider { height: 1px; background-color: #eee; margin: 20px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Password Reset Request</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Dear User,</p>" +
                "<p>You have requested to reset your password. Your OTP code is:</p>" +
                "<div class='otp-box'>" +
                "<div class='otp-code'>" + otp + "</div>" +
                "</div>" +
                "<div class='warning'>" +
                "<strong>⚠️ Important:</strong> This OTP expires in 10 minutes. Please use it immediately to proceed with changing your password." +
                "</div>" +
                "<p>If you did not request a password reset, please ignore this email and ensure your account is secure.</p>" +
                "<div class='divider'></div>" +
                "<p style='font-size: 14px; color: #999;'>Do not share this OTP with anyone. Our team will never ask for your OTP.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p><strong>CashCove</strong></p>" +
                "<p>© 2026 All rights reserved</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset OTP email", e);
        }
    }

}
