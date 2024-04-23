package com.project.alfa.controllers.api;

import com.google.gson.Gson;
import com.project.alfa.repositories.dto.SearchParam;
import com.project.alfa.security.CustomUserDetails;
import com.project.alfa.services.AttachmentService;
import com.project.alfa.services.PostService;
import com.project.alfa.services.dto.PostRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/posts",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PostApiController {
    
    private final PostService       postService;
    private final AttachmentService attachmentService;
    
    /**
     * GET: 게시글 목록 페이지
     *
     * @param searchCondition - 검색 조건
     * @param searchKeyword   - 검색 키워드
     * @param pageable        - 페이징 객체
     * @return
     */
    @GetMapping
    public ResponseEntity<String> postsList(
            @RequestParam(required = false, value = "condition") final String searchCondition,
            @RequestParam(required = false, value = "keyword") final String searchKeyword,
            Pageable pageable) {
        return ResponseEntity.ok(
                new Gson().toJson(postService.findAllPage(new SearchParam(searchCondition, searchKeyword), pageable)));
    }
    
    /**
     * GET: 작성자 기준 게시글 목록 페이지
     *
     * @param userDetails
     * @param pageable    - 페이징 객체
     * @return
     */
    @GetMapping("/writer")
    public ResponseEntity<String> postsListByWriter(
            @AuthenticationPrincipal final UserDetails userDetails, Pageable pageable) {
        return ResponseEntity.ok(new Gson().toJson(
                postService.findAllPageByWriter(((CustomUserDetails) userDetails).getId(), pageable)));
    }
    
    /**
     * GET: 게시글 상세 조회 페이지
     *
     * @param postId  - 게시글 PK
     * @param request
     * @return
     */
    @GetMapping("/{postId}")
    public ResponseEntity<String> readPostPage(@PathVariable final Long postId, HttpServletRequest request) {
        postService.addViewCountWithCaching(postId, request.getSession().getId(), request.getRemoteAddr());
        Map<String, Object> map = new HashMap<>();
        map.put("post", postService.readWithCaching(postId, request.getSession().getId(), request.getRemoteAddr()));
        map.put("files", attachmentService.findAllFilesByPost(postId));
        return ResponseEntity.ok(new Gson().toJson(map));
    }
    
    /**
     * GET: 게시글 작성 페이지
     *
     * @return
     */
    @GetMapping("/write")
    public ResponseEntity<String> writePostPage() {
        return ResponseEntity.ok(new Gson().toJson(new PostRequestDto()));
    }
    
    /**
     * POST: 게시글 작성
     *
     * @param userDetails
     * @param params      - 게시글 작성 정보 DTO
     * @return
     */
    @PostMapping("/write")
    public ResponseEntity<String> writePost(@AuthenticationPrincipal UserDetails userDetails,
                                            @Valid @RequestBody final PostRequestDto params) {
        params.setWriterId(((CustomUserDetails) userDetails).getId());
        Long id = postService.create(params);
        attachmentService.saveAllFiles(id, params.getFiles());
        return ResponseEntity.ok("Post created successfully.");
    }
    
    /**
     * GET: 게시글 수정 페이지
     *
     * @param postId - 게시글 PK
     * @return
     */
    @GetMapping("/write/{postId}")
    public ResponseEntity<String> updatePostPage(@PathVariable final Long postId) {
        Map<String, Object> map = new HashMap<>();
        map.put("post", postService.read(postId));
        map.put("form", new PostRequestDto());
        map.put("files", attachmentService.findAllFilesByPost(postId));
        return new ResponseEntity<>(new Gson().toJson(map), HttpStatus.OK);
    }
    
    /**
     * POST: 게시글 수정
     *
     * @param postId      - 게시글 PK
     * @param userDetails
     * @param params      - 게시글 수정 정보 DTO
     * @return
     */
    @PostMapping("/write/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable final Long postId,
                                             @AuthenticationPrincipal UserDetails userDetails,
                                             @Valid @RequestBody final PostRequestDto params) {
        params.setWriterId(((CustomUserDetails) userDetails).getId());
        postService.update(params);
        attachmentService.deleteAllFilesByIds(params.getRemoveFileIds(), postId);
        attachmentService.saveAllFiles(postId, params.getFiles());
        return ResponseEntity.ok("Post updated successfully.");
    }
    
    /**
     * 게시글 삭제
     *
     * @param userDetails
     * @param params      - 게시글 삭제 정보 DTO
     * @return
     */
    @PostMapping("/delete")
    public ResponseEntity<String> deletePost(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestBody final PostRequestDto params) {
        postService.delete(params.getId(), ((CustomUserDetails) userDetails).getId());
        attachmentService.deleteAllFilesByIds(params.getRemoveFileIds(), params.getId());
        return ResponseEntity.ok("Post deleted successfully.");
    }
    
}
