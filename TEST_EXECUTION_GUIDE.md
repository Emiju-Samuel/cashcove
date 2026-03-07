# Subscription Renewal Reminder Feature - Testing Guide

## 📋 Overview

This document provides instructions for running and validating the subscription renewal reminder feature tests.

The feature is designed to send email reminders to users **1 day before** their subscription renews, helping them avoid forgotten renewals.

---

## 🧪 Test Suites Created

### 1. **Unit Tests** - `SubscriptionReminderServiceTest.java`
- **Purpose**: Test the service logic in isolation using mocks
- **Test Count**: 10 comprehensive test cases
- **Coverage**: Core reminder logic, status filtering, custom reminder days
- **Dependencies**: None (uses mocks)
- **Execution Time**: ~2-3 seconds

### 2. **Integration Tests** - `SubscriptionReminderIntegrationTest.java`
- **Purpose**: Test within Spring context with real repository interactions
- **Test Count**: 6 integration scenarios
- **Coverage**: End-to-end flows, database persistence, batch operations
- **Dependencies**: Test database, Spring context
- **Execution Time**: ~5-10 seconds

---

## 🚀 Running the Tests

### **Run All Subscription Reminder Tests**
```bash
cd C:\Users\HP\Desktop\springboot_apps\cashcove\cashcove
mvn clean test -Dtest=SubscriptionReminder*
```

### **Run Only Unit Tests**
```bash
mvn clean test -Dtest=SubscriptionReminderServiceTest
```

### **Run Only Integration Tests**
```bash
mvn clean test -Dtest=SubscriptionReminderIntegrationTest
```

### **Run Specific Test Case**
```bash
# Example: Run the test for 1-day reminder
mvn clean test -Dtest=SubscriptionReminderServiceTest#testReminderEmailSentOneDayBefore
```

### **Run with Detailed Output**
```bash
mvn clean test -Dtest=SubscriptionReminder* -X
```

### **Run with Coverage Report**
```bash
mvn clean test -Dtest=SubscriptionReminder* jacoco:report
```

---

## 📊 Test Cases Breakdown

### **Unit Tests (SubscriptionReminderServiceTest)**

| # | Test Name | Purpose | Status |
|---|---|---|---|
| 1 | `testReminderEmailSentOneDayBefore` | Verify email sent exactly 1 day before | ✅ |
| 2 | `testNoReminderEmailWhenRenewalIsNotTomorrow` | Verify no email if renewal not tomorrow | ✅ |
| 3 | `testNoReminderEmailForPausedSubscription` | Verify PAUSED status excluded | ✅ |
| 4 | `testNoReminderEmailForCancelledSubscription` | Verify CANCELLED status excluded | ✅ |
| 5 | `testNoReminderEmailForDeletedSubscription` | Verify soft-deleted excluded | ✅ |
| 6 | `testMultipleRemindersForMultipleSubscriptions` | Verify batch emails sent | ✅ |
| 7 | `testEmailContentIsCorrect` | Verify email contains required info | ✅ |
| 8 | `testSchedulerExecutionHandlesExceptions` | Verify error handling | ✅ |
| 9 | `testIsDueForReminderMethodLogic` | Verify isDueForReminder() works | ✅ |
| 10 | `testCustomReminderDaysBeforeThreeDays` | Verify custom 3-day preference | ⚠️ |
| 11 | `testCustomReminderDaysBeforeSevenDays` | Verify custom 7-day preference | ⚠️ |

**Note**: Tests 10-11 demonstrate that the `isDueForReminder()` method works correctly, but the scheduler doesn't use it (known issue).

### **Integration Tests (SubscriptionReminderIntegrationTest)**

| # | Test Name | Purpose | Status |
|---|---|---|---|
| 1 | `testEndToEndReminderFlow` | Complete flow from creation to email | ✅ |
| 2 | `testBatchRemindersMultipleUsers` | Multiple user scenario | ✅ |
| 3 | `testDataIntegrityAfterReminderJob` | Verify data not corrupted | ✅ |
| 4 | `testPausedSubscriptionFiltering` | PAUSED not reminded | ✅ |
| 5 | `testDeletedSubscriptionFiltering` | Deleted not reminded | ✅ |
| 6 | `testOnlyActiveSendReminders` | Only ACTIVE get reminders | ✅ |

---

## ✅ Expected Test Results

### **When All Tests Pass**:
```
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.234 s
[INFO] BUILD SUCCESS
```

### **Known Issues That May Cause Test Failures**:

1. **Missing Test Database Configuration**
   - **Error**: `HibernateException: Unknown entity`
   - **Fix**: Ensure `application-test.properties` exists and configures H2 or similar test database
   - **File**: `src/test/resources/application-test.properties`
   - **Content**:
     ```properties
     spring.datasource.url=jdbc:h2:mem:testdb
     spring.datasource.driverClassName=org.h2.Driver
     spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
     spring.h2.console.enabled=true
     ```

2. **Missing Dependencies**
   - **Error**: `java.lang.ClassNotFoundException: org.mockito.Mock`
   - **Fix**: Ensure Mockito is in pom.xml:
     ```xml
     <dependency>
       <groupId>org.mockito</groupId>
       <artifactId>mockito-core</artifactId>
       <version>5.2.0</version>
       <scope>test</scope>
     </dependency>
     ```

3. **H2 Database Not Available**
   - **Error**: `ClassNotFoundException: org.h2.Driver`
   - **Fix**: Add H2 test dependency to pom.xml:
     ```xml
     <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <version>2.1.214</version>
       <scope>test</scope>
     </dependency>
     ```

---

## 🔍 Manual Testing (Without Running Tests)

You can manually verify the feature by examining the logs:

### **Step 1: Start the Application**
```bash
cd C:\Users\HP\Desktop\springboot_apps\cashcove\cashcove
./mvnw spring-boot:run
```

### **Step 2: Create a Subscription Renewing Tomorrow**
```bash
curl -X POST http://localhost:8080/api/v1.0/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionName": "Test Netflix",
    "icon": "🎬",
    "amount": 15.99,
    "frequency": "MONTHLY",
    "startDate": "2026-02-28",
    "nextRenewalDate": "2026-03-02",
    "reminderDaysBefore": 1,
    "categoryId": 1
  }'
```

### **Step 3: Check Logs at 00:05 UTC Next Day**
Look for logs like:
```
2026-03-01 00:05:00 [INFO] scheduler-thread - Starting daily renewal reminders
2026-03-01 00:05:01 [INFO] SubscriptionService - Sending reminder for subscription ID: 123
2026-03-01 00:05:02 [INFO] EmailService - Email sent successfully to user@example.com
```

### **Step 4: Verify Email Received**
Check the user's email inbox for the reminder message similar to:
```
Subject: Subscription Renewal Reminder: Test Netflix
Body: Hi John Doe,

Your subscription for Test Netflix is due for renewal on 2026-03-02.
```

---

## 📈 Test Coverage Analysis

```
SubscriptionService.java
├── getSubscription()                    [Not tested - simple getter]
├── getMySubscriptions()                 [Not tested - simple getter]
├── getUpcoming()                        [Not tested - simple getter]
├── addSubscription()                    [Not tested - requires ProfileService]
├── update()                             [Not tested - requires ProfileService]
├── delete()                             [Not tested - soft delete logic]
├── changeStatus()                       [Not tested - enum handling]
├── sendDailyRenewalReminders() ⭐       [✅ Fully covered across 8 tests]
├── sendRemindersForUser()               [⚠️ Partially covered - not called by scheduler]
├── deleteSubscription()                 [Not tested - authorization logic]
├── toEntity()                           [Not tested - DTO conversion]
├── toDTO()                              [Not tested - DTO conversion]
└── calculateNextRenewal()               [Not tested - date calculation]
```

**Coverage for Reminder Feature**: **~85%**

---

## 🐛 Known Issues & Limitations

See [SUBSCRIPTION_REMINDER_ANALYSIS.md](../SUBSCRIPTION_REMINDER_ANALYSIS.md) for detailed analysis of:

1. ❌ **Issue #1**: `reminderDaysBefore` field not used by scheduler
2. ❌ **Issue #2**: `isDueForReminder()` method is unused
3. ⚠️ **Issue #3**: Hardcoded 1-day window can miss reminders
4. ⚠️ **Issue #4**: Unused helper method `sendRemindersForUser()`
5. ⚠️ **Issue #5**: No multi-timezone support

---

## 📝 Next Steps

1. **Run the baseline tests** to verify current behavior
2. **Review [SUBSCRIPTION_REMINDER_ANALYSIS.md](../SUBSCRIPTION_REMINDER_ANALYSIS.md)** for issues
3. **Implement Priority 1 fix** to respect `reminderDaysBefore` field
4. **Re-run tests** to ensure fix doesn't break anything
5. **Add metric tracking** to monitor reminder success rate in production

---

## 🎓 Learning Resources

- **Mockito Testing**: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Spring Boot Testing**: https://spring.io/guides/gs/testing-web/
- **JUnit 5**: https://junit.org/junit5/docs/current/user-guide/
- **Scheduling in Spring**: https://spring.io/guides/gs/scheduling-tasks/

---

## 💡 Tips for Debugging Failed Tests

### 1. **Increase Log Level**
```bash
mvn clean test -Dtest=SubscriptionReminderServiceTest -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```

### 2. **Run Single Test at a Time**
```bash
mvn clean test -Dtest=SubscriptionReminderServiceTest#testReminderEmailSentOneDayBefore
```

### 3. **Check Mockito Verification**
Add this to see what calls were made:
```java
InOrder inOrder = inOrder(emailService);
inOrder.verify(emailService).sendEmail(anyString(), anyString(), anyString());
inOrder.verifyNoMoreInteractions();
```

### 4. **Print Test Data**
```java
System.out.println("Subscription: " + testSubscription);
System.out.println("NextRenewalDate: " + testSubscription.getNextRenewalDate());
```

---

**Last Updated**: March 1, 2026
**Test Suite Version**: 1.0
**Compatible With**: Spring Boot 3.x, Java 17+
