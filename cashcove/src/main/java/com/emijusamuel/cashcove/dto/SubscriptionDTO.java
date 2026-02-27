package com.emijusamuel.cashcove.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.emijusamuel.cashcove.enums.SubscriptionFrequency;
import com.emijusamuel.cashcove.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class SubscriptionDTO {

    private Long id;
    private String subscriptionName;
    private String subscriptionCategory;
    private BigDecimal amount;
    private SubscriptionFrequency frequency;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate nextRenewalDate;

    private Integer reminderDaysBefore;

    private Long categoryId;

    private String categoryName;

    private SubscriptionStatus subscriptionStatus;

}
