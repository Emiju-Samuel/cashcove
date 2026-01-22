package com.emijusamuel.cashcove.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterDTO {

    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField;
    private String sortOrder;

}
