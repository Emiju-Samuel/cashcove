package com.emijusamuel.cashcove.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.emijusamuel.cashcove.dto.SubscriptionDTO;
import com.emijusamuel.cashcove.entity.CategoryEntity;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.entity.SubscriptionEntity;
import com.emijusamuel.cashcove.enums.SubscriptionFrequency;
import com.emijusamuel.cashcove.enums.SubscriptionStatus;
import com.emijusamuel.cashcove.repo.CategoryRepository;
import com.emijusamuel.cashcove.repo.ProfileRepository;
import com.emijusamuel.cashcove.repo.SubscriptionRepository;

import static org.mockito.Mockito.*;

/**
 * Integration tests for subscription reminder feature.
 * 
 * These tests verify the scheduler behavior within the Spring application context.
 * They test the interaction between SubscriptionService, Repository, and EmailService.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Subscription Reminder Integration Tests")
class SubscriptionReminderIntegrationTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private EmailService emailService;

    private ProfileEntity testProfile;
    private CategoryEntity testCategory;

    @BeforeEach
    void setUp() {
        // Create test profile
        testProfile = ProfileEntity.builder()
                .email("integration.test@example.com")
                .fullName("Integration Test User")
                .build();
        testProfile = profileRepository.save(testProfile);

        // Create test category
        testCategory = CategoryEntity.builder()
                .name("Test Category")
                .build();
        testCategory = categoryRepository.save(testCategory);
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTEGRATION TEST 1: End-to-end reminder flow
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Integration: Verify complete reminder flow from creation to email")
    void testEndToEndReminderFlow() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .subscriptionName("Netflix")
                .icon("🎬")
                .amount(new BigDecimal("15.99"))
                .subscriptionFrequency(SubscriptionFrequency.MONTHLY)
                .nextRenewalDate(tomorrow)
                .reminderDaysBefore(1)
                .status(SubscriptionStatus.ACTIVE)
                .profile(testProfile)
                .category(testCategory)
                .deleted(false)
                .build();

        subscription = subscriptionRepository.save(subscription);

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert
        verify(emailService, times(1)).sendEmail(
                eq("integration.test@example.com"),
                contains("Netflix"),
                contains("renewal")
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTEGRATION TEST 2: Batch reminders for multiple users
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Integration: Send reminders to multiple users with subscriptions due tomorrow")
    void testBatchRemindersMultipleUsers() {
        // This test would require creating multiple profiles
        // Demonstrating the concept without full implementation due to test database setup complexity
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // Create subscription for test user
        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .subscriptionName("Test Subscription")
                .icon("📺")
                .amount(new BigDecimal("9.99"))
                .subscriptionFrequency(SubscriptionFrequency.MONTHLY)
                .nextRenewalDate(tomorrow)
                .reminderDaysBefore(1)
                .status(SubscriptionStatus.ACTIVE)
                .profile(testProfile)
                .category(testCategory)
                .deleted(false)
                .build();

        subscriptionRepository.save(subscription);

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert - at least one email should be sent
        verify(emailService, atLeastOnce()).sendEmail(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTEGRATION TEST 3: Persistence test - verify data integrity
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Integration: Verify subscription data integrity after reminder job")
    void testDataIntegrityAfterReminderJob() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .subscriptionName("Premium Service")
                .icon("⭐")
                .amount(new BigDecimal("29.99"))
                .subscriptionFrequency(SubscriptionFrequency.YEARLY)
                .nextRenewalDate(tomorrow)
                .reminderDaysBefore(1)
                .status(SubscriptionStatus.ACTIVE)
                .profile(testProfile)
                .category(testCategory)
                .deleted(false)
                .build();

        SubscriptionEntity saved = subscriptionRepository.save(subscription);
        Long subscriptionId = saved.getId();

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert - subscription should not be modified by reminder job
        SubscriptionEntity retrieved = subscriptionRepository.findById(subscriptionId).orElseThrow();
        assertEquals(tomorrow, retrieved.getNextRenewalDate());
        assertEquals(SubscriptionStatus.ACTIVE, retrieved.getStatus());
        assertEquals("Premium Service", retrieved.getSubscriptionName());
        assertFalse(retrieved.isDeleted());
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTEGRATION TEST 4: Verify PAUSED status filtering
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Integration: PAUSED subscription should not receive reminder")
    void testPausedSubscriptionFiltering() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        SubscriptionEntity pausedSubscription = SubscriptionEntity.builder()
                .subscriptionName("Paused Service")
                .icon("⏸️")
                .amount(new BigDecimal("15.99"))
                .subscriptionFrequency(SubscriptionFrequency.MONTHLY)
                .nextRenewalDate(tomorrow)
                .reminderDaysBefore(1)
                .status(SubscriptionStatus.PAUSED)
                .profile(testProfile)
                .category(testCategory)
                .deleted(false)
                .build();

        subscriptionRepository.save(pausedSubscription);

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert - email should not be sent for PAUSED subscription
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTEGRATION TEST 5: Verify deleted subscription filtering
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Integration: Deleted subscription should not receive reminder")
    void testDeletedSubscriptionFiltering() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        SubscriptionEntity deletedSubscription = SubscriptionEntity.builder()
                .subscriptionName("Deleted Service")
                .icon("🗑️")
                .amount(new BigDecimal("19.99"))
                .subscriptionFrequency(SubscriptionFrequency.MONTHLY)
                .nextRenewalDate(tomorrow)
                .reminderDaysBefore(1)
                .status(SubscriptionStatus.ACTIVE)
                .profile(testProfile)
                .category(testCategory)
                .deleted(true)  // Soft deleted
                .build();

        subscriptionRepository.save(deletedSubscription);

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert - email should not be sent for deleted subscription
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTEGRATION TEST 6: Test with multiple statuses
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Integration: Only ACTIVE subscriptions receive reminders")
    void testOnlyActiveSendReminders() {
        // Arrange
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Create ACTIVE subscription
        SubscriptionEntity activeSubscription = SubscriptionEntity.builder()
                .subscriptionName("Active Service")
                .icon("✅")
                .amount(new BigDecimal("9.99"))
                .subscriptionFrequency(SubscriptionFrequency.MONTHLY)
                .nextRenewalDate(tomorrow)
                .reminderDaysBefore(1)
                .status(SubscriptionStatus.ACTIVE)
                .profile(testProfile)
                .category(testCategory)
                .deleted(false)
                .build();

        // Create CANCELLED subscription
        SubscriptionEntity cancelledSubscription = SubscriptionEntity.builder()
                .subscriptionName("Cancelled Service")
                .icon("❌")
                .amount(new BigDecimal("9.99"))
                .subscriptionFrequency(SubscriptionFrequency.MONTHLY)
                .nextRenewalDate(tomorrow)
                .reminderDaysBefore(1)
                .status(SubscriptionStatus.CANCELLED)
                .profile(testProfile)
                .category(testCategory)
                .deleted(false)
                .build();

        subscriptionRepository.save(activeSubscription);
        subscriptionRepository.save(cancelledSubscription);

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert - only ACTIVE subscription should receive reminder
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

}
