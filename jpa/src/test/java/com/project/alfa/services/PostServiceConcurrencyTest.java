package com.project.alfa.services;

import com.project.alfa.services.dto.PostRequestDto;
import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.Role;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
import com.project.alfa.repositories.v1.PostRepositoryV1;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostServiceConcurrencyTest {
    
    static final int THREAD_COUNT = 100;
    
    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    PostService                postService;
    @Autowired
    PostRepositoryV1           postRepository;
    //@Autowired
    //PostRepositoryV2   postRepository;
    //@Autowired
    //PostRepositoryV3   postRepository;
    @Autowired
    MemberRepositoryV1         memberRepository;
    //@Autowired
    //MemberRepositoryV2 memberRepository;
    //@Autowired
    //MemberRepositoryV3 memberRepository;
    @PersistenceContext
    EntityManager              em;
    @Autowired
    CacheManager               cacheManager;
    @Autowired
    DummyGenerator             dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    @BeforeEach
    void setup() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {
                try {
                    postRepository.deleteAll();
                    memberRepository.deleteAll();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
                return null;
            });
        });
        executorService.shutdown();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 정보 수정, 멀티 스레드 락 수정")
    void update_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicReference<Post> postRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                List<Member> writers = dummy.createMembers(1);
                for (Member writer : writers)
                    em.persist(writer);
                writers.get(0).updateRole(Role.ADMIN);  //게시글 작성 시 공지 여부 설정을 위한 계정 '관리자' 권한 부여
                List<Post> posts = dummy.createPosts(writers, 1);
                for (Post post : posts)
                    em.persist(post);
                transactionManager.commit(transactionStatus);
                postRef.set(posts.get(0));
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
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
        
        PostRequestDto dto = new PostRequestDto(id, postRef.get().getWriter().getId(), afterTile, afterContent, true);
        
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
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                List<Member> writers = dummy.createMembers(1);
                for (Member writer : writers)
                    em.persist(writer);
                List<Post> posts = dummy.createPosts(writers, 1);
                for (Post post : posts)
                    em.persist(post);
                transactionManager.commit(transactionStatus);
                idRef.set(posts.get(0).getId());
                writerIdRef.set(posts.get(0).getWriter().getId());
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
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
                    postService.delete(id, writerId);
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        clear();
        
        //Then
        Post deletedPost = em.find(Post.class, id);
        
        assertThat(deletedPost.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 목록 삭제, 멀티 스레드 락 적용")
    void deleteAll_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicLong                  writerIdRef = new AtomicLong();
        AtomicReference<List<Long>> idsRef      = new AtomicReference<>();
        
        executorService.execute(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                List<Member> writers = dummy.createMembers(1);
                for (Member writer : writers)
                    em.persist(writer);
                int        total = dummy.generateRandomNumber(10, 50);
                List<Post> posts = dummy.createPosts(writers, total);
                for (Post post : posts)
                    em.persist(post);
                transactionManager.commit(transactionStatus);
                writerIdRef.set(writers.get(0).getId());
                idsRef.set(posts.stream().filter(post -> post.getWriter().getId().equals(writers.get(0).getId()))
                                .map(Post::getId).collect(toList()));
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long       writerId = writerIdRef.get();
        List<Long> ids      = idsRef.get();
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    postService.deleteAll(ids, writerId);
                } catch (RuntimeException e) {
                    log.info("[{}-thread] 락 획득 실패: {}", threadIndex, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
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
    
}
