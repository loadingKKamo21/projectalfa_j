package com.project.alfa.services;

import com.project.alfa.aop.annotation.LockAop;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.Role;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.dto.SearchParam;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
import com.project.alfa.repositories.v1.PostRepositoryV1;
import com.project.alfa.services.dto.PostRequestDto;
import com.project.alfa.services.dto.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepositoryV1   postRepository;
    //private final PostRepositoryV2   postRepository;
    //private final PostRepositoryV3   postRepository;
    private final MemberRepositoryV1 memberRepository;
    //private final MemberRepositoryV2 memberRepository;
    //private final MemberRepositoryV3 memberRepository;
    private final CacheManager       cacheManager;
    
    /**
     * 게시글 작성
     *
     * @param dto - 게시글 작성 정보 DTO
     * @return PK
     */
    @Transactional
    public Long create(final PostRequestDto dto) {
        Member member = memberRepository.findById(dto.getWriterId(), false)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' by id: " + dto.getWriterId()));
        
        Post.PostBuilder postBuilder = Post.builder();
        postBuilder.writer(member)
                   .title(dto.getTitle())
                   .content(dto.getContent());
        
        //공지 여부 적용 확인
        if (dto.isNoticeYn()) {
            if (member.getRole() == Role.ADMIN)
                postBuilder.noticeYn(dto.isNoticeYn());
            else
                throw new InvalidValueException("Member do not have access.", ErrorCode.HANDLE_ACCESS_DENIED);
        } else
            postBuilder.noticeYn(dto.isNoticeYn());
        
        Post post = postBuilder.build();
        
        postRepository.save(post);
        
        return post.getId();
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - PK
     * @return 게시글 정보 DTO
     */
    public PostResponseDto read(final Long id) {
        return new PostResponseDto(postRepository.findById(id, false)
                                                 .orElseThrow(() -> new EntityNotFoundException(
                                                         "Could not found 'Post' by id: " + id)));
    }
    
    /**
     * 게시글 정보 조회(@CachePut)
     *
     * @param id        - PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     * @return 게시글 정보 DTO
     */
//    @CachePut(value = "postCache",
//              unless = "#id == null || #sessionId == null || #ipAddress == null",
//              key = "{#id, #sessionId, #ipAddress}")
    @CachePut(value = "postCache",
              unless = "#id == null || #sessionId == null || #ipAddress == null",
              keyGenerator = "customKeyGenerator")
    public PostResponseDto readWithCaching(final Long id, final String sessionId, final String ipAddress) {
        return new PostResponseDto(postRepository.findById(id, false)
                                                 .orElseThrow(() -> new EntityNotFoundException(
                                                         "Could not found 'Post' by id: " + id)));
    }
    
    /**
     * 조회수 증가
     *
     * @param id - PK
     */
    @Transactional
    public void addViewCount(final Long id) {
        Post post = postRepository.findById(id, false)
                                  .orElseThrow(
                                          () -> new EntityNotFoundException("Could not found 'Post' by id: " + id));
        post.addViewCount();
    }
    
    /**
     * 조회수 증가(@Cacheable)
     *
     * @param id        - PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     */
//    @Cacheable(value = "postCache",
//               unless = "#id == null || #sessionId == null || #ipAddress == null",
//               key = "{#id, #sessionId, #ipAddress}")
    @Cacheable(value = "postCache",
               unless = "#id == null || #sessionId == null || #ipAddress == null",
               keyGenerator = "customKeyGenerator")
    @Transactional
    public void addViewCountWithCaching(final Long id, final String sessionId, final String ipAddress) {
        Post post = postRepository.findById(id, false)
                                  .orElseThrow(
                                          () -> new EntityNotFoundException("Could not found 'Post' by id: " + id));
        if (!isPostCached(id, sessionId, ipAddress))
            post.addViewCount();
    }
    
    /**
     * 게시글 정보 수정
     *
     * @param dto - 게시글 수정 정보 DTO
     */
    @LockAop
    @Transactional
    public void update(final PostRequestDto dto) {
        Post post = postRepository.findById(dto.getId(), false)
                                  .orElseThrow(() -> new EntityNotFoundException(
                                          "Could not found 'Post' by id: " + dto.getId()));
        
        //수정 권한 검증
        if (!post.getWriter().getId().equals(dto.getWriterId()) || post.getWriter().isDeleteYn())
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST);
        
        //제목 변경
        if (!post.getTitle().equals(dto.getTitle()))
            post.updateTitle(dto.getTitle());
        
        //내용 변경
        if (!post.getContent().equals(dto.getContent()))
            post.updateContent(dto.getContent());
        
        //공지 여부 변경
        if (dto.isNoticeYn()) {
            if (post.getWriter().getRole() == Role.ADMIN)
                post.updateNoticeYn(dto.isNoticeYn());
            else
                throw new InvalidValueException("Member do not have access.", ErrorCode.HANDLE_ACCESS_DENIED);
        } else if (post.isNoticeYn() != dto.isNoticeYn())
            post.updateNoticeYn(dto.isNoticeYn());
    }
    
    /**
     * 게시글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @LockAop
    @Transactional
    public void delete(final Long id, final Long writerId) {
        Post post = postRepository.findById(id, false)
                                  .orElseThrow(
                                          () -> new EntityNotFoundException("Could not found 'Post' by id: " + id));
        
        //삭제 권한 검증
        if (!post.getWriter().getId().equals(writerId) || post.getWriter().isDeleteYn())
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST);
        
        post.isDelete(true);
    }
    
    /**
     * 게시글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @LockAop
    @Transactional
    public void deleteAll(final List<Long> ids, final Long writerId) {
        List<Post> posts = postRepository.findAll(ids, false);
        
        //삭제 권한 검증
        if (posts.stream()
                 .anyMatch(post -> !post.getWriter().getId().equals(writerId) || post.getWriter().isDeleteYn()))
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST);
        
        posts.forEach(post -> post.isDelete(true));
    }
    
    /**
     * 게시글 페이징 목록 조회
     *
     * @param searchParam - 검색 조건, 키워드
     * @param pageable    - 페이징 객체
     * @return 게시글 페이징 목록
     */
    public Page<PostResponseDto> findAllPage(final SearchParam searchParam, Pageable pageable) {
        return postRepository.findAll(searchParam, false, pageable).map(PostResponseDto::new);
    }
    
    /**
     * 작성자 기준 게시글 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 페이징 목록
     */
    public Page<PostResponseDto> findAllPageByWriter(final Long writerId, Pageable pageable) {
        return postRepository.findAll(writerId, false, pageable).map(PostResponseDto::new);
    }
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 게시글 정보 조회 캐싱 여부 확인
     *
     * @param id        - PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     * @return 캐싱 여부
     */
    private boolean isPostCached(final Long id, final String sessionId, final String ipAddress) {
        Cache postCache = cacheManager.getCache("postCache");
        if (postCache == null)
            return false;
        return postCache.get(id.toString() + "," + sessionId + "," + ipAddress) != null;
    }
    
}
