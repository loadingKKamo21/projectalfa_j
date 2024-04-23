package com.project.alfa.controllers.api;

import com.google.gson.Gson;
import com.project.alfa.security.CustomUserDetails;
import com.project.alfa.services.CommentService;
import com.project.alfa.services.dto.CommentRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CommentApiController {
    
    private final CommentService commentService;
    
    /**
     * GET: 댓글 목록 페이지
     *
     * @param postId   - 게시글 FK
     * @param pageable - 페이징 객체
     * @return
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<String> commentsList(@PathVariable final Long postId, Pageable pageable) {
        return ResponseEntity.ok(new Gson().toJson(commentService.findAllPageByPost(postId, pageable)));
    }
    
    /**
     * GET: 작성자 기준 댓글 목록 페이지
     *
     * @param userDetails
     * @param pageable    - 페이징 객체
     * @return
     */
    @GetMapping("/comments/writer")
    public ResponseEntity<String> commentsListByWriter(
            @AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        return ResponseEntity.ok(new Gson().toJson(
                commentService.findAllPageByWriter(((CustomUserDetails) userDetails).getId(), pageable)));
    }
    
    /**
     * GET: 댓글 작성 페이지
     *
     * @param postId - 게시글 FK
     * @return
     */
    @GetMapping("/posts/{postId}/comments/write")
    public ResponseEntity<String> writeCommentPage(@PathVariable final Long postId) {
        return ResponseEntity.ok(new Gson().toJson(new CommentRequestDto()));
    }
    
    /**
     * POST: 댓글 작성
     *
     * @param userDetails
     * @param postId      - 게시글 FK
     * @param params      - 댓글 작성 정보 DTO
     * @return
     */
    @PostMapping("/posts/{postId}/comments/write")
    public ResponseEntity<String> writeComment(@PathVariable final Long postId,
                                               @AuthenticationPrincipal UserDetails userDetails,
                                               @Valid @RequestBody final CommentRequestDto params) {
        params.setWriterId(((CustomUserDetails) userDetails).getId());
        params.setPostId(postId);
        commentService.create(params);
        return ResponseEntity.ok("Comment created successfully.");
    }
    
    /**
     * GET: 댓글 수정 페이지
     *
     * @param postId    - 게사글 FK
     * @param commentId - 댓글 PK
     * @return
     */
    @GetMapping("/posts/{postId}/comments/write/{commentId}")
    public ResponseEntity<String> updateCommentPage(@PathVariable final Long postId,
                                                    @PathVariable final Long commentId) {
        Map<String, Object> map = new HashMap<>();
        map.put("comment", commentService.read(commentId));
        map.put("form", new CommentRequestDto());
        return new ResponseEntity<>(new Gson().toJson(map), HttpStatus.OK);
    }
    
    /**
     * POST: 댓글 수정
     *
     * @param postId      - 게시글 FK
     * @param commentId   - 댓글 PK
     * @param userDetails
     * @param params      - 댓글 수정 정보 DTO
     * @return
     */
    @PostMapping("/posts/{postId}/comments/write/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable final Long postId, @PathVariable final Long commentId,
                                                @AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody final CommentRequestDto params) {
        params.setWriterId(((CustomUserDetails) userDetails).getId());
        params.setPostId(postId);
        commentService.update(params);
        return ResponseEntity.ok("Comment updated successfully.");
    }
    
    /**
     * POST: 댓글 삭제
     *
     * @param postId      - 게시글 FK
     * @param userDetails
     * @param params      - 댓글 삭제 정보 DTO
     * @return
     */
    @PostMapping("/posts/{postId}/comments/delete")
    public ResponseEntity<String> deleteComment(@PathVariable final Long postId,
                                                @AuthenticationPrincipal UserDetails userDetails,
                                                @RequestBody final CommentRequestDto params) {
        commentService.delete(params.getId(), ((CustomUserDetails) userDetails).getId());
        return ResponseEntity.ok("Comment deleted successfully.");
    }
    
}
