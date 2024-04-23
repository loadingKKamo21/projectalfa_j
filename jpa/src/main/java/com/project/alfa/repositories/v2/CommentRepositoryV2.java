package com.project.alfa.repositories.v2;

import com.project.alfa.entities.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryV2 {
    
    private final CommentJpaRepository commentJpaRepository;
    
    /**
     * 댓글 저장
     *
     * @param comment - 댓글 정보
     * @return 댓글 정보
     */
    public Comment save(final Comment comment) {
        return commentJpaRepository.save(comment);
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id - PK
     * @return 댓글 정보
     */
    public Optional<Comment> findById(final Long id) {
        return commentJpaRepository.findById(id);
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보
     */
    public Optional<Comment> findById(final Long id, final boolean deleteYn) {
        return commentJpaRepository.findByIdAndDeleteYn(id, deleteYn);
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @return 댓글 정보 목록
     */
    public List<Comment> findAll() {
        return commentJpaRepository.findAll();
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    public List<Comment> findAll(final boolean deleteYn) {
        return commentJpaRepository.findAllByDeleteYn(deleteYn);
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
        return commentJpaRepository.findAllByIdIn(ids);
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
        return commentJpaRepository.findAllByIdInAndDeleteYn(ids, deleteYn);
    }
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 댓글 정보 목록
     */
    public List<Comment> findAllByWriter(final Long writerId) {
        return commentJpaRepository.findAllByWriter_Id(writerId);
    }
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    public List<Comment> findAllByWriter(final Long writerId, final boolean deleteYn) {
        return commentJpaRepository.findAllByWriter_IdAndDeleteYn(writerId, deleteYn);
    }
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 댓글 정보 목록
     */
    public List<Comment> findAllByPost(final Long postId) {
        return commentJpaRepository.findAllByPost_Id(postId);
    }
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    public List<Comment> findAllByPost(final Long postId, final boolean deleteYn) {
        return commentJpaRepository.findAllByPost_IdAndDeleteYn(postId, deleteYn);
    }
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    public Page<Comment> findAllByWriter(final Long writerId, final Pageable pageable) {
        return commentJpaRepository.findAllByWriter_IdOrderByCreatedDateDesc(writerId, pageable);
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
        return commentJpaRepository.findAllByWriter_IdAndDeleteYnOrderByCreatedDateDesc(writerId, deleteYn, pageable);
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    public Page<Comment> findAllByPost(final Long postId, final Pageable pageable) {
        return commentJpaRepository.findAllByPost_IdOrderByCreatedDateDesc(postId, pageable);
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
        return commentJpaRepository.findAllByPost_IdAndDeleteYnOrderByCreatedDateDesc(postId, deleteYn, pageable);
    }
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param comment - 댓글 정보
     */
    public void delete(final Comment comment) {
        commentJpaRepository.delete(comment);
    }
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param id - PK
     */
    public void deleteById(final Long id) {
        commentJpaRepository.deleteById(id);
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param comments - 댓글 정보 목록
     */
    public void deleteAll(final List<Comment> comments) {
        commentJpaRepository.deleteAll(comments);
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param comments - 댓글 정보 목록
     */
    public void deleteAllInBatch(final List<Comment> comments) {
        commentJpaRepository.deleteAllInBatch(comments);
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllById(final List<Long> ids) {
        commentJpaRepository.deleteAllById(ids);
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllByIdInBatch(final List<Long> ids) {
        commentJpaRepository.deleteAllByIdInBatch(ids);
    }
    
}
