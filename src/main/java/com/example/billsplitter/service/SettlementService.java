package com.example.billsplitter.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.billsplitter.model.Expense;
import com.example.billsplitter.model.Member;
import com.example.billsplitter.repository.ExpenseRepository;
import com.example.billsplitter.repository.MemberRepository;

@Service
public class SettlementService {

    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;

    public SettlementService(MemberRepository memberRepository, ExpenseRepository expenseRepository) {
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
    }

    // Calculate settlements for a group
    public List<String> calculateSettlements(Long grpId) {
        List<Member> members = memberRepository.findByGrpId(grpId);
        List<Expense> expenses = expenseRepository.findByGrpId(grpId);

        if (members.isEmpty() || expenses.isEmpty()) {
            return Collections.singletonList("No data available for this group");
        }

        // Step 1: Calculate total expense
        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Step 2: Calculate per-head share
        BigDecimal perHead = totalExpense.divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);

        // Step 3: Calculate net balance per member
        Map<Long, BigDecimal> balanceMap = new HashMap<>();
        for (Member m : members) balanceMap.put(m.getId(), BigDecimal.ZERO);

        for (Expense e : expenses) {
            Long payerId = e.getPayer().getId();
            balanceMap.put(payerId, balanceMap.get(payerId).add(e.getAmount()));
        }

        for (Member m : members) {
            balanceMap.put(m.getId(), balanceMap.get(m.getId()).subtract(perHead));
        }

        // Step 4: Separate creditors and debtors
        List<Map.Entry<Long, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<Long, BigDecimal>> debtors = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : balanceMap.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) creditors.add(entry);
            else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) debtors.add(entry);
        }

        creditors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        debtors.sort((a, b) -> a.getValue().compareTo(b.getValue()));

        // Step 5: Greedy settlement
        List<String> settlements = new ArrayList<>();
        int i = 0, j = 0;

        while (i < creditors.size() && j < debtors.size()) {
            Long creditorId = creditors.get(i).getKey();
            Long debtorId = debtors.get(j).getKey();

            BigDecimal credit = creditors.get(i).getValue();
            BigDecimal debt = debtors.get(j).getValue().negate();

            BigDecimal settledAmount = credit.min(debt);

            Member creditor = members.stream().filter(m -> m.getId().equals(creditorId)).findFirst().orElse(null);
            Member debtor = members.stream().filter(m -> m.getId().equals(debtorId)).findFirst().orElse(null);

            if (creditor != null && debtor != null) {
                settlements.add(debtor.getName() + " should pay ₹" +
                        settledAmount.setScale(2, RoundingMode.HALF_UP) + " to " + creditor.getName());
            }

            creditors.get(i).setValue(credit.subtract(settledAmount));
            debtors.get(j).setValue(debt.subtract(settledAmount).negate());

            if (credit.subtract(settledAmount).compareTo(BigDecimal.ZERO) == 0) i++;
            if (debt.subtract(settledAmount).compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return settlements;
    }
}
