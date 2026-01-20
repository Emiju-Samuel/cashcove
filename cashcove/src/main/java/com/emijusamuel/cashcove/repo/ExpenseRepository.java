package com.emijusamuel.cashcove.repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.emijusamuel.cashcove.entity.ExpenseEntity;


public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long>{

    // select * from tbl_expenses where profile_id = ?1 order by date desc
    List<ExpenseEntity> findByProfileIdOrderByDateDesc(Long profileId);

    // select * from tbl_expenses where profile_id =?1 order by date desc limit 5
    List<ExpenseEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT SUM(e.amount) FROM ExpenseEntity e WHERE e.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

    // select * from tbl_expenses where profile_id = ?1 and date ?2 and ?3 and like %?4%
    List<ExpenseEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
        Long profileId,
        LocalDate startDate,
        LocalDate endDate,
        String keyword,
        Sort sort
    );

    // select * from tbl_expenses where profile_id = ?1 and date between ?2 and ?3
    List<ExpenseEntity> findByProfileIdAndDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);

}
