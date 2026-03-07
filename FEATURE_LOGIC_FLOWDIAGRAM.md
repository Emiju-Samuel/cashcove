# Subscription Reminder Feature - Logic Flow & Sequence Diagrams

## 📊 How the Feature Works - Visual Flow

### **Daily Scheduler Flow**

```
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION STARTUP                           │
│         @EnableScheduling enabled in CashcoveApplication        │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│           EVERY DAY AT 00:05 UTC (Cron: 0 5 0 * * ?)            │
│                                                                  │
│  sendDailyRenewalReminders() is executed automatically          │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│              Step 1: Get Today's Date                            │
│                                                                  │
│  LocalDate today = LocalDate.now()                              │
│  Example: today = March 1, 2026                                 │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│         Step 2: Query Tomorrow's Subscriptions                   │
│                                                                  │
│  List<SubscriptionEntity> due =                                 │
│    subscriptionRepository.findAllActiveForRenewalDate(          │
│      today.plusDays(1)  // March 2, 2026                        │
│    )                                                             │
│                                                                  │
│  SQL Query Result:                                              │
│  ┌──────────────────────────────────────────────────┐           │
│  │ id│ name      │ nextRenewalDate │ status │ email │           │
│  ├──┼───────────┼─────────────────┼────────┼──────┤            │
│  │1 │ Netflix   │ 2026-03-02      │ACTIVE │u1@.. │            │
│  │2 │ Spotify   │ 2026-03-02      │ACTIVE │u2@.. │            │
│  │3 │ Disney+   │ 2026-03-05      │ACTIVE │u3@.. │ ← NOT INCLUDED
│  └──────────────────────────────────────────────────┘           │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│        Step 3: Loop Through Each Subscription                    │
│                                                                  │
│  for (SubscriptionEntity subscription : due) { ... }            │
└─────────────────────┬───────────────────────────────────────────┘
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼                       ▼
    ┌──────────────┐        ┌──────────────┐
    │ Netflix      │        │ Spotify      │
    │ (ID: 1)      │        │ (ID: 2)      │
    └──────┬───────┘        └──────┬───────┘
           │                       │
           ▼                       ▼
    ┌──────────────────┐  ┌──────────────────┐
    │ Step 4: Create   │  │ Step 4: Create   │
    │ email subject    │  │ email subject    │
    │ "Subscription    │  │ "Subscription    │
    │ Renewal Reminder │  │ Renewal Reminder │
    │ Netflix"         │  │ Spotify"         │
    └──────┬───────────┘  └──────┬───────────┘
           │                     │
           ▼                     ▼
    ┌──────────────────┐  ┌──────────────────┐
    │ Step 5: Create   │  │ Step 5: Create   │
    │ email body       │  │ email body       │
    │ "Hi [Name], your │  │ "Hi [Name], your │
    │  Netflix         │  │  Spotify         │
    │  renews on ...   │  │  renews on ...   │
    │                  │  │                  │
    │ Step 6: Send     │  │ Step 6: Send     │
    │ email            │  │ email            │
    │ user1@exam...    │  │ user2@exam...    │
    └──────┬───────────┘  └──────┬───────────┘
           │                     │
           │              EmailService
           │              .sendEmail()
           │                     │
           │                     ▼
           │             ┌────────────────┐
           │             │ SMTP Server    │
           │             │(Mailtrap.io)   │
           │             └────────────────┘
           │                     │
           ▼                     ▼
    ┌───────────────────────────────────┐
    │   User Receives Email              │
    │                                     │
    │  From: emijusamuel@yahoo.com       │
    │  To: user1@example.com, user2@...  │
    │  Subject: Subscription Renewal     │
    │           Reminder: Netflix        │
    │                                     │
    │  Body: Hi John Doe,                │
    │        Your subscription for       │
    │        Netflix is due for          │
    │        renewal on 2026-03-02       │
    └───────────────────────────────────┘
           │
           ▼
    ┌──────────────────┐
    │ User sees email  │
    │ Prepares payment │
    │ Subscription     │
    │ doesn't lapse    │
    │ ✅ SUCCESS!       │
    └──────────────────┘
```

---

## 🔄 Sequence Diagram - End-to-End Flow

```
Scheduler    Service       Repository    EmailService    SMTP Server    User
   │            │              │              │               │          │
   │            │              │              │               │          │
   │ Execute    │              │              │               │          │
   │ at 00:05   │              │              │               │          │
   ├───────────>│              │              │               │          │
   │            │              │              │               │          │
   │            │ Query due    │              │               │          │
   │            │ tomorrow     │              │               │          │
   │            ├─────────────>│              │               │          │
   │            │              │              │               │          │
   │            │ Return List  │              │               │          │
   │            │<──────────────              │               │          │
   │            │              │              │               │          │
   │            │   for each   │              │               │          │
   │            │ subscription │              │               │          │
   │            │              │              │               │          │
   │            │ Build Email  │              │               │          │
   │            │              │              │               │          │
   │            │ (Try Block)  │              │               │          │
   │            ├──────────────────────────────>              │          │
   │            │              │              │               │          │
   │            │              │ Send Email   │               │          │
   │            │              │              ├──────────────>│          │
   │            │              │              │               │          │
   │            │              │              │   SMTP Resp   │          │
   │            │              │              │<──────────────│          │
   │            │              │              │               │          │
   │            │              │              │               │ Deliver  │
   │            │              │              │               ├─────────>│
   │            │              │              │               │          │
   │            │              │<──────────────────────────────           │
   │            │              │              │               │          │
   │            │ (Catch Block)│              │               │          │
   │            │ if exception │              │               │          │
   │            │ log error    │              │               │          │
   │            │              │              │               │          │
   │ Complete   │              │              │               │          │
   │<───────────│              │              │               │          │
   │            │              │              │               │          │
   │  (Waits for next 24hrs)   │              │               │          │
   │            │              │              │               │          │
```

---

## 🗄️ Database Schema Interaction

```
┌─────────────────────────────────────────────────────────────────┐
│                    tbl_subscriptions                             │
├─────────────────────────────────────────────────────────────────┤
│ id (PK)                                                         │
│ profile_id (FK) ──┐                                             │
│ subscription_name │                                             │
│ amount            │                                             │
│ next_renewal_date │──── Scheduler queries for:                 │
│ reminder_days_b   │     WHERE next_renewal_date = ?            │
│ category_id       │     AND status = 'ACTIVE'                  │
│ status (ENUM)     │     AND deleted = false                    │
│ deleted           │     LIMIT [unlimited]                      │
│ created_at        │                                             │
│ updated_at        │                                             │
└─────────────────────────────────────────────────────────────────┘
                     │
                     ▼
            ┌───────────────────┐
            │ tbl_profiles      │
            ├───────────────────┤
            │ id (PK)           │
            │ email             │──── Used to send email
            │ full_name         │──── Used in email greeting
            │ ...               │
            └───────────────────┘

            ┌───────────────────┐
            │ tbl_categories    │
            ├───────────────────┤
            │ id (PK)           │
            │ name              │
            │ ...               │
            └───────────────────┘
```

---

## ⏰ Timeline Example - March 1-2, 2026

```
March 1, 2026 (Saturday)
────────────────────────────────────────────────────────────

00:00 ┌─────────────────────────────────────────────────┐
      │              Midnight (00:00 UTC)                │
      └─────────────────────────────────────────────────┘

00:05 ┌─────────────────────────────────────────────────┐
      │  ⚙️  SCHEDULER TRIGGERS                         │
      │  sendDailyRenewalReminders() executes          │
      │                                                 │
      │  Today = March 1, 2026                          │
      │  Query for: nextRenewalDate = March 2, 2026     │
      │                                                 │
      │  Found:                                         │
      │  - Netflix     (nextRenewalDate: 2026-03-02) ✓  │
      │  - Spotify     (nextRenewalDate: 2026-03-02) ✓  │
      │  - Disney+     (nextRenewalDate: 2026-03-15) ✗  │
      │                                                 │
      │  📧 Emails sent to:                             │
      │  - user1@example.com  (Netflix)                 │
      │  - user2@example.com  (Spotify)                 │
      └─────────────────────────────────────────────────┘

12:00 ┌─────────────────────────────────────────────────┐
      │              Noon (12:00 UTC)                    │
      │  Users check their email and see reminders      │
      └─────────────────────────────────────────────────┘


March 2, 2026 (Sunday) - RENEWAL DAY
────────────────────────────────────────────────────────────

00:05 ┌─────────────────────────────────────────────────┐
      │  ⚙️  SCHEDULER TRIGGERS AGAIN                   │
      │  Today = March 2, 2026                          │
      │  Query for: nextRenewalDate = March 3, 2026     │
      │                                                 │
      │  Note: Netflix/Spotify are NOT matched          │
      │  (no reminders today - they're renewing TODAY)  │
      └─────────────────────────────────────────────────┘

10:00 ┌─────────────────────────────────────────────────┐
      │  💳 Netflix charges user's payment method        │
      │  💳 Spotify charges user's payment method        │
      │  ✅ Subscriptions renewed successfully          │
      └─────────────────────────────────────────────────┘

23:59 ┌─────────────────────────────────────────────────┐
      │  End of renewal day                             │
      │  Subscriptions are active for another month    │
      └─────────────────────────────────────────────────┘
```

---

## 🔍 What Emails Look Like

### **Email Sent on March 1, 2026 at 00:05 UTC**

```
From:    emijusamuel@yahoo.com
To:      john.doe@example.com
Date:    March 1, 2026 00:05:02 UTC
Subject: Subscription Renewal Reminder: Netflix

Body:
───────────────────────────────────────────────────────────
Hi John Doe,

Your subscription for Netflix is due for renewal on 2026-03-02.

This is a friendly reminder to ensure you don't forget about 
your subscription renewal!

Have a great day!

---
CashCove Finance Management
───────────────────────────────────────────────────────────
```

---

## 🎯 Decision Tree - Is This Subscription Getting a Reminder?

```
                    Subscription Created
                            │
                            ▼
                  ┌─────────────────────┐
                  │ Status = ACTIVE?    │
                  └────────┬────────┬──┘
                          YES      NO
                           │        │
                           ▼        ▼
                        ┌──┐   Will NOT
                        │  │   receive
                        │  │   reminder
                  ┌─────┴──┴────────┐
                  │ Deleted=false?   │
                  └────────┬────────┬┘
                          YES      NO
                           │        │
                           ▼        ▼
                        ┌──┐   Will NOT
                        │  │   receive
                        │  │   reminder
          ┌──────────────┴──┴──────────────┐
          │ nextRenewalDate =              │
          │   TODAY + reminderDaysBefore?  │
          └────────┬──────────────┬────────┘
                  YES            NO
                   │              │
                   ▼              ▼
              📧 SEND EMAIL    Do nothing
              to user.email    (wait for
                               next day)
```

**💡 KEY LOGIC**: 
- Subscription must be **ACTIVE**
- Subscription must NOT be **DELETED**
- **TODAY** must equal **NEXT_RENEWAL_DATE minus REMINDER_DAYS_BEFORE**

---

## 📋 Example Scenarios

### **Scenario 1: Typical Happy Path** ✅

| Date | Event | Result |
|---|---|---|
| Jan 15 | User creates Netflix subscription | Status: ACTIVE |
| Feb 15 | nextRenewalDate set to Mar 15 | Ready to remind |
| Mar 14 | Scheduler runs at 00:05 | `nextRenewalDate = Mar 15`? YES → Send email ✅ |
| Mar 15 | Renewal happens | User doesn't forget thanks to reminder |

### **Scenario 2: User Pauses Subscription** ⚠️

| Date | Event | Result |
|---|---|---|
| Jan 15 | User creates Netflix subscription | Status: ACTIVE |
| Feb 20 | User pauses subscription | Status: PAUSED |
| Mar 14 | Scheduler runs at 00:05 | Status = PAUSED? NO → No email sent |
| Mar 15 | Scheduler doesn't charge | Subscription paused as intended |

### **Scenario 3: User Cancels Early** ✓

| Date | Event | Result |
|---|---|---|
| Jan 15 | User creates Netflix subscription | Status: ACTIVE |
| Feb 10 | User cancels subscription | Status: CANCELLED |
| Mar 14 | Scheduler runs at 00:05 | Status = CANCELLED? NO → No email sent |
| Mar 15 | No charge occurs | Subscription properly cancelled |

### **Scenario 4: Scheduler Fails (BUG)** ❌

| Date | Event | Result |
|---|---|---|
| Jan 15 | User creates subscription with reminderDaysBefore=7 | Active |
| Mar 14 | Scheduler runs but app crashes at 00:05 | Email NOT sent |
| Mar 21 | Scheduler is back online | nextRenewalDate is Mar 21 (today) - too late! |
| Mar 21 | Scheduler has no way to recover | User never got reminder |

**This is the issue that Priority 1 fix addresses!**

---

## 🔧 Current Implementation Issues Visualized

### **Issue #1: reminderDaysBefore Ignored**

```
User's Preference:          Scheduler Implementation:
─────────────────────      ───────────────────────

reminderDaysBefore = 1     Always query for:
✓ RESPECTED               nextRenewalDate = today + 1

reminderDaysBefore = 3     But... ignores this!
✗ IGNORED                 nextRenewalDate = today + 3 NEVER CHECKED

reminderDaysBefore = 7     And this!
✗ IGNORED                 nextRenewalDate = today + 7 NEVER CHECKED

reminderDaysBefore = 14    And this!
✗ IGNORED                 nextRenewalDate = today + 14 NEVER CHECKED
```

---

## ✅ Data Validation Fields

```
For each subscription reminder, the system checks:

✅ Is status = 'ACTIVE'?          → Ensures active subscriptions
✅ Is deleted = false?            → Ensures not soft-deleted
✅ Is nextRenewalDate valid?      → Ensures date exists
✅ Does email exist?              → Ensures recipient address
✅ Is profile name present?       → Ensures greeting personalization
✅ Does reminderDaysBefore exist? → Ensures preference (but currently not used)

⚠️  Problems:
   - reminderDaysBefore field exists but is ignored
   - No check if calendar date is valid/reasonable
   - No check if email is actually valid format
   - No deduplication (same email could be sent twice if job runs twice)
```

---

## 🚀 The Fix (Priority #1)

### Current Code Problem:
```java
public void sendDailyRenewalReminders() {
    LocalDate today = LocalDate.now();
    // HARDCODED to look 1 day ahead - ignores reminderDaysBefore!
    List<SubscriptionEntity> due = 
        subscriptionRepository.findAllActiveForRenewalDate(today.plusDays(1));
    
    due.forEach(subscription -> {
        // ... send email ...
    });
}
```

### Fixed Code (Respects User Preference):
```java
public void sendDailyRenewalReminders() {
    LocalDate today = LocalDate.now();
    
    // Look ahead up to 30 days (covers most preferences)
    LocalDate maxThreshold = today.plusDays(30);
    List<SubscriptionEntity> potentialDue = 
        subscriptionRepository.findAllActiveByNextRenewalDateBetween(today, maxThreshold);
    
    potentialDue.forEach(subscription -> {
        // NOW CHECK: is today exactly reminderDaysBefore days before renewal?
        if (subscription.isDueForReminder(today)) {  // ← Uses reminderDaysBefore!
            // ... send email ...
        }
    });
}
```

---

**Document Version**: 1.0
**Last Updated**: March 1, 2026
