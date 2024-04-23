package com.project.alfa.repositories.mybatis;

import com.github.pagehelper.PageHelper;
import com.project.alfa.entities.Comment;
import com.project.alfa.repositories.CommentRepository;
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
public class CommentRepositoryImpl implements CommentRepository {
    
    private final CommentMapper commentMapper;
    
    /**
     * 댓글 저장
     *
     * @param comment - 댓글 정보
     * @return 댓글 정보
     */
    @Override
    public Comment save(Comment comment) {
        commentMapper.save(comment);
        return comment;
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id - PK
     * @return 댓글 정보
     */
    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(commentMapper.findById(id));
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보
     */
    @Override
    public Optional<Comment> findById(Long id, boolean deleteYn) {
        return Optional.ofNullable(commentMapper.findByIdAndDeleteYn(id, deleteYn));
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @return 댓글 정보 목록
     */
    @Override
    public List<Comment> findAll() {
        return commentMapper.findAll();
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    @Override
    public List<Comment> findAll(boolean deleteYn) {
        return commentMapper.findAllByDeleteYn(deleteYn);
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 댓글 정보 목록
     */
    @Override
    public List<Comment> findAll(List<Long> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return commentMapper.findAllByIds(ids);
    }
    
    /**
     * 댓글 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    @Override
    public List<Comment> findAll(List<Long> ids, boolean deleteYn) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return commentMapper.findAllByIdsAndDeleteYn(ids, deleteYn);
    }
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @return 댓글 정보 목록
     */
    @Override
    public List<Comment> findAllByWriter(Long writerId) {
        return commentMapper.findAllByWriter(writerId);
    }
    
    /**
     * 작성자 기준 댓글 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    @Override
    public List<Comment> findAllByWriter(Long writerId, boolean deleteYn) {
        return commentMapper.findAllByWriterAndDeleteYn(writerId, deleteYn);
    }
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 댓글 정보 목록
     */
    @Override
    public List<Comment> findAllByPost(Long postId) {
        return commentMapper.findAllByPost(postId);
    }
    
    /**
     * 게시글 기준 댓글 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 댓글 정보 목록
     */
    @Override
    public List<Comment> findAllByPost(Long postId, boolean deleteYn) {
        return commentMapper.findAllByPostAndDeleteYn(postId, deleteYn);
    }
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    @Override
    public List<Comment> findAllByWriter(Long writerId, Pageable pageable) {
        pagingAndSorting(pageable);
        return commentMapper.findAllByWriter(writerId);
    }
    
    /**
     * 작성자 기준 댓글 정보 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 객체
     * @return 댓글 정보 페이징 목록
     */
    @Override
    public List<Comment> findAllByWriter(Long writerId, boolean deleteYn, Pageable pageable) {
        pagingAndSorting(pageable);
        return commentMapper.findAllByWriterAndDeleteYn(writerId, deleteYn);
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    @Override
    public List<Comment> findAllByPost(Long postId, Pageable pageable) {
        pagingAndSorting(pageable);
        return commentMapper.findAllByPost(postId);
    }
    
    /**
     * 게시글 기준 댓글 정보 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @param pageable - 페이징 목록
     * @return 댓글 정보 페이징 목록
     */
    @Override
    public List<Comment> findAllByPost(Long postId, boolean deleteYn, Pageable pageable) {
        pagingAndSorting(pageable);
        return commentMapper.findAllByPostAndDeleteYn(postId, deleteYn);
    }
    
    /**
     * 댓글 정보 수정
     *
     * @param param - 댓글 수정 정보
     */
    @Override
    public void update(Comment param) {
        commentMapper.update(param);
    }
    
    /**
     * 댓글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @Override
    public void deleteById(Long id, Long writerId) {
        commentMapper.deleteById(id, writerId);
    }
    
    /**
     * 댓글 정보 영구 삭제
     *
     * @param id - PK
     */
    @Override
    public void permanentlyDeleteById(Long id) {
        commentMapper.permanentlyDeleteById(id);
    }
    
    /**
     * 댓글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @Override
    public void deleteAllByIds(List<Long> ids, Long writerId) {
        if (ids.isEmpty())
            return;
        commentMapper.deleteAllByIds(ids, writerId);
    }
    
    /**
     * 댓글 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    @Override
    public void permanentlyDeleteAllByIds(List<Long> ids) {
        if (ids.isEmpty())
            return;
        commentMapper.permanentlyDeleteAllByIds(ids);
    }
    
    /**
     * 페이징 및 정렬 적용
     *
     * @param pageable - 페이징 객체
     */
    private void pagingAndSorting(Pageable pageable) {
        StringBuilder sb     = new StringBuilder();
        String        prefix = "comment.";
        String        regex  = "^(?!\\s*$)(?!.* (asc|desc)$).+$";
        
        Sort sort = pageable.getSort();
        if (!sort.isEmpty()) {
            for (Sort.Order order : sort) {
                if (sb.length() > 0)
                    sb.append(", ");
                String         property  = order.getProperty();
                Sort.Direction direction = order.getDirection();
                
                switch (property) {
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
