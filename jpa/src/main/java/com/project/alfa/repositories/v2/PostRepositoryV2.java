package com.project.alfa.repositories.v2;

import com.project.alfa.entities.Post;
import com.project.alfa.repositories.dto.SearchParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.project.alfa.repositories.v2.specification.PostSpecification.searchAndSortSpecification;

@Repository
@RequiredArgsConstructor
public class PostRepositoryV2 {
    
    private final PostJpaRepository postJpaRepository;
    
    /**
     * 게시글 저장
     *
     * @param post - 게시글 정보
     * @return 게시글 정보
     */
    public Post save(final Post post) {
        return postJpaRepository.save(post);
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보
     */
    public Optional<Post> findById(final Long id) {
        return postJpaRepository.findById(id);
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보
     */
    public Optional<Post> findById(final Long id, final boolean deleteYn) {
        return postJpaRepository.findByIdAndDeleteYn(id, deleteYn);
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @return 게시글 정보 목록
     */
    public List<Post> findAll() {
        return postJpaRepository.findAll();
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final boolean deleteYn) {
        return postJpaRepository.findAllByDeleteYn(deleteYn);
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final List<Long> ids) {
        return postJpaRepository.findAllByIdIn(ids);
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final List<Long> ids, final boolean deleteYn) {
        return postJpaRepository.findAllByIdInAndDeleteYn(ids, deleteYn);
    }
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final Long writerId) {
        return postJpaRepository.findAllByWriter_Id(writerId);
    }
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    public List<Post> findAll(final Long writerId, final boolean deleteYn) {
        return postJpaRepository.findAllByWriter_IdAndDeleteYn(writerId, deleteYn);
    }
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(Pageable pageable) {
        return postJpaRepository.findAllByOrderByCreatedDateDesc(pageable);
    }
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(final boolean deleteYn, Pageable pageable) {
        return postJpaRepository.findAllByDeleteYnOrderByCreatedDateDesc(deleteYn, pageable);
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(final Long writerId, Pageable pageable) {
        return postJpaRepository.findAllByWriter_IdOrderByCreatedDateDesc(writerId, pageable);
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
        return postJpaRepository.findAllByWriter_IdAndDeleteYnOrderByCreatedDateDesc(writerId, deleteYn, pageable);
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    public Page<Post> findAll(final SearchParam param, Pageable pageable) {
        return postJpaRepository.findAll(searchAndSortSpecification(param, pageable), pageable);
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
        return postJpaRepository.findAll(searchAndSortSpecification(param, deleteYn, pageable), pageable);
    }
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param post - 게시글 정보
     */
    public void delete(final Post post) {
        postJpaRepository.delete(post);
    }
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param id - PK
     */
    public void deleteById(final Long id) {
        postJpaRepository.deleteById(id);
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param posts - 게시글 정보 목록
     */
    public void deleteAll(final List<Post> posts) {
        postJpaRepository.deleteAll(posts);
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param posts - 게시글 정보 목록
     */
    public void deleteAllInBatch(final List<Post> posts) {
        postJpaRepository.deleteAllInBatch(posts);
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllById(final List<Long> ids) {
        postJpaRepository.deleteAllById(ids);
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllByIdInBatch(final List<Long> ids) {
        postJpaRepository.deleteAllByIdInBatch(ids);
    }
    
}
