package com.project.alfa.repositories.v2.specification;

import com.project.alfa.entities.Post;
import com.project.alfa.repositories.dto.SearchParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class PostSpecification {
    
    /**
     * 검색 조건, 키워드에 따라 게시글 Specification 생성
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return
     */
    public static Specification<Post> searchAndSortSpecification(final SearchParam param, Pageable pageable) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            Predicate searchCondition = getSearchCondition(param, root, criteriaBuilder);
            if (searchCondition != null)
                predicates.add(searchCondition);
            
            query.orderBy(getSortCondition(pageable, root, criteriaBuilder));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * 검색 조건, 키워드, 삭제 여부에 따라 게시글 Specification 생성
     *
     * @param param    - 검색 조건, 키워드
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return
     */
    public static Specification<Post> searchAndSortSpecification(final SearchParam param,
                                                                 final boolean deleteYn,
                                                                 Pageable pageable) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            Predicate searchCondition = getSearchCondition(param, root, criteriaBuilder);
            if (searchCondition != null)
                predicates.add(searchCondition);
            
            predicates.add(criteriaBuilder.equal(root.get("deleteYn"), deleteYn));
            
            query.orderBy(getSortCondition(pageable, root, criteriaBuilder));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * 검색 조건, 키워드에 따라 Predicate 생성
     *
     * @param param           - 검색 조건, 키워드
     * @param root
     * @param criteriaBuilder
     * @return
     */
    private static Predicate getSearchCondition(final SearchParam param,
                                                Root<Post> root,
                                                CriteriaBuilder criteriaBuilder) {
        String       searchKeyword   = param.getSearchKeyword();
        String       searchCondition = param.getSearchCondition();
        List<String> keywords        = param.getKeywords();
        
        if (!StringUtils.hasText(searchKeyword) || keywords.isEmpty())
            return null;
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (keywords.size() == 1)
            switch (searchCondition) {
                case "title":
                    predicates.add(criteriaBuilder.like(root.get("title"), "%" + searchKeyword + "%"));
                    break;
                case "content":
                    predicates.add(criteriaBuilder.like(root.get("content"), "%" + searchKeyword + "%"));
                    break;
                case "titleOrContent":
                    predicates.add(criteriaBuilder.like(root.get("title"), "%" + searchKeyword + "%"));
                    predicates.add(criteriaBuilder.like(root.get("content"), "%" + searchKeyword + "%"));
                    break;
                case "writer":
                    predicates.add(criteriaBuilder.like(root.get("writer").get("nickname"), "%" + searchKeyword + "%"));
                    break;
                default:
                    predicates.add(criteriaBuilder.like(root.get("title"), "%" + searchKeyword + "%"));
                    predicates.add(criteriaBuilder.like(root.get("content"), "%" + searchKeyword + "%"));
                    predicates.add(criteriaBuilder.like(root.get("writer").get("nickname"), "%" + searchKeyword + "%"));
                    break;
            }
        else if (keywords.size() >= 2)
            for (int i = 1; i <= keywords.size(); i++)
                switch (searchCondition) {
                    case "title":
                        predicates.add(criteriaBuilder.like(root.get("title"), "%" + keywords.get(i - 1) + "%"));
                        break;
                    case "content":
                        predicates.add(criteriaBuilder.like(root.get("content"), "%" + keywords.get(i - 1) + "%"));
                        break;
                    case "titleOrContent":
                        predicates.add(criteriaBuilder.like(root.get("title"), "%" + keywords.get(i - 1) + "%"));
                        predicates.add(criteriaBuilder.like(root.get("content"), "%" + keywords.get(i - 1) + "%"));
                        break;
                    case "writer":
                        predicates.add(
                                criteriaBuilder.like(root.get("writer").get("nickname"),
                                                     "%" + keywords.get(i - 1) + "%"));
                        break;
                    default:
                        predicates.add(criteriaBuilder.like(root.get("title"), "%" + keywords.get(i - 1) + "%"));
                        predicates.add(criteriaBuilder.like(root.get("content"), "%" + keywords.get(i - 1) + "%"));
                        predicates.add(criteriaBuilder.like(root.get("writer").get("nickname"),
                                                            "%" + keywords.get(i - 1) + "%"));
                        break;
                }
        
        return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
    }
    
    /**
     * 페이징 객체에 포함된 정렬 조건에 따라 Order 목록 생성
     *
     * @param pageable        - 페이징 객체
     * @param root
     * @param criteriaBuilder
     * @return
     */
    private static List<Order> getSortCondition(Pageable pageable, Root<Post> root, CriteriaBuilder criteriaBuilder) {
        List<Order> orders = new ArrayList<>();
        
        if (!pageable.getSort().isEmpty())
            pageable.getSort().forEach(order -> {
                switch (order.getProperty()) {
                    case "viewCount":
                        if (order.getDirection().isAscending())
                            orders.add(criteriaBuilder.asc(root.get("viewCount")));
                        else
                            orders.add(criteriaBuilder.desc(root.get("viewCount")));
                        break;
                    case "createdDate":
                        if (order.getDirection().isAscending())
                            orders.add(criteriaBuilder.asc(root.get("createdDate")));
                        else
                            orders.add(criteriaBuilder.desc(root.get("createdDate")));
                        break;
                    case "lastModifiedDate":
                        if (order.getDirection().isAscending())
                            orders.add(criteriaBuilder.asc(root.get("lastModifiedDate")));
                        else
                            orders.add(criteriaBuilder.asc(root.get("lastModifiedDate")));
                        break;
                    default:
                        break;
                }
            });
        
        if (orders.isEmpty())
            orders.add(criteriaBuilder.desc(root.get("createdDate")));
        
        return orders;
    }
    
}
