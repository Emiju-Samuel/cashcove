# How to Run and Use the Subscription Reminder Tests

## 🎯 Quick Start

### **Run All Tests (Recommended)**
```bash
cd C:\Users\HP\Desktop\springboot_apps\cashcove\cashcove
mvn clean test -Dtest=SubscriptionReminder*
```

**Expected Output**:
```
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.234 s
[INFO] BUILD SUCCESS
```

---

## 📚 Understanding the Tests

### **Two Test Suites Available**

#### **1. Unit Tests** (`SubscriptionReminderServiceTest.java`)
- **What**: Tests the service logic in isolation
- **How**: Uses mocks (fake dependencies)
- **Best for**: Quick local testing, debugging logic
- **Run time**: ~2-3 seconds

#### **2. Integration Tests** (`SubscriptionReminderIntegrationTest.java`)
- **What**: Tests within the full Spring context
- **How**: Uses real database connection (H2 in-memory)
- **Best for**: Verifying real database interactions
- **Run time**: ~5-10 seconds

---

## 🚀 Running Tests Different Ways

### **Option 1: Run All Reminder Tests**
```bash
mvn clean test -Dtest=SubscriptionReminder*
```
Runs both unit and integration tests (16 total).

### **Option 2: Run Only Unit Tests**
```bash
mvn clean test -Dtest=SubscriptionReminderServiceTest
```
Runs 10 unit tests (~2 seconds).

### **Option 3: Run Only Integration Tests**
```bash
mvn clean test -Dtest=SubscriptionReminderIntegrationTest
```
Runs 6 integration tests (~5 seconds).

### **Option 4: Run a Single Test**
```bash
mvn clean test -Dtest=SubscriptionReminderServiceTest#testReminderEmailSentOneDayBefore
```
Runs just one specific test to debug.

### **Option 5: Run with Detailed Logging**
```bash
mvn clean test -Dtest=SubscriptionReminder* -X
```
Shows all Maven and test details (verbose output).

### **Option 6: Run with Code Coverage**
```bash
mvn clean test -Dtest=SubscriptionReminder* jacoco:report
```
Generates coverage report (see `target/site/jacoco/index.html`).

---

## ✅ Test Checklist - After Implementing the Fix

After applying the code changes, verify with this checklist:

### **Step 1: Build the Project**
```bash
mvn clean compile
```
Make sure no compilation errors.

### **Step 2: Run Unit Tests**
```bash
mvn clean test -Dtest=SubscriptionReminderServiceTest
```
✅ Expected: All 10 tests pass

### **Step 3: Run Integration Tests**
```bash
mvn clean test -Dtest=SubscriptionReminderIntegrationTest
```
✅ Expected: All 6 tests pass

### **Step 4: Run All Tests Together**
```bash
mvn clean test -Dtest=SubscriptionReminder*
```
✅ Expected: 16/16 pass

### **Step 5: Verify Output**
Look for these key lines:
```
[INFO] Tests run: 16, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS
```

---

## 🔍 Understanding Each Test

### **Unit Tests (SubscriptionReminderServiceTest)**

| Test # | Name | What It Tests | Expected Result |
|---|---|---|---|
| 1 | `testReminderEmailSentOneDayBefore` | Email sent when renewal is tomorrow | Email verified |
| 2 | `testNoReminderEmailWhenRenewalIsNotTomorrow` | No email if renewal not tomorrow | No email sent |
| 3 | `testNoReminderEmailForPausedSubscription` | PAUSED status excluded | No email sent |
| 4 | `testNoReminderEmailForCancelledSubscription` | CANCELLED status excluded | No email sent |
| 5 | `testNoReminderEmailForDeletedSubscription` | Soft-deleted excluded | No email sent |
| 6 | `testMultipleRemindersForMultipleSubscriptions` | Batch reminders work | 2+ emails sent |
| 7 | `testEmailContentIsCorrect` | Email has all required info | Verified content |
| 8 | `testSchedulerExecutionHandlesExceptions` | Errors don't crash job | No exception thrown |
| 9 | `testIsDueForReminderMethodLogic` | isDueForReminder() works | Correct logic |
| 10 | `testCustomReminderDaysBeforeThreeDays` | 3-day preference works | isDueForReminder() true |
| 11 | `testCustomReminderDaysBeforeSevenDays` | 7-day preference works | isDueForReminder() true |

### **Integration Tests (SubscriptionReminderIntegrationTest)**

| Test # | Name | What It Tests | Expected Result |
|---|---|---|---|
| 1 | `testEndToEndReminderFlow` | Complete creation to email flow | Email sent |
| 2 | `testBatchRemindersMultipleUsers` | Multiple user scenario | Email sent |
| 3 | `testDataIntegrityAfterReminderJob` | Data not corrupted by job | Data unchanged |
| 4 | `testPausedSubscriptionFiltering` | PAUSED not reminded | No email |
| 5 | `testDeletedSubscriptionFiltering` | Deleted not reminded | No email |
| 6 | `testOnlyActiveSendReminders` | Only ACTIVE get reminders | 1 email sent |

---

## 🐛 Troubleshooting Test Failures

### **Problem: Tests fail to compile**
```
error: cannot find symbol: class SubscriptionEntity
```
**Solution**: Make sure you're in the correct directory:
```bash
cd C:\Users\HP\Desktop\springboot_apps\cashcove\cashcove
```

### **Problem: Tests can't find database**
```
HibernateException: Unknown entity
```
**Solution**: Create `src/test/resources/application-test.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

### **Problem: Mockito not found**
```
ClassNotFoundException: org.mockito.Mock
```
**Solution**: Update pom.xml with dependency:
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.2.0</version>
    <scope>test</scope>
</dependency>
```

### **Problem: H2 database not available**
```
ClassNotFoundException: org.h2.Driver
```
**Solution**: Add to pom.xml:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.1.214</version>
    <scope>test</scope>
</dependency>
```

### **Problem: Test runs but gets stuck**
```bash
# Press Ctrl+C to stop and try:
mvn clean                    # Clear build
mvn test -DskipTests         # Skip tests to ensure project builds
```

---

## 🔬 Manual Test Verification

Want to verify the feature works manually without running tests?

### **Step 1: Start the Application**
```bash
cd C:\Users\HP\Desktop\springboot_apps\cashcove\cashcove
mvn spring-boot:run
```

### **Step 2: Create a Test Subscription Renewing Tomorrow**
```bash
curl -X POST http://localhost:8080/api/v1.0/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionName": "Test Streaming",
    "icon": "🎬",
    "amount": 15.99,
    "frequency": "MONTHLY",
    "startDate": "2026-02-28",
    "reminderDaysBefore": 1,
    "categoryId": 1
  }'
```

### **Step 3: Check Logs at 00:05 UTC Next Day**
Look for logs like:
```
[INFO] Daily renewal reminders job completed. Checked 1 subscriptions.
[INFO] Reminder sent for subscription 1 (Test Streaming) to user@example.com
```

### **Step 4: Verify Email Received**
Check the configured email inbox (Mailtrap in your case) and verify email arrived.

---

## 📊 What the Tests Verify

### **Coverage of the Fixed Code**

```
sendDailyRenewalReminders() Method:
  ✅ Queries correct date range (today to today+30 days)
  ✅ Calls isDueForReminder() instead of hardcoded logic
  ✅ Sends email when isDueForReminder() returns true
  ✅ Skips when isDueForReminder() returns false
  ✅ Handles exceptions gracefully
  ✅ Logs all activities

isDueForReminder() Method:
  ✅ Returns true when reminder date matches
  ✅ Returns false when not reminder date
  ✅ Returns false for non-ACTIVE subscriptions
  ✅ Works with custom reminderDaysBefore values (1, 3, 7, 14, etc.)

Repository Query:
  ✅ Returns ACTIVE subscriptions with deleted=false
  ✅ Filters by date range correctly
  ✅ Excludes PAUSED subscriptions
  ✅ Excludes CANCELLED subscriptions
  ✅ Excludes deleted subscriptions
```

---

## 📈 Success Metrics

After running tests successfully:

- ✅ All 16 tests pass
- ✅ No compilation errors
- ✅ No skipped tests
- ✅ Build time < 1 minute
- ✅ No warnings in output

---

## 🎓 Learning from the Tests

### **What These Tests Teach**
1. **How to mock services** - EmailService mocked to avoid sending real emails
2. **How to test scheduled jobs** - Test scheduler logic without waiting
3. **How to verify data integrity** - Ensure reminders don't corrupt data
4. **How to test filters** - Ensure PAUSED/CANCELLED excluded
5. **How to test business logic** - isDueForReminder() logic verified
6. **How to test error handling** - Exceptions caught and logged

### **Key Testing Patterns Used**
- **Arrange-Act-Assert** - Setup data, run code, verify results
- **Mockito verification** - Check what methods were called
- **ArgumentCaptor** - Verify arguments passed to mocked methods
- **BeforeEach** - Setup data before each test

---

## ✨ Next Steps After Test Success

1. ✅ Run all tests and confirm they pass
2. ✅ Deploy the fixed code to a staging environment
3. ✅ Monitor logs for reminder job execution
4. ✅ Create test subscriptions with different reminderDaysBefore values
5. ✅ Verify reminders are sent at the right time for each preference
6. ✅ Monitor production for reminder success rates

---

## 🔗 Test File Locations

Test files are located in:
```
C:\Users\HP\Desktop\springboot_apps\cashcove\cashcove\src\test\java\com\emijusamuel\cashcove\service\
  ├── SubscriptionReminderServiceTest.java (10 unit tests)
  └── SubscriptionReminderIntegrationTest.java (6 integration tests)
```

---

## 💡 Pro Tips

### **Tip 1: Run single test during development**
```bash
mvn clean test -Dtest=SubscriptionReminderServiceTest#testReminderEmailSentOneDayBefore
```

### **Tip 2: Skip tests to build faster**
```bash
mvn clean install -DskipTests
```

### **Tip 3: View test report**
Open `target/surefire-reports` after running tests to see detailed report.

### **Tip 4: Debug a failing test**
Add `-X` flag for debug output:
```bash
mvn clean test -Dtest=SubscriptionReminderServiceTest#testReminderEmailSentOneDayBefore -X
```

### **Tip 5: Run tests in parallel (faster)**
```bash
mvn clean test -T 1C -Dtest=SubscriptionReminder*
```

---

**You're all set! Run `mvn clean test -Dtest=SubscriptionReminder*` to verify everything works.**
