package com.example.billsplitter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billsplitter.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGrpId(Long grpId);
}
