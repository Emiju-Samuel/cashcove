package com.emijusamuel.cashcove.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale.Category;
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


    // ─── 1. Read ────────────────────────────────────────────────────────────────
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
    @Scheduled(cron = "0 5 0 * * ?")   // every day at 00:05
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
                    String subject = "Subscription Renewal Reminder: " + subscription.getSubscriptionName();
                    String body = "Hi " + subscription.getProfile().getFullName() + ",\n\nYour subscription for " + 
                                  subscription.getSubscriptionName() + " is due for renewal on " + subscription.getNextRenewalDate() + ".";
                    emailService.sendEmail(subscription.getProfile().getEmail(), subject, body);
                    log.info("Reminder sent for subscription {} ({}) to {}", 
                             subscription.getId(), subscription.getSubscriptionName(), subscription.getProfile().getEmail());
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

}
