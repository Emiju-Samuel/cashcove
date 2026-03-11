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

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<h2 style='color: #FF5722;'>Password Reset Request</h2>" +
                "<p>Dear User,</p>" +
                "<p>You have requested to reset your password. Your OTP is:</p>" +
                "<div style='background-color: #f0f0f0; padding: 10px; border-radius: 5px; font-size: 18px; font-weight: bold; text-align: center;'>" + otp + "</div>" +
                "<p>This OTP expires in 10 minutes. Please use it to proceed with changing your password.</p>" +
                "<p>If you did not request this, please ignore this email.</p>" +
                "<br>" +
                "<p>Best regards,</p>" +
                "<p><strong>Emiju Samuel</strong><br>Software Engineer</p>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset OTP email", e);
        }
    }

}
