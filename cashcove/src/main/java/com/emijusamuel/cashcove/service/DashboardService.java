package com.emijusamuel.cashcove.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import com.emijusamuel.cashcove.dto.ExpenseDTO;
import com.emijusamuel.cashcove.dto.IncomeDTO;
import com.emijusamuel.cashcove.dto.RecentTransactionDTO;
import com.emijusamuel.cashcove.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboardData(){
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();
        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
        List<RecentTransactionDTO> recentTransactions = Stream.concat(
        latestIncomes.stream().map(income -> 
            RecentTransactionDTO.builder()
            .id(income.getId())
            .profileId(profile.getId())
            .icon(income.getIcon())
            .name(income.getName())
            .amount(income.getAmount())
            .date(income.getDate())
            .createdAt(income.getCreatedAt())
            .type("income")
            .build()), 
        latestExpenses.stream().map(expense -> 
            RecentTransactionDTO.builder()
            .id(expense.getId())
            .profileId(profile.getId())
            .icon(expense.getIcon())
            .name(expense.getName())
            .amount(expense.getAmount())
            .date(expense.getDate())
            .createdAt(expense.getCreatedAt())
            .type("expense")
            .build()))
            .sorted((a, b) -> {
                int cmp = b.getDate().compareTo(a.getDate());
                if(cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null){
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                }
                return cmp;
            }).collect(Collectors.toList());
            returnValue.put("totalBalance", 
            incomeService.getTotalIncomeForCurrentUser()
            .subtract(expenseService.getTotalExpenseForCurrentUser()));
        returnValue.put("totalIncome", incomeService.getTotalIncomeForCurrentUser());
        returnValue.put("totalExpense", expenseService.getTotalExpenseForCurrentUser());
        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recent5Incomes", latestIncomes);
        returnValue.put("recentTransactions", recentTransactions);
        return returnValue;
    }

}
