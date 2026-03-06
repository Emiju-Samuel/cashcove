package com.emijusamuel.cashcove.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseDTO {

    private Long id;
    private String name;
    private String icon;
    private String categoryName;
    private Long categoryId;
    private BigDecimal amount;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
