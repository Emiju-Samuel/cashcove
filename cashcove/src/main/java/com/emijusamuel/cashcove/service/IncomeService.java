package com.emijusamuel.cashcove.service;

import org.springframework.stereotype.Service;

import com.emijusamuel.cashcove.dto.ExpenseDTO;
import com.emijusamuel.cashcove.dto.IncomeDTO;
import com.emijusamuel.cashcove.entity.ExpenseEntity;
import com.emijusamuel.cashcove.entity.IncomeEntity;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.entity.CategoryEntity;
import com.emijusamuel.cashcove.repo.ExpenseRepository;
import com.emijusamuel.cashcove.repo.IncomeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryService categoryService;
    private final IncomeRepository incomeRepository;

    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category){
        return IncomeEntity.builder()
        .name(dto.getName())
        .icon(dto.getIcon())
        .amount(dto.getAmount())
        .date(dto.getDate())
        .profile(profile)
        .category(category)
        .build();
    }

    private IncomeDTO toDTO(IncomeEntity entity){
        return IncomeDTO.builder()
        .id(entity.getId())
        .name(entity.getName())
        .icon(entity.getIcon())
        .categoryId(entity.getCategory() != null ? entity.getCategory().getId(): null)
        .categoryName(entity.getCategory() != null ? entity.getCategory().getName(): "N/A")
        .amount(entity.getAmount())
        .date(entity.getDate())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
    }
}
