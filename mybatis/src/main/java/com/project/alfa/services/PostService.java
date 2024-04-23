package com.project.alfa.services;

import com.project.alfa.entities.Post;
import com.project.alfa.entities.Role;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.MemberRepository;
import com.project.alfa.repositories.PostRepository;
import com.project.alfa.repositories.dto.SearchParam;
import com.project.alfa.services.dto.PostRequestDto;
import com.project.alfa.services.dto.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository   postRepository;
    private final MemberRepository memberRepository;
    private final CacheManager     cacheManager;
    
    /**
     * 게시글 작성
     *
     * @param dto - 게시글 작성 정보 DTO
     * @return PK
     */
    @Transactional
    public Long create(final PostRequestDto dto) {
        if (!validateMemberExist(dto.getWriterId()))
            throw new EntityNotFoundException("Could not found 'Member' by id: " + dto.getWriterId());
        
        Post.PostBuilder paramBuilder = Post.builder();
        paramBuilder.writerId(dto.getWriterId())
                    .title(dto.getTitle())
                    .content(dto.getContent());
        
        //공지 여부 적용 확인
        if (dto.isNoticeYn()) {
            if (memberRepository.findById(dto.getWriterId(), false).orElseThrow(
                                        () -> new EntityNotFoundException("Could not found 'Member' by id: " + dto.getWriterId()))
                                .getRole() == Role.ADMIN)
                paramBuilder.noticeYn(dto.isNoticeYn());
            else
                throw new InvalidValueException("Member do not have access.", ErrorCode.HANDLE_ACCESS_DENIED);
        }
        
        Post post = paramBuilder.build();
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
        if (!postRepository.existsById(id, false))
            throw new EntityNotFoundException("Could not found 'Post' by id: " + id);
        postRepository.addViewCount(id);
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
        if (!postRepository.existsById(id, false))
            throw new EntityNotFoundException("Could not found 'Post' by id: " + id);
        
        if (!isPostCached(id, sessionId, ipAddress))
            postRepository.addViewCount(id);
    }
    
    /**
     * 게시글 정보 수정
     *
     * @param dto - 게시글 수정 정보 DTO
     */
    @Transactional
    public void update(final PostRequestDto dto) {
        //수정 권한 검증
        validatePostExist(dto.getWriterId(), dto.getId());
        
        Post post = postRepository.findById(dto.getId(), false)
                                  .orElseThrow(() -> new EntityNotFoundException(
                                          "Could not found 'Post' by id: " + dto.getId()));
        boolean flag = false;
        
        Post.PostBuilder paramBuilder = Post.builder();
        paramBuilder.id(dto.getId()).writerId(dto.getWriterId());
        
        //제목 변경
        if (!post.getTitle().equals(dto.getTitle())) {
            flag = true;
            paramBuilder.title(dto.getTitle());
        }
        
        //내용 변경
        if (!post.getContent().equals(dto.getContent())) {
            flag = true;
            paramBuilder.content(dto.getContent());
        }
        
        //공지 여부 변경
        if (post.isNoticeYn() != dto.isNoticeYn()) {
            if (memberRepository.findById(dto.getWriterId(), false)
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "Could not found 'Member' by id: " + dto.getWriterId()))
                                .getRole() == Role.ADMIN) {
                flag = true;
                paramBuilder.noticeYn(dto.isNoticeYn());
            } else
                throw new InvalidValueException("Member do not have access.", ErrorCode.HANDLE_ACCESS_DENIED);
        }
        
        Post param = paramBuilder.build();
        
        //변경될 값이 있는 지 확인
        if (flag)
            postRepository.update(param);
    }
    
    /**
     * 게시글 삭제
     *
     * @param id       - PK
     * @param writerId - 작성자 FK
     */
    @Transactional
    public void delete(final Long id, final Long writerId) {
        //삭제 권한 검증
        validatePostExist(writerId, id);
        
        postRepository.deleteById(id, writerId);
    }
    
    /**
     * 게시글 목록 삭제
     *
     * @param ids      - PK 목록
     * @param writerId - 작성자 FK
     */
    @Transactional
    public void deleteAll(final List<Long> ids, final Long writerId) {
        //삭제 권한 검증
        validatePostsExist(writerId, ids);
        
        postRepository.deleteAllByIds(ids, writerId);
    }
    
    /**
     * 게시글 페이징 목록 조회
     *
     * @param searchParam - 검색 조건, 키워드
     * @param pageable    - 페이징 객체
     * @return 게시글 페이징 목록
     */
    public List<PostResponseDto> findAllPage(final SearchParam searchParam, Pageable pageable) {
        return postRepository.findAll(searchParam, false, pageable)
                             .stream().map(PostResponseDto::new).collect(toList());
    }
    
    /**
     * 작성자 기준 게시글 페이징 목록 조회
     *
     * @param writerId - 작성자 FK
     * @param pageable - 페이징 객체
     * @return 게시글 페이징 목록
     */
    public List<PostResponseDto> findAllPageByWriter(final Long writerId, Pageable pageable) {
        return postRepository.findAll(writerId, false, pageable)
                             .stream().map(PostResponseDto::new).collect(toList());
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
     * 작성자 FK, 게시글 PK로 게시글 엔티티 존재 검증
     * 작성자 FK -> 계정 엔티티 존재 여부 및 게시글 PK의 작성자인지 확인
     * 게시글 PK -> 게시글 엔티티 조회
     * 게시글의 수정 또는 삭제시 사용
     *
     * @param writerId - 작성자 FK
     * @param postId   - PK
     */
    private void validatePostExist(final Long writerId, final Long postId) {
        if (!validateMemberExist(writerId))
            throw new EntityNotFoundException("Could not found 'Member' by id: " + writerId);
        
        Post post = postRepository.findById(postId, false)
                                  .orElseThrow(
                                          () -> new EntityNotFoundException("Could not found 'Post' by id: " + postId));
        
        if (!post.getWriterId().equals(writerId))
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST);
    }
    
    /**
     * 작성자 FK, 게시글 PK 목록으로 게시글 엔티티 존재 검증
     * 게시글 목록 삭제시 사용
     *
     * @param writerId - 작성자 FK
     * @param postIds  - PK 목록
     */
    private void validatePostsExist(final Long writerId, final List<Long> postIds) {
        if (!validateMemberExist(writerId))
            throw new EntityNotFoundException("Could not found 'Member' by id: " + writerId);
        
        boolean isAccess = postRepository.findAll(writerId, false).stream().map(Post::getId).collect(toList())
                                         .containsAll(postIds);
        
        if (!isAccess)
            throw new InvalidValueException("Member do not have access.", ErrorCode.NOT_WRITER_OF_POST);
    }
    
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
