package com.emijusamuel.cashcove.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.emijusamuel.cashcove.dto.SubscriptionDTO;
import com.emijusamuel.cashcove.entity.CategoryEntity;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.entity.SubscriptionEntity;
import com.emijusamuel.cashcove.enums.SubscriptionStatus;
import com.emijusamuel.cashcove.repo.CategoryRepository;
import com.emijusamuel.cashcove.repo.SubscriptionRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;


    // Read
    @Transactional(readOnly = true)
    public SubscriptionDTO getSubscription(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SubscriptionEntity subscription = subscriptionRepository.findByIdAndProfileAndDeletedFalse(id, profile)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        return toDTO(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getMySubscriptions() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findAllByProfileAndDeletedFalse(profile);
        return subscriptions.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getUpcoming(int daysAhead) {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate threshold = LocalDate.now().plusDays(daysAhead);
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findByProfileAndNextRenewalDateBetween(
                profile, LocalDate.now(), threshold);
        return subscriptions.stream().map(this::toDTO).toList();
    }

    // Add a new subscription to the database
    public SubscriptionDTO addSubscription(SubscriptionDTO dto){
        ProfileEntity profile = profileService.getCurrentProfile();
        if(subscriptionRepository.existsByProfileAndSubscriptionNameAndDeletedFalse(profile, dto.getSubscriptionName())){
            throw new RuntimeException("Subscription with this name already exists");
        }
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
        .orElseThrow(()->new RuntimeException("Category not found"));
        SubscriptionEntity newSubscription = toEntity(dto, profile, category);
        newSubscription = subscriptionRepository.save(newSubscription);
        return toDTO(newSubscription);
    }

    // Update
    public SubscriptionDTO update(Long id, SubscriptionDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SubscriptionEntity existingSubscription = subscriptionRepository.findByIdAndProfileAndDeletedFalse(id, profile)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (dto.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existingSubscription.setCategory(category);
        }

        existingSubscription.setIcon(dto.getIcon());
        existingSubscription.setSubscriptionName(dto.getSubscriptionName());
        existingSubscription.setAmount(dto.getAmount());
        existingSubscription.setSubscriptionFrequency(dto.getFrequency());
        existingSubscription.setStartDate(dto.getStartDate());
        existingSubscription.setNextRenewalDate(calculateNextRenewal(dto));
        existingSubscription.setReminderDaysBefore(dto.getReminderDaysBefore());

        SubscriptionEntity saved = subscriptionRepository.save(existingSubscription);
        return toDTO(saved);
    }


    // Soft Delete
    @Transactional
    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SubscriptionEntity subscription = subscriptionRepository.findByIdAndProfileAndDeletedFalse(id, profile)
                .orElseThrow(() -> new RuntimeException("Not found"));
        subscription.setDeleted(true);
        subscriptionRepository.save(subscription);
    }

    // Change subscription status (ACTIVE, PAUSED, CANCELLED)
    @Transactional
    public SubscriptionDTO changeStatus(Long id, String status) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SubscriptionEntity subscription = subscriptionRepository.findByIdAndProfileAndDeletedFalse(id, profile)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        try {
            SubscriptionStatus newStatus = SubscriptionStatus.valueOf(status.toUpperCase());
            subscription.setStatus(newStatus);
            subscription = subscriptionRepository.save(subscription);
            return toDTO(subscription);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    // Background job
    @Transactional
    // @Scheduled(cron = "0 * * * * *", zone = "UTC")
    // @Scheduled(cron = "0 5 0 * * ?")   every day at 00:05
    public void sendDailyRenewalReminders() {
        LocalDate today = LocalDate.now();
        
        // NEW: Query for subscriptions up to 30 days ahead to respect reminderDaysBefore preference
        // This covers most reasonable reminder preferences (1, 3, 7, 14, 30 days)
        LocalDate maxThreshold = today.plusDays(30);
        List<SubscriptionEntity> potentialDue = subscriptionRepository.findAllActiveByNextRenewalDateBetween(today, maxThreshold);
        
        potentialDue.forEach(subscription -> {
            try {
                // FIX: Now use the isDueForReminder() method to respect reminderDaysBefore!
                // This checks if today equals (nextRenewalDate - reminderDaysBefore)
                if (subscription.isDueForReminder(today)) {
                    String subject = "Action Required: " + subscription.getSubscriptionName() + " Renewal Due";
                    String body = buildSubscriptionReminderEmail(
                        subscription.getProfile().getFullName(),
                        subscription.getSubscriptionName(),
                        subscription.getNextRenewalDate(),
                        subscription.getAmount()
                    );
                    emailService.sendHtmlEmail(subscription.getProfile().getEmail(), subject, body);
                    log.info("Reminder sent for subscription {} ({}) to {}", 
                             subscription.getId(), subscription.getSubscriptionName(), subscription.getProfile().getEmail());
                    
                    // Add delay to respect email service rate limits (500ms between emails)
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send reminder for subscription {}", subscription.getId(), e);
            }
        });
        
        log.info("Daily renewal reminders job completed. Checked {} subscriptions.", potentialDue.size());
    }

    // Helper
    private void sendRemindersForUser(ProfileEntity profile, LocalDate today) {
        List<SubscriptionEntity> due = subscriptionRepository.findActiveDueSoon(
                profile, today, today.plusDays(7)); // example 7-day window
        due.forEach(sub -> {
            String subject = "Upcoming Renewal: " + sub.getSubscriptionName();
            String body = "Hello " + profile.getFullName() + ",\n\nYour " + sub.getSubscriptionName() + 
                          " subscription is renewing soon on " + sub.getNextRenewalDate() + ".";
            emailService.sendEmail(profile.getEmail(), subject, body);
        });
    }


    // delete subscription by id for current user
    public void deleteSubscription(Long subscriptionId){
        ProfileEntity profile = profileService.getCurrentProfile();
        SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
        .orElseThrow(()-> new RuntimeException("Subscription not found"));
        if(!subscription.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this income");
        }
        subscriptionRepository.delete(subscription);
    }


    // convert from dto to entity
    private SubscriptionEntity toEntity(SubscriptionDTO dto, ProfileEntity profile, CategoryEntity category){
        return SubscriptionEntity.builder()
        .subscriptionName(dto.getSubscriptionName())
        .icon(dto.getIcon())
        .amount(dto.getAmount())
        .startDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now())
        .subscriptionFrequency(dto.getFrequency())
        .nextRenewalDate(dto.getStartDate() != null ? calculateNextRenewal(dto) : null)
        .reminderDaysBefore(dto.getReminderDaysBefore() != null ? dto.getReminderDaysBefore() : 1)
        .profile(profile)
        .category(category)
        .status(SubscriptionStatus.ACTIVE)
        .build();
    }

    // convert from entity to dto
    private SubscriptionDTO toDTO(SubscriptionEntity entity){
        return SubscriptionDTO.builder()
        .id(entity.getId())
        .subscriptionName(entity.getSubscriptionName())
        .icon(entity.getIcon())
        .categoryId(entity.getCategory() != null ? entity.getCategory().getId(): null)
        .categoryName(entity.getCategory() != null ? entity.getCategory().getName(): "N/A")
        .amount(entity.getAmount())
        .startDate(entity.getStartDate())
        .nextRenewalDate(entity.getNextRenewalDate())
        .frequency(entity.getSubscriptionFrequency())
        .reminderDaysBefore(entity.getReminderDaysBefore())
        .subscriptionStatus(entity.getStatus())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();

    }


    private LocalDate calculateNextRenewal(SubscriptionDTO dto) {
        if (dto.getStartDate() == null || dto.getFrequency() == null) return null;
        return switch (dto.getFrequency()) {
            case MONTHLY    -> dto.getStartDate().plusMonths(1);
            case QUARTERLY  -> dto.getStartDate().plusMonths(3);
            case SEMI_ANNUAL-> dto.getStartDate().plusMonths(6);
            case YEARLY     -> dto.getStartDate().plusMonths(12);
            default         -> dto.getStartDate(); // or throw
        };
    }

    private String buildSubscriptionReminderEmail(String fullName, String subscriptionName, LocalDate renewalDate, BigDecimal amount) {
        String formattedAmount = String.format("%.2f", amount);
        
        return "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>" +
            "* { margin: 0; padding: 0; box-sizing: border-box; }" +
            "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); }" +
            ".container { max-width: 600px; margin: 20px auto; background: white; border-radius: 12px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); overflow: hidden; }" +
            ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px 20px; text-align: center; color: white; }" +
            ".header h1 { font-size: 24px; margin-bottom: 8px; font-weight: 600; }" +
            ".header p { font-size: 14px; opacity: 0.9; }" +
            ".content { padding: 40px 30px; }" +
            ".alert-box { background: linear-gradient(135deg, #fff5e6 0%, #ffe6cc 100%); border-left: 4px solid #ff6b35; padding: 20px; border-radius: 8px; margin-bottom: 30px; }" +
            ".alert-box h2 { color: #ff6b35; font-size: 18px; margin-bottom: 8px; }" +
            ".alert-box p { color: #cc5200; font-size: 14px; font-weight: 500; }" +
            ".greeting { font-size: 16px; color: #2d3748; margin-bottom: 25px; line-height: 1.6; }" +
            ".details-card { background: #f8fafc; border: 2px solid #e2e8f0; border-radius: 8px; padding: 25px; margin: 25px 0; }" +
            ".detail-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #e2e8f0; }" +
            ".detail-row:last-child { border-bottom: none; }" +
            ".detail-label { font-size: 13px; color: #718096; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }" +
            ".detail-value { font-size: 16px; color: #2d3748; font-weight: 600; }" +
            ".renewal-date { color: #ff6b35; font-size: 20px; }" +
            ".cta-button { background: linear-gradient(135deg, #ff6b35 0%, #ff4757 100%); color: white; text-align: center; padding: 16px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; margin: 30px 0; display: inline-block; width: 100%; box-sizing: border-box; cursor: pointer; font-size: 16px; transition: transform 0.2s; }" +
            ".cta-button:hover { transform: translateY(-2px); }" +
            ".footer-text { font-size: 13px; color: #718096; line-height: 1.6; margin: 30px 0 0 0; padding-top: 20px; border-top: 1px solid #e2e8f0; }" +
            ".urgency-badge { display: inline-block; background: #ff6b35; color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; margin-left: 10px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>🔔 Subscription Renewal Alert</h1>" +
            "<p>Action required to maintain your subscription</p>" +
            "</div>" +
            "<div class='content'>" +
            "<p class='greeting'>Hi <strong>" + fullName + "</strong>,</p>" +
            "<div class='alert-box'>" +
            "<h2>⚠️ Time to Renew Your Subscription</h2>" +
            "<p>Your subscription renewal is coming up soon. Don't let your service lapse!</p>" +
            "</div>" +
            "<div class='details-card'>" +
            "<div class='detail-row'>" +
            "<span class='detail-label'>💳 Service</span>" +
            "<span class='detail-value'>" + subscriptionName + "</span>" +
            "</div>" +
            "<div class='detail-row'>" +
            "<span class='detail-label'>📅 Renewal Date <span class='urgency-badge'>URGENT</span></span>" +
            "<span class='detail-value renewal-date'>" + renewalDate.toString() + "</span>" +
            "</div>" +
            "<div class='detail-row'>" +
            "<span class='detail-label'>💰 Amount Due</span>" +
            "<span class='detail-value'>$" + formattedAmount + "</span>" +
            "</div>" +
            "</div>" +
            "<p style='color: #2d3748; font-size: 14px; line-height: 1.8; margin: 20px 0;'>" +
            "Your <strong>" + subscriptionName + "</strong> subscription is set to renew on " +
            "<strong style='color: #ff6b35; font-size: 16px;'>" + renewalDate.toString() + "</strong>." +
            "<br><br>" +
            "Please ensure your payment method is up to date to avoid any service interruptions. " +
            "Renew now to keep enjoying uninterrupted service!" +
            "</p>" +
            "<a href='#' class='cta-button'>Renew Now</a>" +
            "<p class='footer-text'>" +
            "<strong>Why are you receiving this?</strong><br>" +
            "We send you reminders before your subscription renews so you're always in control. " +
            "If you have questions about your subscription, please contact our support team." +
            "<br><br>" +
            "<em>CashCove Financial Management</em>" +
            "</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }

}
