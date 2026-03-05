package com.emijusamuel.cashcove.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.emijusamuel.cashcove.dto.SubscriptionDTO;
import com.emijusamuel.cashcove.entity.CategoryEntity;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.entity.SubscriptionEntity;
import com.emijusamuel.cashcove.enums.SubscriptionFrequency;
import com.emijusamuel.cashcove.enums.SubscriptionStatus;
import com.emijusamuel.cashcove.repo.CategoryRepository;
import com.emijusamuel.cashcove.repo.SubscriptionRepository;

/**
 * Test suite for subscription renewal reminder feature.
 * 
 * Tests verify that:
 * 1. Emails are sent 1 day before subscription renewal
 * 2. Emails are only sent for ACTIVE subscriptions
 * 3. Emails respect the reminderDaysBefore preference
 * 4. The scheduler correctly identifies subscriptions due for reminder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription Reminder Service Tests")
class SubscriptionReminderServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private ProfileEntity testProfile;
    private CategoryEntity testCategory;
    private SubscriptionEntity testSubscription;

    @BeforeEach
    void setUp() {
        // Setup test data
        testProfile = ProfileEntity.builder()
                .id(1L)
                .email("user@example.com")
                .fullName("John Doe")
                .build();

        testCategory = CategoryEntity.builder()
                .id(1L)
                .name("Streaming Services")
                .build();

        testSubscription = SubscriptionEntity.builder()
                .id(1L)
                .subscriptionName("Netflix")
                .icon("🎬")
                .amount(new BigDecimal("15.99"))
                .subscriptionFrequency(SubscriptionFrequency.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .profile(testProfile)
                .category(testCategory)
                .reminderDaysBefore(1)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 1: Email sent 1 day before renewal
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should send reminder email when renewal date is tomorrow")
    void testReminderEmailSentOneDayBefore() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate nextRenewalDate = today.plusDays(1); // Renewal is tomorrow
        testSubscription.setNextRenewalDate(nextRenewalDate);

        when(subscriptionRepository.findAllActiveForRenewalDate(nextRenewalDate))
                .thenReturn(Arrays.asList(testSubscription));

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(1)).sendEmail(toCaptor.capture(), subjectCaptor.capture(), bodyCaptor.capture());

        assertEquals("user@example.com", toCaptor.getValue());
        assertTrue(subjectCaptor.getValue().contains("Netflix"));
        assertTrue(bodyCaptor.getValue().contains("renewal"));
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 2: No email sent if renewal is not tomorrow
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should NOT send reminder when renewal date is not tomorrow")
    void testNoReminderEmailWhenRenewalIsNotTomorrow() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate renewalInThreeDays = today.plusDays(3);
        testSubscription.setNextRenewalDate(renewalInThreeDays);

        when(subscriptionRepository.findAllActiveForRenewalDate(today.plusDays(1)))
                .thenReturn(Collections.emptyList());

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 3: Email NOT sent for PAUSED subscriptions
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should NOT send reminder for PAUSED subscriptions")
    void testNoReminderEmailForPausedSubscription() {
        // Arrange
        LocalDate today = LocalDate.now();
        testSubscription.setStatus(SubscriptionStatus.PAUSED);
        testSubscription.setNextRenewalDate(today.plusDays(1));

        when(subscriptionRepository.findAllActiveForRenewalDate(today.plusDays(1)))
                .thenReturn(Collections.emptyList()); // Query doesn't return PAUSED subscriptions

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 4: Email NOT sent for CANCELLED subscriptions
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should NOT send reminder for CANCELLED subscriptions")
    void testNoReminderEmailForCancelledSubscription() {
        // Arrange
        LocalDate today = LocalDate.now();
        testSubscription.setStatus(SubscriptionStatus.CANCELLED);
        testSubscription.setNextRenewalDate(today.plusDays(1));

        when(subscriptionRepository.findAllActiveForRenewalDate(today.plusDays(1)))
                .thenReturn(Collections.emptyList());

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 5: Email NOT sent for soft-deleted subscriptions
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should NOT send reminder for deleted subscriptions")
    void testNoReminderEmailForDeletedSubscription() {
        // Arrange
        LocalDate today = LocalDate.now();
        testSubscription.setDeleted(true);
        testSubscription.setNextRenewalDate(today.plusDays(1));

        when(subscriptionRepository.findAllActiveForRenewalDate(today.plusDays(1)))
                .thenReturn(Collections.emptyList()); // Query filters deleted=false

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 6: Multiple reminders sent for multiple subscriptions
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should send reminders for multiple subscriptions due tomorrow")
    void testMultipleRemindersForMultipleSubscriptions() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        SubscriptionEntity subscription2 = SubscriptionEntity.builder()
                .id(2L)
                .subscriptionName("Spotify")
                .profile(testProfile)
                .status(SubscriptionStatus.ACTIVE)
                .nextRenewalDate(tomorrow)
                .reminderDaysBefore(1)
                .deleted(false)
                .build();

        testSubscription.setNextRenewalDate(tomorrow);

        when(subscriptionRepository.findAllActiveForRenewalDate(tomorrow))
                .thenReturn(Arrays.asList(testSubscription, subscription2));

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 7: Email content is correct
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should include correct information in reminder email")
    void testEmailContentIsCorrect() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        testSubscription.setNextRenewalDate(tomorrow);
        testSubscription.setSubscriptionName("Disney+");
        testSubscription.getProfile().setFullName("Jane Smith");

        when(subscriptionRepository.findAllActiveForRenewalDate(tomorrow))
                .thenReturn(Arrays.asList(testSubscription));

        // Act
        subscriptionService.sendDailyRenewalReminders();

        // Assert
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());

        String emailBody = bodyCaptor.getValue();
        assertTrue(emailBody.contains("Disney+"), "Email should contain subscription name");
        assertTrue(emailBody.contains("Jane Smith"), "Email should contain user's name");
        assertTrue(emailBody.contains(tomorrow.toString()), "Email should contain renewal date");
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 8: Scheduler runs without throwing exceptions
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should handle scheduler execution gracefully")
    void testSchedulerExecutionHandlesExceptions() {
        // Arrange
        LocalDate today = LocalDate.now();
        when(subscriptionRepository.findAllActiveForRenewalDate(today.plusDays(1)))
                .thenReturn(Arrays.asList(testSubscription));
        
        // Simulate email sending exception
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> subscriptionService.sendDailyRenewalReminders());
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 9: isDueForReminder() correctly checks reminder timing
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("isDueForReminder() returns true when reminder date matches")
    void testIsDueForReminderMethodLogic() {
        // Arrange
        LocalDate today = LocalDate.of(2026, 3, 1);
        LocalDate renewalDate = LocalDate.of(2026, 3, 2); // Tomorrow
        testSubscription.setNextRenewalDate(renewalDate);
        testSubscription.setReminderDaysBefore(1);
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);

        // Act & Assert
        assertTrue(testSubscription.isDueForReminder(today), 
                "Should be due for reminder 1 day before renewal");
    }

    @Test
    @DisplayName("isDueForReminder() returns false when not the reminder day")
    void testIsDueForReminderReturnsFalseOnOtherDays() {
        // Arrange
        LocalDate today = LocalDate.of(2026, 3, 1);
        LocalDate renewalDate = LocalDate.of(2026, 3, 5); // 4 days from now
        testSubscription.setNextRenewalDate(renewalDate);
        testSubscription.setReminderDaysBefore(1);
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);

        // Act & Assert
        assertFalse(testSubscription.isDueForReminder(today),
                "Should not be due for reminder yet");
    }

    @Test
    @DisplayName("isDueForReminder() returns false for non-ACTIVE status")
    void testIsDueForReminderReturnsFalseForInactiveStatus() {
        // Arrange
        LocalDate today = LocalDate.of(2026, 3, 1);
        LocalDate renewalDate = LocalDate.of(2026, 3, 2); // Tomorrow
        testSubscription.setNextRenewalDate(renewalDate);
        testSubscription.setReminderDaysBefore(1);
        testSubscription.setStatus(SubscriptionStatus.PAUSED);

        // Act & Assert
        assertFalse(testSubscription.isDueForReminder(today),
                "Should not send reminder for non-ACTIVE subscriptions");
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 10: Multiple reminders scenarios with different custom reminder days
    // ─────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Should respect custom reminderDaysBefore setting (3 days)")
    void testCustomReminderDaysBeforeThreeDays() {
        // Arrange
        LocalDate today = LocalDate.of(2026, 3, 1);
        LocalDate renewalDate = LocalDate.of(2026, 3, 4); // 3 days from now
        testSubscription.setNextRenewalDate(renewalDate);
        testSubscription.setReminderDaysBefore(3); // Custom: 3 days before
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);

        // Act & Assert
        assertTrue(testSubscription.isDueForReminder(today),
                "Should be due for reminder 3 days before renewal");
    }

    @Test
    @DisplayName("Should respect custom reminderDaysBefore setting (7 days)")
    void testCustomReminderDaysBeforeSevenDays() {
        // Arrange
        LocalDate today = LocalDate.of(2026, 3, 1);
        LocalDate renewalDate = LocalDate.of(2026, 3, 8); // 7 days from now
        testSubscription.setNextRenewalDate(renewalDate);
        testSubscription.setReminderDaysBefore(7); // Custom: 7 days before
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);

        // Act & Assert
        assertTrue(testSubscription.isDueForReminder(today),
                "Should be due for reminder 7 days before renewal");
    }

}
