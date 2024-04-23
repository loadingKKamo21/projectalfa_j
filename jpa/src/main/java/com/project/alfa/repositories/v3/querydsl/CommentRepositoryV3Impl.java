package com.project.alfa.repositories.v3.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryV3Impl implements CommentRepositoryV3Custom {
    
    private final JPAQueryFactory jpaQueryFactory;
    
}
