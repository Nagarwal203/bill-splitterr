package com.example.billsplitter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.billsplitter.model.Expense;
import com.example.billsplitter.model.Grp;
import com.example.billsplitter.model.Member;
import com.example.billsplitter.repository.ExpenseRepository;
import com.example.billsplitter.repository.GrpRepository;
import com.example.billsplitter.repository.MemberRepository;
import com.example.billsplitter.service.SettlementService;

@RestController
@RequestMapping("/api")
public class BillSplitterController {

    private final GrpRepository grpRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementService settlementService;

    public BillSplitterController(
            GrpRepository grpRepository,
            MemberRepository memberRepository,
            ExpenseRepository expenseRepository,
            SettlementService settlementService
    ) {
        this.grpRepository = grpRepository;
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
        this.settlementService = settlementService;
    }

    // ------------------- GROUP APIs -------------------

    @PostMapping("/groups")
    public Grp createGroup(@RequestParam String name, @RequestParam String description) {
        Grp grp = new Grp();
        grp.setName(name);
        grp.setDescription(description);
        return grpRepository.save(grp);
    }

    @GetMapping("/groups")
    public List<Grp> getAllGroups() {
        return grpRepository.findAll();
    }

    // ------------------- MEMBER APIs -------------------

    @PostMapping("/groups/{grpId}/members")
    public Member addMember(
            @PathVariable Long grpId,
            @RequestParam String name,
            @RequestParam int age,
            @RequestParam String contact
    ) {
        Grp grp = grpRepository.findById(grpId).orElseThrow(() -> new RuntimeException("Group not found"));
        Member member = new Member();
        member.setName(name);
        member.setAge(age);
        member.setContact(contact);
        member.setGrp(grp);
        return memberRepository.save(member);
    }

    @GetMapping("/groups/{grpId}/members")
    public List<Member> getMembers(@PathVariable Long grpId) {
        return memberRepository.findByGrpId(grpId);
    }

    // ------------------- EXPENSE APIs -------------------

    @PostMapping("/groups/{grpId}/expenses")
    public Expense addExpense(
            @PathVariable Long grpId,
            @RequestParam Long payerId,
            @RequestParam double amount,
            @RequestParam String description
    ) {
        Grp grp = grpRepository.findById(grpId).orElseThrow(() -> new RuntimeException("Group not found"));
        Member payer = memberRepository.findById(payerId).orElseThrow(() -> new RuntimeException("Member not found"));
        Expense expense = new Expense();
        expense.setGrp(grp);
        expense.setPayer(payer);
        expense.setAmount(java.math.BigDecimal.valueOf(amount));
        expense.setDescription(description);
        return expenseRepository.save(expense);
    }

    @GetMapping("/groups/{grpId}/expenses")
    public List<Expense> getExpenses(@PathVariable Long grpId) {
        return expenseRepository.findByGrpId(grpId);
    }

    // ------------------- SETTLEMENT API -------------------

    @GetMapping("/groups/{grpId}/settlements")
    public List<String> getSettlements(@PathVariable Long grpId) {
        return settlementService.calculateSettlements(grpId);
    }
}
