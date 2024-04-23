package com.project.alfa.services;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.config.redis.EmbeddedRedisConfig;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.Role;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.dto.SearchParam;
import com.project.alfa.repositories.v1.PostRepositoryV1;
import com.project.alfa.services.dto.PostRequestDto;
import com.project.alfa.services.dto.PostResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({TestConfig.class, EmbeddedRedisConfig.class})
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostServiceTest {
    
    @Autowired
    PostService      postService;
    @Autowired
    PostRepositoryV1 postRepository;
    //@Autowired
    //PostRepositoryV2 postRepository;
    //@Autowired
    //PostRepositoryV3 postRepository;
    @PersistenceContext
    EntityManager    em;
    @Autowired
    CacheManager     cacheManager;
    @Autowired
    DummyGenerator   dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    @Test
    @DisplayName("게시글 작성")
    void create() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        PostRequestDto dto = new PostRequestDto(null,
                                                writers.get(0).getId(),
                                                dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                                dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                                false);
        
        //When
        Long id = postService.create(dto);
        clear();
        
        //Then
        Post findPost = em.find(Post.class, id);
        
        assertThat(dto.getWriterId()).isEqualTo(findPost.getWriter().getId());
        assertThat(dto.getTitle()).isEqualTo(findPost.getTitle());
        assertThat(dto.getContent()).isEqualTo(findPost.getContent());
        assertThat(dto.isNoticeYn()).isEqualTo(findPost.isNoticeYn());
    }
    
    @Test
    @DisplayName("게시글 작성, 존재하지 않는 계정")
    void create_unknownWriter() {
        //Given
        Long writerId = new Random().nextLong();
        PostRequestDto dto = new PostRequestDto(null,
                                                writerId,
                                                dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                                dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                                false);
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> postService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by id: " + writerId);
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회")
    void read() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Long id = posts.get(0).getId();
        
        //When
        PostResponseDto dto = postService.read(id);
        clear();
        
        //Then
        Post findPost = em.find(Post.class, id);
        
        assertThat(findPost.getId()).isEqualTo(dto.getId());
        assertThat(findPost.getWriter().getNickname()).isEqualTo(dto.getWriter());
        assertThat(findPost.getTitle()).isEqualTo(dto.getTitle());
        assertThat(findPost.getContent()).isEqualTo(dto.getContent());
        assertThat(findPost.getViewCount()).isEqualTo(dto.getViewCount());
        assertThat(findPost.isNoticeYn()).isEqualTo(dto.isNoticeYn());
        assertThat(findPost.getCommentsCount()).isEqualTo(dto.getCommentsCount());
        assertThat(findPost.getAttachmentsCount()).isEqualTo(dto.getAttachmentsCount());
        assertThat(findPost.getCreatedDate()).isEqualTo(dto.getCreatedDate());
        assertThat(findPost.getLastModifiedDate()).isEqualTo(dto.getLastModifiedDate());
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회, 존재하지 않는 PK")
    void read_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> postService.read(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: " + id);
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회, 이미 삭제된 게시글")
    void read_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post post = posts.get(0);
        Long id   = post.getId();
        post.isDelete(true);
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> postService.read(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: " + id);
    }
    
    @Test
    @DisplayName("PK로 게시글 상세 조회, 캐싱")
    void read_caching() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Long id = posts.get(0).getId();
        
        //When
        PostResponseDto dto = postService.readWithCaching(id, UUID.randomUUID().toString(), "127.0.0.1");
        clear();
        
        //Then
        Post  findPost  = em.find(Post.class, id);
        Cache postCache = cacheManager.getCache("postCache");
        
        assertThat(findPost.getId()).isEqualTo(dto.getId());
        assertThat(findPost.getWriter().getNickname()).isEqualTo(dto.getWriter());
        assertThat(findPost.getTitle()).isEqualTo(dto.getTitle());
        assertThat(findPost.getContent()).isEqualTo(dto.getContent());
        assertThat(findPost.getViewCount()).isEqualTo(dto.getViewCount());
        assertThat(findPost.isNoticeYn()).isEqualTo(dto.isNoticeYn());
        assertThat(findPost.getCommentsCount()).isEqualTo(dto.getCommentsCount());
        assertThat(findPost.getAttachmentsCount()).isEqualTo(dto.getAttachmentsCount());
        assertThat(findPost.getCreatedDate()).isEqualTo(dto.getCreatedDate());
        assertThat(findPost.getLastModifiedDate()).isEqualTo(dto.getLastModifiedDate());
        assertThat(postCache).isNotNull();
    }
    
    @Test
    @DisplayName("조회수 증가")
    void addViewCount() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post post            = posts.get(0);
        Long id              = post.getId();
        int  beforeViewCount = post.getViewCount();
        
        //When
        postService.addViewCount(id);
        clear();
        
        //Then
        int afterViewCount = em.find(Post.class, id).getViewCount();
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1);
    }
    
    @Test
    @DisplayName("조회수 증가, 이미 삭제된 게시글")
    void addViewCount_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post post = posts.get(0);
        Long id   = post.getId();
        post.isDelete(true);
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> postService.addViewCount(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: " + id);
    }
    
    @Test
    @DisplayName("조회수 증가, 캐싱")
    void addViewCount_caching() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post post            = posts.get(0);
        Long id              = post.getId();
        int  beforeViewCount = post.getViewCount();
        
        //When
        postService.addViewCountWithCaching(id, UUID.randomUUID().toString(), "127.0.0.1");
        clear();
        
        //Then
        int   afterViewCount = em.find(Post.class, id).getViewCount();
        Cache postCache      = cacheManager.getCache("postCache");
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1);
        assertThat(postCache).isNotNull();
    }
    
    @Test
    @DisplayName("게시글 정보 수정")
    void update() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        writers.get(0).updateRole(Role.ADMIN);  //게시글 작성 시 공지 여부 설정을 위한 계정 '관리자' 권한 부여
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post    post           = posts.get(0);
        Long    id             = post.getId();
        String  beforeTitle    = post.getTitle();
        String  beforeContent  = post.getContent();
        boolean beforeNoticeYn = post.isNoticeYn();
        
        String afterTile;
        String afterContent;
        do {
            afterTile = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(100, 500));
        } while (beforeTitle.equals(afterTile) || beforeContent.equals(afterContent));
        
        PostRequestDto dto = new PostRequestDto(id, post.getWriter().getId(), afterTile, afterContent, true);
        
        //When
        postService.update(dto);
        clear();
        
        //Then
        Post findPost = em.find(Post.class, id);
        
        assertThat(findPost.getTitle()).isEqualTo(afterTile);
        assertThat(findPost.getTitle()).isNotEqualTo(beforeTitle);
        assertThat(findPost.getContent()).isEqualTo(afterContent);
        assertThat(findPost.getContent()).isNotEqualTo(beforeContent);
        assertThat(beforeNoticeYn).isFalse();
        assertThat(findPost.isNoticeYn()).isTrue();
    }
    
    @Test
    @DisplayName("게시글 정보 수정, 접근 권한 없는 계정")
    void update_notWriter() {
        //Given
        List<Member> writers = dummy.createMembers(2);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post post = posts.get(0);
        Long id   = post.getId();
        
        Long anotherWriterId = null;
        for (Member writer : writers)
            if (!post.getWriter().getId().equals(writer.getId())) {
                anotherWriterId = writer.getId();
                break;
            }
        
        PostRequestDto dto = new PostRequestDto(id,
                                                anotherWriterId,
                                                dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                                dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                                false);
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> postService.update(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_POST)
                .hasMessage("Member do not have access.");
    }
    
    @Test
    @DisplayName("게시글 정보 수정, 이미 삭제된 게시글")
    void update_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post post = posts.get(0);
        Long id   = post.getId();
        post.isDelete(true);
        PostRequestDto dto = new PostRequestDto(id,
                                                post.getWriter().getId(),
                                                dummy.generateRandomString(dummy.generateRandomNumber(1, 100)),
                                                dummy.generateRandomString(dummy.generateRandomNumber(100, 500)),
                                                false);
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> postService.update(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: " + id);
    }
    
    @Test
    @DisplayName("게시글 삭제")
    void delete() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post post     = posts.get(0);
        Long id       = post.getId();
        Long writerId = post.getWriter().getId();
        
        //When
        postService.delete(id, writerId);
        clear();
        
        //Then
        Post deletedPost = em.find(Post.class, id);
        
        assertThat(deletedPost.isDeleteYn()).isTrue();
    }
    
    @Test
    @DisplayName("게시글 삭제, 접근 권한 없는 계정")
    void delete_notWriter() {
        //Given
        List<Member> writers = dummy.createMembers(2);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Post post = posts.get(0);
        Long id   = post.getId();
        
        Long anotherWriterId = null;
        for (Member writer : writers)
            if (!post.getWriter().getId().equals(writer.getId())) {
                anotherWriterId = writer.getId();
                break;
            }
        Long unknownWriterId = anotherWriterId;
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> postService.delete(id, unknownWriterId))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_POST)
                .hasMessage("Member do not have access.");
    }
    
    @Test
    @DisplayName("게시글 목록 삭제")
    void deleteAll() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(10, 50);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts)
            em.persist(post);
        Long writerId = writers.get(0).getId();
        List<Long> ids = posts.stream().filter(post -> post.getWriter().getId().equals(writerId)).map(Post::getId)
                              .collect(toList());
        
        //When
        postService.deleteAll(ids, writerId);
        clear();
        
        //Then
        List<Post> deletedPosts = postRepository.findAll()
                                                .stream()
                                                .filter(post -> ids.contains(post.getId()) &&
                                                                post.getWriter().getId().equals(writerId) &&
                                                                post.isDeleteYn())
                                                .collect(toList());
        
        for (Post post : deletedPosts)
            assertThat(post.isDeleteYn()).isTrue();
    }
    
    @Test
    @DisplayName("게시글 목록 삭제, 접근 권한 없는 계정")
    void deleteAll_notWriter() {
        //Given
        List<Member> writers = dummy.createMembers(2);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(10, 50);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts)
            em.persist(post);
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        List<Long> ids = posts.stream().filter(post -> !post.getWriter().getId().equals(writerId)).map(Post::getId)
                              .collect(toList());
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> postService.deleteAll(ids, writerId))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_POST)
                .hasMessage("Member do not have access.");
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회, 검색 조건 없음")
    void findAllPage() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int total = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total).forEach(post -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            em.persist(post);
        });
        SearchParam param       = new SearchParam(null, "");
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<PostResponseDto> findPosts = postService.findAllPage(param, pageRequest);
        clear();
        
        //Then
        List<PostResponseDto> posts = postRepository.findAll().stream()
                                                    .sorted(comparing(Post::getCreatedDate).reversed())
                                                    .limit(10).map(PostResponseDto::new).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            PostResponseDto postDto     = posts.get(i);
            PostResponseDto findPostDto = findPosts.getContent().get(i);
            
            assertThat(findPostDto.getId()).isEqualTo(postDto.getId());
            assertThat(findPostDto.getWriter()).isEqualTo(postDto.getWriter());
            assertThat(findPostDto.getTitle()).isEqualTo(postDto.getTitle());
            assertThat(findPostDto.getContent()).isEqualTo(postDto.getContent());
            assertThat(findPostDto.getViewCount()).isEqualTo(postDto.getViewCount());
            assertThat(findPostDto.isNoticeYn()).isEqualTo(postDto.isNoticeYn());
            assertThat(findPostDto.getCommentsCount()).isEqualTo(postDto.getCommentsCount());
            assertThat(findPostDto.getAttachmentsCount()).isEqualTo(postDto.getAttachmentsCount());
            assertThat(findPostDto.getCreatedDate()).isEqualTo(postDto.getCreatedDate());
            assertThat(findPostDto.getLastModifiedDate()).isEqualTo(postDto.getLastModifiedDate());
        }
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회, 검색 조건 키워드('키워드')")
    void findAllPage_OneWord() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int total = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total).forEach(post -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            em.persist(post);
        });
        String      keyword     = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param       = new SearchParam("titleOrContent", keyword);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<PostResponseDto> findPosts = postService.findAllPage(param, pageRequest);
        clear();
        
        //Then
        List<PostResponseDto> posts = postRepository.findAll().stream()
                                                    .filter(post -> post.getTitle().contains(keyword) ||
                                                                    post.getContent().contains(keyword))
                                                    .sorted(comparing(Post::getCreatedDate).reversed())
                                                    .limit(10).map(PostResponseDto::new).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            PostResponseDto postDto     = posts.get(i);
            PostResponseDto findPostDto = findPosts.getContent().get(i);
            
            assertThat(findPostDto.getId()).isEqualTo(postDto.getId());
            assertThat(findPostDto.getWriter()).isEqualTo(postDto.getWriter());
            assertThat(findPostDto.getTitle()).isEqualTo(postDto.getTitle());
            assertThat(findPostDto.getContent()).isEqualTo(postDto.getContent());
            assertThat(findPostDto.getViewCount()).isEqualTo(postDto.getViewCount());
            assertThat(findPostDto.isNoticeYn()).isEqualTo(postDto.isNoticeYn());
            assertThat(findPostDto.getCommentsCount()).isEqualTo(postDto.getCommentsCount());
            assertThat(findPostDto.getAttachmentsCount()).isEqualTo(postDto.getAttachmentsCount());
            assertThat(findPostDto.getCreatedDate()).isEqualTo(postDto.getCreatedDate());
            assertThat(findPostDto.getLastModifiedDate()).isEqualTo(postDto.getLastModifiedDate());
        }
    }
    
    @Test
    @DisplayName("게시글 페이징 목록 조회, 검색 조건 키워드('키워드1 키워드2')")
    void findAllPage_MultiWords() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int total = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total).forEach(post -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            em.persist(post);
        });
        String      keyword1    = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        String      keyword2    = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param       = new SearchParam("content", keyword1 + " " + keyword2);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<PostResponseDto> findPosts = postService.findAllPage(param, pageRequest);
        clear();
        
        //Then
        List<PostResponseDto> posts = postRepository.findAll().stream()
                                                    .filter(post -> post.getContent().contains(keyword1) ||
                                                                    post.getContent().contains(keyword2))
                                                    .sorted(comparing(Post::getCreatedDate).reversed())
                                                    .limit(10).map(PostResponseDto::new).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            PostResponseDto postDto     = posts.get(i);
            PostResponseDto findPostDto = findPosts.getContent().get(i);
            
            assertThat(findPostDto.getId()).isEqualTo(postDto.getId());
            assertThat(findPostDto.getWriter()).isEqualTo(postDto.getWriter());
            assertThat(findPostDto.getTitle()).isEqualTo(postDto.getTitle());
            assertThat(findPostDto.getContent()).isEqualTo(postDto.getContent());
            assertThat(findPostDto.getViewCount()).isEqualTo(postDto.getViewCount());
            assertThat(findPostDto.isNoticeYn()).isEqualTo(postDto.isNoticeYn());
            assertThat(findPostDto.getCommentsCount()).isEqualTo(postDto.getCommentsCount());
            assertThat(findPostDto.getAttachmentsCount()).isEqualTo(postDto.getAttachmentsCount());
            assertThat(findPostDto.getCreatedDate()).isEqualTo(postDto.getCreatedDate());
            assertThat(findPostDto.getLastModifiedDate()).isEqualTo(postDto.getLastModifiedDate());
        }
    }
    
    @Test
    @DisplayName("작성자 기준 게시글 페이징 목록 조회")
    void findAllPageByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int total = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total).forEach(post -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            em.persist(post);
        });
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<PostResponseDto> findPosts = postService.findAllPageByWriter(writerId, pageRequest);
        clear();
        
        //Then
        List<PostResponseDto> posts = postRepository.findAll().stream()
                                                    .filter(post -> post.getWriter().getId().equals(writerId))
                                                    .sorted(comparing(Post::getCreatedDate).reversed()).limit(10)
                                                    .map(PostResponseDto::new).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            PostResponseDto postDto     = posts.get(i);
            PostResponseDto findPostDto = findPosts.getContent().get(i);
            
            assertThat(findPostDto.getId()).isEqualTo(postDto.getId());
            assertThat(findPostDto.getWriter()).isEqualTo(postDto.getWriter());
            assertThat(findPostDto.getTitle()).isEqualTo(postDto.getTitle());
            assertThat(findPostDto.getContent()).isEqualTo(postDto.getContent());
            assertThat(findPostDto.getViewCount()).isEqualTo(postDto.getViewCount());
            assertThat(findPostDto.isNoticeYn()).isEqualTo(postDto.isNoticeYn());
            assertThat(findPostDto.getCommentsCount()).isEqualTo(postDto.getCommentsCount());
            assertThat(findPostDto.getAttachmentsCount()).isEqualTo(postDto.getAttachmentsCount());
            assertThat(findPostDto.getCreatedDate()).isEqualTo(postDto.getCreatedDate());
            assertThat(findPostDto.getLastModifiedDate()).isEqualTo(postDto.getLastModifiedDate());
        }
    }
    
}