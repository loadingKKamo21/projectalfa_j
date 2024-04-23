package com.project.alfa.services;

import com.project.alfa.entities.Comment;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.CommentRepository;
import com.project.alfa.repositories.MemberRepository;
import com.project.alfa.repositories.PostRepository;
import com.project.alfa.services.dto.CommentRequestDto;
import com.project.alfa.services.dto.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final MemberRepository  memberRepository;
    private final PostRepository    postRepository;
    
    /**
     * 댓글 작성
     *
     * @param dto - 댓글 작성 정보 DTO
     * @return PK
     */
    @Transactional
    public Long create(final CommentRequestDto dto) {
        if (!validateMemberExist(dto.getWriterId()))
            throw new EntityNotFoundException("Could not found 'Member' by id: " + dto.getWriterId());
        
        if (!validatePostExist(dto.getPostId()))
            throw new EntityNotFoundException("Could not found 'Post' by id: " + dto.getPostId());
        
        Comment comment = Comment.builder()
                                 .writerId(dto.getWriterId())
                                 .postId(dto.getPostId())
                                 .content(dto.getContent())
                                 .build();
        
        commentRepository.save(comment);
        
        return comment.getId();
    }
    
    /**
     * 댓글 정보 조회
     *
     * @param id - PK
     * @return 댓글 정보 DTO
     */
    public CommentResponseDto read(final Long id) {
        return new CommentResponseDto(commentRepository.findById(id, false)
                                                       .orElseThrow(() -> new EntityNotFoundException(
                                                               "Could not found 'Comment' by id: " + id)));
    }
    
    /**
     * 댓글 정보 수정
     *
     * @param dto - 댓글 수정 정보 DTO
     */
    @Transactional
    public void update(final CommentRequestDto dto) {
        //수정 권한 검증
        validateCommentExist(dto.getWriterId(), dto.getId());
        
        Comment comment = commentRepository.findById(dto.getId(), false)
                                           .orElseThrow(() -> new EntityNotFoundException(
                                                   "Could not found 'Comment' by id: " + dto.getId()));
        
        Comment.CommentBuilder paramBuilder = Comment.builder();
        paramBuilder.id(dto.getId()).writerId(dto.getWriterId()).postId(dto.getPostId());
        
        //내용 변경
        if (!comment.getContent().equals(dto.getContent()))
            paramBuilder.content(dto.getContent());
        
        Comment param = paramBuilder.build();
        
        commentRepository.update(param);
    }
    
    /**
     * 댓글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @Transactional
    public void delete(final Long id, final Long writerId) {
        //삭제 권한 검증
        validateCommentExist(writerId, id);
        
        commentRepository.deleteById(id, writerId);
    }
    
    /**
     * 댓글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @Transactional
    public void deleteAll(final List<Long> ids, final Long writerId) {
        //삭제 권한 검증
        validateCommentsExist(writerId, ids);
        
        commentRepository.deleteAllByIds(ids, writerId);
    }
    
    /**
     * 게시글 기준 댓글 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 객체
     * @return 댓글 페이징 목록
     */
    public List<CommentResponseDto> findAllPageByPost(final Long postId, Pageable pageable) {
        return commentRepository.findAllByPost(postId, pageable)
                                .stream().map(CommentResponseDto::new).collect(toList());
    }
    
    /**
     * 작성자 기준 댓글 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 페이징 목록
     */
    public List<CommentResponseDto> findAllPageByWriter(final Long writerId, Pageable pageable) {
        return commentRepository.findAllByWriter(writerId, pageable)
                                .stream().map(CommentResponseDto::new).collect(toList());
    }
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 작성자 FK로 계정 엔티티 존재 검증
     *
     * @param writerId - 작성자 FK
     * @return 존재 여부
     */
    private boolean validateMemberExist(final Long writerId) {
        return memberRepository.existsById(writerId, false);
    }
    
    /**
     * 게시글 FK로 게시글 엔티티 존재 검증
     *
     * @param postId - 게시글 FK
     * @return 존재 여부
     */
    private boolean validatePostExist(final Long postId) {
        return postRepository.existsById(postId, false);
    }
    
    /**
     * 작성자 FK, 게시글 FK, 댓글 PK로 댓글 엔티티 존재 검증
     * 작성자 FK -> 계정 엔티티 존재 여부 및 댓글 PK의 작성자인지 확인
     * 댓글 PK -> 댓글 엔티티 조회
     * 댓글의 수정 또는 삭제시 사용
     *
     * @param writerId  - 작성자 FK
     * @param commentId - 댓글 PK
     */
    private void validateCommentExist(final Long writerId, final Long commentId) {
        if (!validateMemberExist(writerId))
            throw new EntityNotFoundException("Could not found 'Member' by id: " + writerId);
        
        Comment comment = commentRepository.findById(commentId, false)
                                           .orElseThrow(() -> new EntityNotFoundException(
                                                   "Could not found 'Comment' by id: " + commentId));
        
        if (!comment.getWriterId().equals(writerId))
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT);
    }
    
    /**
     * 작성자 FK, 댓글 PK 목록으로 댓글 엔티티 존재 검증
     * 댓글 목록 삭제시 사용
     *
     * @param writerId   - 작성자 FK
     * @param commentIds - PK 목록
     */
    private void validateCommentsExist(final Long writerId, final List<Long> commentIds) {
        if (!validateMemberExist(writerId))
            throw new EntityNotFoundException("Could not found 'Member' by id: " + writerId);
        
        boolean isAccess = commentRepository.findAllByWriter(writerId, false).stream().map(Comment::getId)
                                            .collect(toList()).containsAll(commentIds);
        
        if (!isAccess)
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT);
    }
    
}
