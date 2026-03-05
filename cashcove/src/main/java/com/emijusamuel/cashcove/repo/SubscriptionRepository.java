package com.emijusamuel.cashcove.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.entity.SubscriptionEntity;
import com.emijusamuel.cashcove.enums.SubscriptionStatus;

@Repository

public interface SubscriptionRepository extends JpaRepository <SubscriptionEntity, Long>{

    List<SubscriptionEntity> findByProfileIdOrderByNextRenewalDateDesc(Long profileId);

    Optional<SubscriptionEntity> findByIdAndProfileAndDeletedFalse(Long id, ProfileEntity profile);

    List<SubscriptionEntity> findAllByProfileAndDeletedFalse(ProfileEntity profile);

    List<SubscriptionEntity> findAllByProfileAndStatusAndDeletedFalse(

            ProfileEntity profile, SubscriptionStatus status);

    boolean existsByProfileAndSubscriptionNameAndDeletedFalse(ProfileEntity profile, String subscriptionName);


    // ─── 2. Derived queries (Spring generates JPQL automatically)
    List<SubscriptionEntity> findByProfileAndNextRenewalDateBetween(
            ProfileEntity profile, LocalDate start, LocalDate end);

    List<SubscriptionEntity> findByProfileAndStatusAndNextRenewalDateBefore(
            ProfileEntity profile, SubscriptionStatus status, LocalDate date);


    // ─── 3. Important custom @Query – used for daily reminder job ──────────────
    @Query("""
        SELECT s FROM SubscriptionEntity s
         WHERE s.profile = :profile
           AND s.status = 'ACTIVE'
           AND s.deleted = false
           AND s.nextRenewalDate <= :threshold
           AND s.nextRenewalDate >= :today
        ORDER BY s.nextRenewalDate ASC
        """)
    List<SubscriptionEntity> findActiveDueSoon(
            @Param("profile") ProfileEntity profile,
            @Param("today") LocalDate today,
            @Param("threshold") LocalDate threshold);

    // Very common for background job (run daily for ALL profiles)
    @Query("""
        SELECT s FROM SubscriptionEntity s
         WHERE s.status = 'ACTIVE'
           AND s.deleted = false
           AND s.nextRenewalDate = :renewalDate
        """)
    List<SubscriptionEntity> findAllActiveForRenewalDate(@Param("renewalDate") LocalDate renewalDate);


    // ─── 4. Optional: count for dashboard / statistics ─────────────────────────
    @Query("SELECT COUNT(s) FROM SubscriptionEntity s WHERE s.profile = :profile AND s.status = 'ACTIVE' AND s.deleted = false")
    long countActiveByProfile(@Param("profile") ProfileEntity profile);

    // ─── 5. Optional: exists check to prevent duplicates ───────────────────────
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SubscriptionEntity s " +
           "WHERE s.profile = :profile AND LOWER(s.subscriptionName) = LOWER(:name) AND s.id != :excludeId AND s.deleted = false")
    boolean existsByProfileAndNameIgnoringCaseAndIdNot(
            @Param("profile") ProfileEntity profile,
            @Param("name") String name,
            @Param("excludeId") long excludeId);

    // ─── 6. NEW: Query for subscriptions within date range for flexible reminder logic ─
    // Used by the enhanced scheduler to respect reminderDaysBefore preference
    @Query("""
        SELECT s FROM SubscriptionEntity s
         WHERE s.status = 'ACTIVE'
           AND s.deleted = false
           AND s.nextRenewalDate BETWEEN :startDate AND :endDate
        ORDER BY s.nextRenewalDate ASC
        """)
    List<SubscriptionEntity> findAllActiveByNextRenewalDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

}
