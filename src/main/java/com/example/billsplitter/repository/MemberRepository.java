package com.example.billsplitter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billsplitter.model.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByGrpId(Long grpId);
}
