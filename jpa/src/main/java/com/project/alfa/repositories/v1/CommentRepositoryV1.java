package com.project.alfa.repositories.v1;

import com.project.alfa.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Repository
public class CommentRepositoryV1 {
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * 댓글 저장
     *
     * @param comment - 댓글 정보
     * @return 댓글 정보
     */
    public Comment save(final Comment comment) {
        em.persist(comment);
        return comment;
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id - PK
     * @return 댓글 정보
     */
    public Optional<Comment> findById(final Long id) {
        return Optional.ofNullable(em.createQuery("SELECT c FROM Comment c WHERE c.id = :id", Comment.class)
                                     .setParameter("id", id)
                                     .getResultList().stream().findFirst().orElse(null));
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보
     */
    public Optional<Comment> findById(final Long id, final boolean deleteYn) {
        return Optional.ofNullable(
                em.createQuery("SELECT c FROM Comment c WHERE c.id = :id AND c.deleteYn = :deleteYn", Comment.class)
                  .setParameter("id", id)
                  .setParameter("deleteYn", deleteYn)
                  .getResultList().stream().findFirst().orElse(null)
        );
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @return 댓글 정보 목록
     */
    public List<Comment> findAll() {
        return em.createQuery("SELECT c FROM Comment c", Comment.class).getResultList();
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    public List<Comment> findAll(final boolean deleteYn) {
        return em.createQuery("SELECT c FROM Comment c WHERE c.deleteYn = :deleteYn", Comment.class)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 댓글 정보 목록
     */
    public List<Comment> findAll(final List<Long> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return em.createQuery("SELECT c FROM Comment c WHERE c.id IN :ids", Comment.class)
                 .setParameter("ids", ids)
                 .getResultList();
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    public List<Comment> findAll(final List<Long> ids, final boolean deleteYn) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return em.createQuery("SELECT c FROM Comment c WHERE c.id IN :ids AND c.deleteYn = :deleteYn", Comment.class)
                 .setParameter("ids", ids)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 댓글 정보 목록
     */
    public List<Comment> findAllByWriter(final Long writerId) {
        return em.createQuery("SELECT c FROM Comment c WHERE c.writer.id = :writerId", Comment.class)
                 .setParameter("writerId", writerId)
                 .getResultList();
    }
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    public List<Comment> findAllByWriter(final Long writerId, final boolean deleteYn) {
        return em.createQuery("SELECT c FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn",
                              Comment.class)
                 .setParameter("writerId", writerId)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 댓글 정보 목록
     */
    public List<Comment> findAllByPost(final Long postId) {
        return em.createQuery("SELECT c FROM Comment c WHERE c.post.id = :postId", Comment.class)
                 .setParameter("postId", postId)
                 .getResultList();
    }
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    public List<Comment> findAllByPost(final Long postId, final boolean deleteYn) {
        return em.createQuery("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn",
                              Comment.class)
                 .setParameter("postId", postId)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    public Page<Comment> findAllByWriter(final Long writerId, final Pageable pageable) {
        String contentJpql = "SELECT c FROM Comment c WHERE c.writer.id = :writerId" + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(c) FROM Comment c WHERE c.writer.id = :writerId";
        
        TypedQuery<Comment> contentQuery = em.createQuery(contentJpql, Comment.class);
        TypedQuery<Long>    countQuery   = em.createQuery(countJpql, Long.class);
        contentQuery.setParameter("writerId", writerId);
        countQuery.setParameter("writerId", writerId);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Comment>(contentQuery.setFirstResult((int) pageable.getOffset())
                                                 .setMaxResults(pageable.getPageSize())
                                                 .getResultList(), pageable, count);
    }
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    public Page<Comment> findAllByWriter(final Long writerId, final boolean deleteYn, final Pageable pageable) {
        String contentJpql = "SELECT c FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn" + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(c) FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn";
        
        TypedQuery<Comment> contentQuery = em.createQuery(contentJpql, Comment.class);
        TypedQuery<Long>    countQuery   = em.createQuery(countJpql, Long.class);
        contentQuery.setParameter("writerId", writerId);
        contentQuery.setParameter("deleteYn", deleteYn);
        countQuery.setParameter("writerId", writerId);
        countQuery.setParameter("deleteYn", deleteYn);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Comment>(contentQuery.setFirstResult((int) pageable.getOffset())
                                                 .setMaxResults(pageable.getPageSize())
                                                 .getResultList(), pageable, count);
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    public Page<Comment> findAllByPost(final Long postId, final Pageable pageable) {
        String contentJpql = "SELECT c FROM Comment c WHERE c.post.id = :postId" + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId";
        
        TypedQuery<Comment> contentQuery = em.createQuery(contentJpql, Comment.class);
        TypedQuery<Long>    countQuery   = em.createQuery(countJpql, Long.class);
        contentQuery.setParameter("postId", postId);
        countQuery.setParameter("postId", postId);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Comment>(contentQuery.setFirstResult((int) pageable.getOffset())
                                                 .setMaxResults(pageable.getPageSize())
                                                 .getResultList(), pageable, count);
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    public Page<Comment> findAllByPost(final Long postId, final boolean deleteYn, final Pageable pageable) {
        String contentJpql = "SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn" + getSortingJpql(pageable);
        String countJpql   = "SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn";
        
        TypedQuery<Comment> contentQuery = em.createQuery(contentJpql, Comment.class);
        TypedQuery<Long>    countQuery   = em.createQuery(countJpql, Long.class);
        contentQuery.setParameter("postId", postId);
        contentQuery.setParameter("deleteYn", deleteYn);
        countQuery.setParameter("postId", postId);
        countQuery.setParameter("deleteYn", deleteYn);
        
        Long count = countQuery.getSingleResult();
        
        return new PageImpl<Comment>(contentQuery.setFirstResult((int) pageable.getOffset())
                                                 .setMaxResults(pageable.getPageSize())
                                                 .getResultList(), pageable, count);
    }
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param comment - 댓글 정보
     */
    public void delete(final Comment comment) {
        em.remove(em.find(Comment.class, comment.getId()));
    }
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param id - PK
     */
    public void deleteById(final Long id) {
        em.remove(em.find(Comment.class, id));
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param comments - 댓글 정보 목록
     */
    public void deleteAll(final List<Comment> comments) {
        for (Comment comment : comments)
            em.remove(comment);
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param comments - 댓글 정보 목록
     */
    public void deleteAllInBatch(final List<Comment> comments) {
        List<Long> ids = comments.stream().map(Comment::getId).collect(toList());
        em.createQuery("DELETE FROM Comment c WHERE c.id IN :ids").setParameter("ids", ids).executeUpdate();
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllById(final List<Long> ids) {
        for (Long id : ids)
            em.remove(em.find(Comment.class, id));
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllByIdInBatch(final List<Long> ids) {
        em.createQuery("DELETE FROM Comment c WHERE c.id IN :ids").setParameter("ids", ids).executeUpdate();
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
        String        prefix = " c.";
        String        regex  = "^(?!\\s*$)(?!.* (ASC|DESC)$).+$";
        
        Sort sort = pageable.getSort();
        sb.append(" ORDER BY");
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                String         property  = order.getProperty();
                Sort.Direction direction = order.getDirection();
                
                switch (property) {
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
    
}
