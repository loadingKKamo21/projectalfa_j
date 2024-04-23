package com.project.alfa.repositories.v3.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryV3Impl implements MemberRepositoryV3Custom {
    
    private final JPAQueryFactory jpaQueryFactory;
    
}
