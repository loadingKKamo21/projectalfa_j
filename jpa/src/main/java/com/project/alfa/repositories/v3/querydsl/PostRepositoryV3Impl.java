package com.project.alfa.repositories.v3.querydsl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.QPost;
import com.project.alfa.repositories.dto.SearchParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryV3Impl implements PostRepositoryV3Custom {
    
    private final JPAQueryFactory jpaQueryFactory;
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    @Override
    public Page<Post> findAll(SearchParam param, Pageable pageable) {
        List<Post> content = jpaQueryFactory.selectFrom(QPost.post)
                                            .where(getSearchCondition(param))
                                            .orderBy(getSortCondition(pageable))
                                            .offset(pageable.getOffset())
                                            .limit(pageable.getPageSize())
                                            .fetch();
        JPAQuery<Long> count = jpaQueryFactory.select(QPost.post.count())
                                              .from(QPost.post)
                                              .where(getSearchCondition(param));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    @Override
    public Page<Post> findAll(SearchParam param, boolean deleteYn, Pageable pageable) {
        List<Post> content = jpaQueryFactory.selectFrom(QPost.post)
                                            .where(getSearchCondition(param), QPost.post.deleteYn.eq(deleteYn))
                                            .orderBy(getSortCondition(pageable))
                                            .offset(pageable.getOffset())
                                            .limit(pageable.getPageSize())
                                            .fetch();
        JPAQuery<Long> count = jpaQueryFactory.select(QPost.post.count())
                                              .from(QPost.post)
                                              .where(getSearchCondition(param));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 검색 조건, 키워드에 따라 BooleanExpression 생성
     *
     * @param param - 검색 조건, 키워드
     * @return
     */
    private BooleanExpression getSearchCondition(final SearchParam param) {
        String       searchKeyword   = param.getSearchKeyword();
        String       searchCondition = param.getSearchCondition();
        List<String> keywords        = param.getKeywords();
        
        if (!StringUtils.hasText(searchKeyword) || !keywords.isEmpty()) {
            if (keywords.size() == 1) {
                BooleanExpression titleExpression   = QPost.post.title.like("%" + searchKeyword + "%");
                BooleanExpression contentExpression = QPost.post.content.like("%" + searchKeyword + "%");
                BooleanExpression writerExpression  = QPost.post.writer.nickname.like("%" + searchKeyword + "%");
                
                switch (searchCondition) {
                    case "title":
                        return titleExpression;
                    case "content":
                        return contentExpression;
                    case "titleOrContent":
                        return titleExpression.or(contentExpression);
                    case "writer":
                        return writerExpression;
                    default:
                        return titleExpression.or(contentExpression).or(writerExpression);
                }
            } else if (keywords.size() >= 2) {
                BooleanExpression expression = null;
                
                for (int i = 1; i <= keywords.size(); i++) {
                    BooleanExpression titleExpression   = QPost.post.title.like("%" + keywords.get(i - 1) + "%");
                    BooleanExpression contentExpression = QPost.post.content.like("%" + keywords.get(i - 1) + "%");
                    BooleanExpression writerExpression = QPost.post.writer.nickname.like("%" + keywords.get(i - 1) + "%");
                    
                    switch (searchCondition) {
                        case "title":
                            expression = i == 1 ? titleExpression : expression.or(titleExpression);
                            break;
                        case "content":
                            expression = i == 1 ? contentExpression : expression.or(contentExpression);
                            break;
                        case "titleOrContent":
                            expression = i == 1 ? titleExpression.or(contentExpression) : expression.or(titleExpression.or(contentExpression));
                            break;
                        case "writer":
                            expression = i == 1 ? writerExpression : expression.or(writerExpression);
                            break;
                        default:
                            expression = i == 1 ? titleExpression.or(contentExpression).or(writerExpression) : expression.or(titleExpression.or(contentExpression).or(writerExpression));
                            break;
                    }
                }
                
                return expression;
            }
        }
        
        return null;
    }
    
    /**
     * 페이징 객체에 포함된 정렬 조건에 따라 OrderSpecifier 목록 생성
     *
     * @param pageable - 페이징 객체
     * @return
     */
    private OrderSpecifier<?>[] getSortCondition(Pageable pageable) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        
        if (!pageable.getSort().isEmpty())
            pageable.getSort().forEach(order -> {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                
                switch (order.getProperty()) {
                    case "viewCount":
                        orderSpecifiers.add(new OrderSpecifier(direction, QPost.post.viewCount));
                        break;
                    case "createdDate":
                        orderSpecifiers.add(new OrderSpecifier(direction, QPost.post.createdDate));
                        break;
                    case "lastModifiedDate":
                        orderSpecifiers.add(new OrderSpecifier(direction, QPost.post.lastModifiedDate));
                        break;
                    default:
                        break;
                }
            });
        
        if (orderSpecifiers.isEmpty())
            orderSpecifiers.add(new OrderSpecifier(Order.DESC, QPost.post.createdDate));
        
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
    
}
