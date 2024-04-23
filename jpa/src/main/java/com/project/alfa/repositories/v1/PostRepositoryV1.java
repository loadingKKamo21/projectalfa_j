package com.project.alfa.repositories.v1;

import com.project.alfa.entities.Post;
import com.project.alfa.repositories.dto.SearchParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Repository
public class PostRepositoryV1 {
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * 게시글 저장
     *
     * @param post - 게시글 정보
     * @return 게시글 정보
     */
    public Post save(final Post post) {
        em.persist(post);
        return post;
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보
     */
    public Optional<Post> findById(final Long id) {
        return Optional.ofNullable(em.createQuery("SELECT p FROM Post p WHERE p.id = :id", Post.class)
                                     .setParameter("id", id)
                                     .getResultList().stream().findFirst().orElse(null));
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보
     */
    public Optional<Post> findById(final Long id, final boolean deleteYn) {
        return Optional.ofNullable(
                em.createQuery("SELECT p FROM Post p WHERE p.id = :id AND p.deleteYn = :deleteYn", Post.class)
                  .setParameter("id", id)
                  .setParameter("deleteYn", deleteYn)
                  .getResultList().stream().findFirst().orElse(null)
        );
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @return 게시글 정보 목록
     */
    public List<Post> findAll() {
        return em.createQuery("SELECT p FROM Post p", Post.class).getResultList();
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final boolean deleteYn) {
        return em.createQuery("SELECT p FROM Post p WHERE p.deleteYn = :deleteYn", Post.class)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final List<Long> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return em.createQuery("SELECT p FROM Post p WHERE p.id IN :ids", Post.class)
                 .setParameter("ids", ids)
                 .getResultList();
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final List<Long> ids, final boolean deleteYn) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return em.createQuery("SELECT p FROM Post p WHERE p.id IN :ids AND p.deleteYn = :deleteYn", Post.class)
                 .setParameter("ids", ids)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final Long writerId) {
        return em.createQuery("SELECT p FROM Post p WHERE p.writer.id = :writerId", Post.class)
                 .setParameter("writerId", writerId)
                 .getResultList();
    }
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final Long writerId, final boolean deleteYn) {
        return em.createQuery("SELECT p FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn",
                              Post.class)
                 .setParameter("writerId", writerId)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(Pageable pageable) {
        String contentJpql = "SELECT p FROM Post p" + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(p) FROM Post p";
        
        TypedQuery<Post> contentQuery = em.createQuery(contentJpql, Post.class);
        TypedQuery<Long> countQuery   = em.createQuery(countJpql, Long.class);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Post>(contentQuery.setFirstResult((int) pageable.getOffset())
                                              .setMaxResults(pageable.getPageSize())
                                              .getResultList(), pageable, count);
    }
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(final boolean deleteYn, Pageable pageable) {
        String contentJpql = "SELECT p FROM Post p WHERE p.deleteYn = :deleteYn" + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(p) FROM Post p WHERE p.deleteYn = :deleteYn";
        
        TypedQuery<Post> contentQuery = em.createQuery(contentJpql, Post.class);
        TypedQuery<Long> countQuery   = em.createQuery(countJpql, Long.class);
        contentQuery.setParameter("deleteYn", deleteYn);
        countQuery.setParameter("deleteYn", deleteYn);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Post>(contentQuery.setFirstResult((int) pageable.getOffset())
                                              .setMaxResults(pageable.getPageSize())
                                              .getResultList(), pageable, count);
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(final Long writerId, Pageable pageable) {
        String contentJpql = "SELECT p FROM Post p WHERE p.writer.id = :writerId" + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(p) FROM Post p WHERE p.writer.id = :writerId";
        
        TypedQuery<Post> contentQuery = em.createQuery(contentJpql, Post.class);
        TypedQuery<Long> countQuery   = em.createQuery(countJpql, Long.class);
        contentQuery.setParameter("writerId", writerId);
        countQuery.setParameter("writerId", writerId);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Post>(contentQuery.setFirstResult((int) pageable.getOffset())
                                              .setMaxResults(pageable.getPageSize())
                                              .getResultList(), pageable, count);
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(final Long writerId, final boolean deleteYn, Pageable pageable) {
        String contentJpql = "SELECT p FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn" + getSortingJpql(pageable);
        String countJpql = "SELECT COUNT(p) FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn";
        
        TypedQuery<Post> contentQuery = em.createQuery(contentJpql, Post.class);
        TypedQuery<Long> countQuery   = em.createQuery(countJpql, Long.class);
        contentQuery.setParameter("writerId", writerId);
        contentQuery.setParameter("deleteYn", deleteYn);
        countQuery.setParameter("writerId", writerId);
        countQuery.setParameter("deleteYn", deleteYn);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Post>(contentQuery.setFirstResult((int) pageable.getOffset())
                                              .setMaxResults(pageable.getPageSize())
                                              .getResultList(), pageable, count);
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(final SearchParam param, Pageable pageable) {
        String contentJpql = "SELECT p FROM Post p" + getSearchingJpql(param) + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(p) FROM Post p" + getSearchingJpql(param);
        
        TypedQuery<Post> contentQuery = em.createQuery(contentJpql, Post.class);
        TypedQuery<Long> countQuery   = em.createQuery(countJpql, Long.class);
        if (StringUtils.hasText(param.getSearchKeyword())) {
            List<String> keywords = param.getKeywords();
            if (keywords.size() == 1) {
                contentQuery.setParameter("keyword", param.getSearchKeyword());
                countQuery.setParameter("keyword", param.getSearchKeyword());
            } else if (keywords.size() >= 2)
                for (int i = 1; i <= keywords.size(); i++) {
                    contentQuery.setParameter("keyword" + i, keywords.get(i - 1));
                    countQuery.setParameter("keyword" + i, keywords.get(i - 1));
                }
        }
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Post>(contentQuery.setFirstResult((int) pageable.getOffset())
                                              .setMaxResults(pageable.getPageSize())
                                              .getResultList(), pageable, count);
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(final SearchParam param, final boolean deleteYn, Pageable pageable) {
        String contentJpql = "SELECT p FROM Post p" + getSearchingJpql(param) + deleteYnJpql(param) + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(p) FROM Post p" + getSearchingJpql(param) + deleteYnJpql(param);
        
        TypedQuery<Post> contentQuery = em.createQuery(contentJpql, Post.class);
        TypedQuery<Long> countQuery   = em.createQuery(countJpql, Long.class);
        if (StringUtils.hasText(param.getSearchKeyword())) {
            List<String> keywords = param.getKeywords();
            if (keywords.size() == 1) {
                contentQuery.setParameter("keyword", param.getSearchKeyword());
                countQuery.setParameter("keyword", param.getSearchKeyword());
            } else if (keywords.size() >= 2)
                for (int i = 1; i <= keywords.size(); i++) {
                    contentQuery.setParameter("keyword" + i, keywords.get(i - 1));
                    countQuery.setParameter("keyword" + i, keywords.get(i - 1));
                }
        }
        contentQuery.setParameter("deleteYn", deleteYn);
        countQuery.setParameter("deleteYn", deleteYn);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Post>(contentQuery.setFirstResult((int) pageable.getOffset())
                                              .setMaxResults(pageable.getPageSize())
                                              .getResultList(), pageable, count);
    }
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param post - 게시글 정보
     */
    public void delete(final Post post) {
        em.remove(em.find(Post.class, post.getId()));
    }
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param id - PK
     */
    public void deleteById(final Long id) {
        em.remove(em.find(Post.class, id));
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param posts - 게시글 정보 목록
     */
    public void deleteAll(final List<Post> posts) {
        for (Post post : posts)
            em.remove(post);
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param posts - 게시글 정보 목록
     */
    public void deleteAllInBatch(final List<Post> posts) {
        List<Long> ids = posts.stream().map(Post::getId).collect(toList());
        em.createQuery("DELETE FROM Post p WHERE p.id IN :ids").setParameter("ids", ids).executeUpdate();
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllById(final List<Long> ids) {
        for (Long id : ids)
            em.remove(em.find(Post.class, id));
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllByIdInBatch(final List<Long> ids) {
        em.createQuery("DELETE FROM Post p WHERE p.id IN :ids").setParameter("ids", ids).executeUpdate();
    }
    
    //==================== 조건문 생성 메서드 ====================//
    
    /**
     * 정렬 조건에 따른 조건문 생성
     *
     * @param pageable - 페이징 객체
     * @return JPQL
     */
    private String getSortingJpql(Pageable pageable) {
        StringBuilder sb     = new StringBuilder();
        String        prefix = " p.";
        String        regex  = "^(?!\\s*$)(?!.* (ASC|DESC)$).+$";
        
        Sort sort = pageable.getSort();
        sb.append(" ORDER BY");
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                String         property  = order.getProperty();
                Sort.Direction direction = order.getDirection();
                
                switch (property) {
                    case "viewCount":
                        sb.append(prefix + "viewCount");
                        break;
                    case "createdDate":
                        sb.append(prefix + "createdDate");
                        break;
                    case "lastModifiedDate":
                        sb.append(prefix + "lastModifiedDate");
                        break;
                    default:
                        break;
                }
                
                if (Pattern.compile(regex).matcher(sb.toString()).matches()) {
                    if (direction.isAscending())
                        sb.append(" ASC");
                    else
                        sb.append(" DESC");
                }
            }
        } else
            sb.append(prefix + "createdDate DESC");
        
        return sb.toString();
    }
    
    /**
     * 검색 조건, 키워드에 따른 조건문 생성
     *
     * @param param - 검색 조건, 키워드
     * @return JPQL
     */
    private String getSearchingJpql(final SearchParam param) {
        StringBuilder sb              = new StringBuilder();
        String        searchCondition = param.getSearchCondition();
        List<String>  keywords        = param.getKeywords();
        
        String prefix  = " p.";
        String title   = prefix + "title LIKE CONCAT('%%', %s, '%%')";
        String content = prefix + "content LIKE CONCAT('%%', %s, '%%')";
        String writer  = prefix + "writer.nickname LIKE CONCAT('%%', %s, '%%')";
        
        if (StringUtils.hasText(param.getSearchKeyword()) && !keywords.isEmpty()) {
            sb.append(" WHERE");
            if (keywords.size() == 1)
                switch (searchCondition) {
                    case "title":
                        sb.append(String.format(title, ":keyword"));
                        break;
                    case "content":
                        sb.append(String.format(content, ":keyword"));
                        break;
                    case "titleOrContent":
                        sb.append(" (");
                        sb.append(String.format(title, ":keyword")).append(" OR")
                          .append(String.format(content, ":keyword"));
                        sb.append(")");
                        break;
                    case "writer":
                        sb.append(String.format(writer, ":keyword"));
                        break;
                    default:
                        sb.append(" (");
                        sb.append(String.format(title, ":keyword")).append(" OR")
                          .append(String.format(content, ":keyword")).append(" OR")
                          .append(String.format(writer, ":keyword"));
                        sb.append(")");
                        break;
                }
            else if (keywords.size() >= 2)
                for (int i = 1; i <= keywords.size(); i++) {
                    sb.append(" (");
                    switch (searchCondition) {
                        case "title":
                            sb.append(String.format(title, ":keyword" + i));
                            break;
                        case "content":
                            sb.append(String.format(content, ":keyword" + i));
                            break;
                        case "titleOrContent":
                            sb.append(" (");
                            sb.append(String.format(title, ":keyword" + i)).append(" OR")
                              .append(String.format(content, ":keyword" + i));
                            sb.append(")");
                            break;
                        case "writer":
                            sb.append(String.format(writer, ":keyword" + i));
                            break;
                        default:
                            sb.append(" (");
                            sb.append(String.format(title, ":keyword" + i)).append(" OR")
                              .append(String.format(content, ":keyword" + i)).append(" OR")
                              .append(String.format(writer, ":keyword" + i));
                            sb.append(")");
                            break;
                    }
                    sb.append(") OR");
                }
        }
        
        if (sb.toString().endsWith(" OR"))
            sb.delete(sb.length() - 3, sb.length());
        
        return sb.toString();
    }
    
    /**
     * 검색 조건, 키워드에 따라 삭제 여부 조건문 생성
     *
     * @param param - 검색 조건, 키워드
     * @return JPQL
     */
    private String deleteYnJpql(final SearchParam param) {
        StringBuilder sb     = new StringBuilder();
        String        prefix = " p.";
        
        if (StringUtils.hasText(param.getSearchKeyword()) && !param.getKeywords().isEmpty())
            sb.append(" AND " + prefix + "deleteYn = :deleteYn");
        else
            sb.append(" WHERE " + prefix + "deleteYn = :deleteYn");
        
        return sb.toString();
    }
    
}
