package com.emijusamuel.cashcove.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import com.emijusamuel.cashcove.dto.ExpenseDTO;
import com.emijusamuel.cashcove.entity.CategoryEntity;
import com.emijusamuel.cashcove.entity.ExpenseEntity;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import com.emijusamuel.cashcove.repo.CategoryRepository;
import com.emijusamuel.cashcove.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;


    // Adds a new expense to the database
    public ExpenseDTO addExpense(ExpenseDTO dto){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
        .orElseThrow(()-> new RuntimeException("Category not found"));
        ExpenseEntity newExpense = toEntity(dto, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    // Retrieve the expenses for the current month based on the start date and end date
    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }


    private ExpenseEntity toEntity(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category){
        return ExpenseEntity.builder()
        .name(dto.getName())
        .icon(dto.getIcon())
        .amount(dto.getAmount())
        .date(dto.getDate())
        .profile(profile)
        .category(category)
        .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity entity){
        return ExpenseDTO.builder()
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
