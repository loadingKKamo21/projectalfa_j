package com.project.alfa.repositories.mybatis;

import com.github.pagehelper.PageHelper;
import com.project.alfa.entities.Post;
import com.project.alfa.repositories.PostRepository;
import com.project.alfa.repositories.dto.SearchParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    
    private final PostMapper postMapper;
    
    /**
     * 게시글 저장
     *
     * @param post - 게시글 정보
     * @return 게시글 정보
     */
    @Override
    public Post save(Post post) {
        postMapper.save(post);
        return post;
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보
     */
    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(postMapper.findById(id));
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보
     */
    @Override
    public Optional<Post> findById(Long id, boolean deleteYn) {
        return Optional.ofNullable(postMapper.findByIdAndDeleteYn(id, deleteYn));
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @return 게시글 정보 목록
     */
    @Override
    public List<Post> findAll() {
        return postMapper.findAll();
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    @Override
    public List<Post> findAll(boolean deleteYn) {
        return postMapper.findAllByDeleteYn(deleteYn);
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 게시글 정보 목록
     */
    @Override
    public List<Post> findAll(List<Long> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return postMapper.findAllByIds(ids);
    }
    
    /**
     * 게시글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    @Override
    public List<Post> findAll(List<Long> ids, boolean deleteYn) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return postMapper.findAllByIdsAndDeleteYn(ids, deleteYn);
    }
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 게시글 정보 목록
     */
    @Override
    public List<Post> findAll(Long writerId) {
        return postMapper.findAllByWriter(writerId);
    }
    
    /**
     * 작성자 기준 게시글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 게시글 정보 목록
     */
    @Override
    public List<Post> findAll(Long writerId, boolean deleteYn) {
        return postMapper.findAllByWriterAndDeleteYn(writerId, deleteYn);
    }
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    @Override
    public List<Post> findAll(Pageable pageable) {
        pagingAndSorting(pageable);
        return postMapper.findAll();
    }
    
    /**
     * 게시글 정보 페이징 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    @Override
    public List<Post> findAll(boolean deleteYn, Pageable pageable) {
        pagingAndSorting(pageable);
        return postMapper.findAllByDeleteYn(deleteYn);
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    @Override
    public List<Post> findAll(Long writerId, Pageable pageable) {
        pagingAndSorting(pageable);
        return postMapper.findAllByWriter(writerId);
    }
    
    /**
     * 작성자 기준 게시글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    @Override
    public List<Post> findAll(Long writerId, boolean deleteYn, Pageable pageable) {
        pagingAndSorting(pageable);
        return postMapper.findAllByWriterAndDeleteYn(writerId, deleteYn);
    }
    
    /**
     * 검색 조건, 키워드로 게시글 정보 페이징 목록 조회
     *
     * @param param    - 검색 조건, 키워드
     * @param pageable - 페이징 객체
     * @return 게시글 정보 페이징 목록
     */
    @Override
    public List<Post> findAll(SearchParam param, Pageable pageable) {
        pagingAndSorting(pageable);
        return postMapper.findAllBySearchParam(param);
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
    public List<Post> findAll(SearchParam param, boolean deleteYn, Pageable pageable) {
        pagingAndSorting(pageable);
        return postMapper.findAllBySearchParamAndDeleteYn(param, deleteYn);
    }
    
    /**
     * 조회수 증가
     *
     * @param id - PK
     */
    @Override
    public void addViewCount(Long id) {
        postMapper.addViewCount(id);
    }
    
    /**
     * 게시글 정보 수정
     *
     * @param param - 게시글 수정 정보
     */
    @Override
    public void update(Post param) {
        postMapper.update(param);
    }
    
    /**
     * 게시글 엔티티 존재 확인
     *
     * @param id - PK
     * @return 존재 여부
     */
    @Override
    public boolean existsById(Long id) {
        return postMapper.existsById(id);
    }
    
    /**
     * 게시글 엔티티 존재 확인
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 존재 여부
     */
    @Override
    public boolean existsById(Long id, boolean deleteYn) {
        return postMapper.existsByIdAndDeleteYn(id, deleteYn);
    }
    
    /**
     * 게시글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @Override
    public void deleteById(Long id, Long writerId) {
        postMapper.deleteById(id, writerId);
    }
    
    /**
     * 게시글 정보 영구 삭제
     *
     * @param id - PK
     */
    @Override
    public void permanentlyDeleteById(Long id) {
        postMapper.permanentlyDeleteById(id);
    }
    
    /**
     * 게시글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @Override
    public void deleteAllByIds(List<Long> ids, Long writerId) {
        if (ids.isEmpty())
            return;
        postMapper.deleteAllByIds(ids, writerId);
    }
    
    /**
     * 게시글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    @Override
    public void permanentlyDeleteAllByIds(List<Long> ids) {
        if (ids.isEmpty())
            return;
        postMapper.permanentlyDeleteAllByIds(ids);
    }
    
    /**
     * 페이징 및 정렬 적용
     *
     * @param pageable - 페이징 객체
     */
    private void pagingAndSorting(Pageable pageable) {
        StringBuilder sb     = new StringBuilder();
        String        prefix = "post.";
        String        regex  = "^(?!\\s*$)(?!.* (asc|desc)$).+$";
        
        Sort sort = pageable.getSort();
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                if (sb.length() > 0)
                    sb.append(", ");
                String         property  = order.getProperty();
                Sort.Direction direction = order.getDirection();
                
                switch (property) {
                    case "viewCount":
                        sb.append(prefix + "view_count");
                        break;
                    case "createdDate":
                        sb.append(prefix + "created_date");
                        break;
                    case "lastModifiedDate":
                        sb.append(prefix + "last_modified_date");
                        break;
                    default:
                        break;
                }
                
                if (Pattern.compile(regex).matcher(sb.toString()).matches()) {
                    if (direction.isAscending())
                        sb.append(" asc");
                    else
                        sb.append(" desc");
                }
            }
        } else
            sb.append(prefix + "created_date desc");
        
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        PageHelper.orderBy(sb.toString());
    }
    
}
