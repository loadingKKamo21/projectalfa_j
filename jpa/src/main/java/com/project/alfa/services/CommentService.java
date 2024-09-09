package com.project.alfa.services;

import com.project.alfa.aop.annotation.LockAop;
import com.project.alfa.entities.Comment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.v1.CommentRepositoryV1;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
import com.project.alfa.repositories.v1.PostRepositoryV1;
import com.project.alfa.services.dto.CommentRequestDto;
import com.project.alfa.services.dto.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepositoryV1 commentRepository;
    //private final CommentRepositoryV2 commentRepository;
    //private final CommentRepositoryV3 commentRepository;
    private final MemberRepositoryV1  memberRepository;
    //private final MemberRepositoryV2  memberRepository;
    //private final MemberRepositoryV3  memberRepository;
    private final PostRepositoryV1    postRepository;
    //private final PostRepositoryV2    postRepository;
    //private final PostRepositoryV3    postRepository;
    
    /**
     * 댓글 작성
     *
     * @param dto - 댓글 작성 정보 DTO
     * @return PK
     */
    @Transactional
    public Long create(final CommentRequestDto dto) {
        Member member = memberRepository.findById(dto.getWriterId(), false)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' by id: " + dto.getWriterId()));
        Post post = postRepository.findById(dto.getPostId(), false)
                                  .orElseThrow(() -> new EntityNotFoundException(
                                          "Could not found 'Post' by id: " + dto.getPostId()));
        
        Comment comment = Comment.builder()
                                 .writer(member)
                                 .post(post)
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
    @LockAop
    @Transactional
    public void update(final CommentRequestDto dto) {
        Comment comment = commentRepository.findById(dto.getId(), false)
                                           .orElseThrow(() -> new EntityNotFoundException(
                                                   "Could not found 'Comment' by id: " + dto.getId()));
        
        //수정 권한 검증
        if (!comment.getWriter().getId().equals(dto.getWriterId()) || comment.getWriter().isDeleteYn())
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT);
        
        //내용 변경
        if (!comment.getContent().equals(dto.getContent()))
            comment.updateContent(dto.getContent());
    }
    
    /**
     * 댓글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @LockAop
    @Transactional
    public void delete(final Long id, final Long writerId) {
        Comment comment = commentRepository.findById(id, false)
                                           .orElseThrow(() -> new EntityNotFoundException(
                                                   "Could not found 'Comment' by id: " + id));
        
        //삭제 권한 검증
        if (!comment.getWriter().getId().equals(writerId) || comment.getWriter().isDeleteYn())
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT);
        
        comment.isDelete(true);
    }
    
    /**
     * 댓글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @LockAop
    @Transactional
    public void deleteAll(final List<Long> ids, final Long writerId) {
        List<Comment> comments = commentRepository.findAll(ids, false);
        
        //삭제 권한 검증
        if (comments.stream().anyMatch(
                comment -> !comment.getWriter().getId().equals(writerId) || comment.getWriter().isDeleteYn()))
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_COMMENT);
        
        comments.forEach(comment -> comment.isDelete(true));
    }
    
    /**
     * 게시글 기준 댓글 페이징 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 객체
     * @return 댓글 페이징 목록
     */
    public Page<CommentResponseDto> findAllPageByPost(final Long postId, Pageable pageable) {
        return commentRepository.findAllByPost(postId, pageable).map(CommentResponseDto::new);
    }
    
    /**
     * 작성자 기준 댓글 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 댓글 페이징 목록
     */
    public Page<CommentResponseDto> findAllPageByWriter(final Long writerId, Pageable pageable) {
        return commentRepository.findAllByWriter(writerId, pageable).map(CommentResponseDto::new);
    }
    
}
