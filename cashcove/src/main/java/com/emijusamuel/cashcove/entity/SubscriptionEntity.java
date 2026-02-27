package com.emijusamuel.cashcove.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.emijusamuel.cashcove.enums.SubscriptionFrequency;
import com.emijusamuel.cashcove.enums.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_subscriptions",
    indexes = {
           @Index(name = "idx_sub_user_next_renewal", columnList = "profile_id, nextRenewalDate"),
           @Index(name = "idx_sub_next_renewal", columnList = "nextRenewalDate")
       })
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    @Column
    private String icon;

    @Column(nullable = false, length = 150)
    private String subscriptionName;

    @Column(nullable = false, length = 150)
    private String subscriptionCategory;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal Amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionFrequency subscriptionFrequency;

    @Column
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate nextRenewalDate;

    @Column(nullable = false)
    private Integer reminderDaysBefore = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Optional: soft delete support
    @Column(nullable = false)
    private boolean deleted = false;

    // Business method – helps in service layer
    public boolean isDueForReminder(LocalDate today) {
        if (status != SubscriptionStatus.ACTIVE) {
            return false;
        }
        return nextRenewalDate != null &&
               nextRenewalDate.minusDays(reminderDaysBefore).equals(today);
    }

    

    public void prePersist(){
        if(this.startDate == null){
            this.startDate = LocalDate.now();
        }
    }

}
