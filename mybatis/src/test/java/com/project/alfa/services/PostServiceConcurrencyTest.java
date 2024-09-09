package com.project.alfa.services;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.config.redis.EmbeddedRedisConfig;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.Role;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.MemberRepository;
import com.project.alfa.repositories.PostRepository;
import com.project.alfa.repositories.mybatis.MemberMapper;
import com.project.alfa.repositories.mybatis.PostMapper;
import com.project.alfa.services.dto.PostRequestDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@Import({TestConfig.class, EmbeddedRedisConfig.class})
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostServiceConcurrencyTest {
    
    static final int THREAD_COUNT = 100;
    
    @Autowired
    PostService      postService;
    @Autowired
    PostRepository   postRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PostMapper       postMapper;
    @Autowired
    MemberMapper     memberMapper;
    @Autowired
    DummyGenerator   dummy;
    @Autowired
    CacheManager     cacheManager;
    
    @BeforeEach
    void setup() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            postRepository.deleteAll();
            memberRepository.deleteAll();
        });
        executorService.shutdown();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 정보 수정, 멀티 스레드 락 적용")
    void update_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicReference<Post> postRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            try {
                List<Member> writers = dummy.createMembers(1, true);
                memberMapper.update(Member.builder()
                                          .id(writers.get(0).getId())
                                          .role(Role.ADMIN) //게시글 작성 시 공지 여부 설정을 위한 계정 '관리자' 권한 부여
                                          .build());
                Post post = dummy.createPosts(writers, 1, true).get(0);
                postRef.set(post);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long    id             = postRef.get().getId();
        String  beforeTitle    = postRef.get().getTitle();
        String  beforeContent  = postRef.get().getContent();
        boolean beforeNoticeYn = postRef.get().isNoticeYn();
        
        String afterTile;
        String afterContent;
        do {
            afterTile = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(100, 500));
        } while (beforeTitle.equals(afterTile) || beforeContent.equals(afterContent));
        
        PostRequestDto dto = new PostRequestDto(id, postRef.get().getWriterId(), afterTile, afterContent, true);
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    postService.update(dto);
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        //Then
        Post findPost = postMapper.findById(id);
        
        assertThat(findPost.getTitle()).isEqualTo(afterTile);
        assertThat(findPost.getTitle()).isNotEqualTo(beforeTitle);
        assertThat(findPost.getContent()).isEqualTo(afterContent);
        assertThat(findPost.getContent()).isNotEqualTo(beforeContent);
        assertThat(beforeNoticeYn).isFalse();
        assertThat(findPost.isNoticeYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 삭제, 멀티 스레드 락 적용")
    void delete_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicLong idRef       = new AtomicLong();
        AtomicLong writerIdRef = new AtomicLong();
        
        executorService.execute(() -> {
            try {
                List<Member> writers = dummy.createMembers(1, true);
                Post         post    = dummy.createPosts(writers, 1, true).get(0);
                idRef.set(post.getId());
                writerIdRef.set(post.getWriterId());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long id       = idRef.get();
        Long writerId = writerIdRef.get();
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    if (threadIndex == 0)
                        postService.delete(id, writerId);
                    else
                        assertThatThrownBy(() -> postService.delete(id, writerId))
                                .isInstanceOf(EntityNotFoundException.class)
                                .hasMessage("Could not found 'Post' by id: " + id);
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        //Then
        Post deletedPost = postMapper.findById(id);
        
        assertThat(deletedPost.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 목록 삭제, 멀티 스레드 락 적용")
    void deleteAll_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicLong                  writeridRef = new AtomicLong();
        AtomicReference<List<Long>> idsRef      = new AtomicReference<>();
        
        executorService.execute(() -> {
            try {
                List<Member> writers = dummy.createMembers(1, true);
                int          total   = dummy.generateRandomNumber(10, 50);
                List<Post>   posts   = dummy.createPosts(writers, total, true);
                writeridRef.set(writers.get(0).getId());
                idsRef.set(posts.stream().filter(post -> post.getWriterId().equals(writers.get(0).getId()))
                                .map(Post::getId).collect(toList()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long       writerId = writeridRef.get();
        List<Long> ids      = idsRef.get();
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    if (threadIndex == 0)
                        postService.deleteAll(ids, writerId);
                    else
                        assertThatThrownBy(() -> postService.deleteAll(ids, writerId))
                                .isInstanceOf(InvalidValueException.class)
                                .hasMessage("Member do not have access.");
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        //Then
        List<Post> deletedPosts = postMapper.findAll().stream().filter(post -> ids.contains(post.getId()) &&
                                                                               post.getWriterId().equals(writerId) &&
                                                                               post.isDeleteYn())
                                            .collect(toList());
        
        for (Post post : deletedPosts)
            assertThat(post.isDeleteYn()).isTrue();
    }
    
}
