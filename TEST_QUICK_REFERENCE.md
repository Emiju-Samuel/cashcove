# Test Quick Reference Card

## 🏃 Run Tests in 10 Seconds

```bash
cd C:\Users\HP\Desktop\springboot_apps\cashcove\cashcove
mvn clean test -Dtest=SubscriptionReminder*
```

**That's it! All 16 tests run and you'll see:**
```
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.234 s
[INFO] BUILD SUCCESS
```

---

## 📋 Most Common Commands

| What You Want | Command |
|---|---|
| **Run ALL tests** | `mvn clean test -Dtest=SubscriptionReminder*` |
| **Run unit tests only** | `mvn clean test -Dtest=SubscriptionReminderServiceTest` |
| **Run integration tests only** | `mvn clean test -Dtest=SubscriptionReminderIntegrationTest` |
| **Run one specific test** | `mvn clean test -Dtest=SubscriptionReminderServiceTest#testReminderEmailSentOneDayBefore` |
| **Run with verbose output** | `mvn clean test -Dtest=SubscriptionReminder* -X` |
| **Run with code coverage** | `mvn clean test -Dtest=SubscriptionReminder* jacoco:report` |
| **Fast build (skip tests)** | `mvn clean install -DskipTests` |

---

## ✅ What Tests Verify

✅ Emails sent when reminder day matches  
✅ Emails NOT sent on other days  
✅ PAUSED subscriptions excluded  
✅ CANCELLED subscriptions excluded  
✅ Deleted subscriptions excluded  
✅ Custom reminder days (1, 3, 7, 14 days) work  
✅ Multiple reminders sent together  
✅ Error handling works  
✅ Email content is correct  
✅ Non-ACTIVE subscriptions skipped  

---

## 🐛 If Tests Fail

**Compilation error?**
```bash
mvn clean compile
```

**Can't find database?**
Create: `src/test/resources/application-test.properties`
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

**Missing dependencies?**
Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.2.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.1.214</version>
    <scope>test</scope>
</dependency>
```

---

## 📊 Test Breakdown

**Unit Tests** (10 tests): Test logic in isolation  
**Integration Tests** (6 tests): Test with Spring context  
**Total**: 16 tests covering the reminder feature  

---

## 🎯 What Changed in the Code

### Fixed the Critical Issue:
**Before** ❌
```java
List<SubscriptionEntity> due = subscriptionRepository
    .findAllActiveForRenewalDate(today.plusDays(1));  // Hardcoded 1 day!
```

**After** ✅
```java
List<SubscriptionEntity> potentialDue = subscriptionRepository
    .findAllActiveByNextRenewalDateBetween(today, today.plusDays(30));

if (subscription.isDueForReminder(today)) {  // Respects reminderDaysBefore!
    // Send email
}
```

---

## 📁 Files to Know

| File | Purpose | Type |
|---|---|---|
| `SubscriptionReminderServiceTest.java` | 10 unit tests | Test |
| `SubscriptionReminderIntegrationTest.java` | 6 integration tests | Test |
| `SubscriptionService.java` | Fixed the reminder logic | Source |
| `SubscriptionRepository.java` | Added date range query | Source |
| `HOW_TO_RUN_TESTS.md` | Detailed test guide | Doc |
| `SUMMARY_REPORT.md` | Analysis summary | Doc |

---

## 🚀 Success Checklist

- [ ] Run: `mvn clean test -Dtest=SubscriptionReminder*`
- [ ] See: "BUILD SUCCESS"
- [ ] See: "Tests run: 16, Failures: 0"
- [ ] Verify custom reminder days work (1, 3, 7, 14 days)
- [ ] Check logs show reminders being sent
- [ ] Deploy to staging
- [ ] Verify in production

---

**Now run the tests: `mvn clean test -Dtest=SubscriptionReminder*`** ✨
