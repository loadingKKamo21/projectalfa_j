package com.project.alfa.services;

import com.project.alfa.services.dto.CommentRequestDto;
import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Comment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.repositories.v1.CommentRepositoryV1;
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
class CommentServiceConcurrencyTest {
    
    static final int THREAD_COUNT = 100;
    
    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    CommentService             commentService;
    @Autowired
    CommentRepositoryV1        commentRepository;
    //@Autowired
    //CommentRepositoryV2 commentRepository;
    //@Autowired
    //CommentRepositoryV3 commentRepository;
    @Autowired
    MemberRepositoryV1         memberRepository;
    //@Autowired
    //MemberRepositoryV2  memberRepository;
    //@Autowired
    //MemberRepositoryV3  memberRepository;
    @Autowired
    PostRepositoryV1           postRepository;
    //@Autowired
    //PostRepositoryV2    postRepository;
    //@Autowired
    //PostRepositoryV3    postRepository;
    @PersistenceContext
    EntityManager              em;
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
                    commentRepository.deleteAll();
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
    @DisplayName("댓글 정보 수정, 멀티 스레드 락 적용")
    void update_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicReference<Comment> commentRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                List<Member> writers = dummy.createMembers(1);
                for (Member writer : writers)
                    em.persist(writer);
                List<Post> posts = dummy.createPosts(writers, 1);
                for (Post post : posts)
                    em.persist(post);
                List<Comment> comments = dummy.createComments(writers, posts, 1);
                for (Comment comment : comments)
                    em.persist(comment);
                transactionManager.commit(transactionStatus);
                commentRef.set(comments.get(0));
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Comment comment       = commentRef.get();
        Long    id            = comment.getId();
        String  beforeContent = comment.getContent();
        
        String afterContent;
        do {
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
        } while (beforeContent.equals(afterContent));
        
        CommentRequestDto dto = new CommentRequestDto(id,
                                                      comment.getWriter().getId(),
                                                      comment.getPost().getId(),
                                                      afterContent);
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    commentService.update(dto);
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
        Comment findComment = em.find(Comment.class, id);
        
        assertThat(findComment.getContent()).isEqualTo(afterContent);
        assertThat(findComment.getContent()).isNotEqualTo(beforeContent);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 삭제, 멀티 스레드 락 적용")
    void delete_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        AtomicReference<Comment> commentRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                List<Member> writers = dummy.createMembers(1);
                for (Member writer : writers)
                    em.persist(writer);
                List<Post> posts = dummy.createPosts(writers, 1);
                for (Post post : posts)
                    em.persist(post);
                List<Comment> comments = dummy.createComments(writers, posts, 1);
                for (Comment comment : comments)
                    em.persist(comment);
                transactionManager.commit(transactionStatus);
                commentRef.set(comments.get(0));
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Comment comment  = commentRef.get();
        Long    id       = comment.getId();
        Long    writerId = comment.getWriter().getId();
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    commentService.delete(id, writerId);
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
        Comment deletedComment = em.find(Comment.class, id);
        
        assertThat(deletedComment.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 목록 삭제, 멀티 스레드 락 적용")
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
                List<Post> posts = dummy.createPosts(writers, 1);
                for (Post post : posts)
                    em.persist(post);
                int           total    = dummy.generateRandomNumber(10, 50);
                List<Comment> comments = dummy.createComments(writers, posts, total);
                for (Comment comment : comments)
                    em.persist(comment);
                transactionManager.commit(transactionStatus);
                writerIdRef.set(writers.get(0).getId());
                idsRef.set(comments.stream()
                                   .filter(comment -> comment.getWriter().getId()
                                                             .equals(writers.get(0).getId()))
                                   .map(Comment::getId).collect(toList()));
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
                    commentService.deleteAll(ids, writerId);
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
        List<Comment> deletedComments = commentRepository.findAll().stream()
                                                         .filter(comment -> ids.contains(comment.getId()) &&
                                                                            comment.getWriter().getId()
                                                                                   .equals(writerId) &&
                                                                            comment.isDeleteYn())
                                                         .collect(toList());
        
        for (Comment comment : deletedComments)
            assertThat(comment.isDeleteYn()).isTrue();
    }
    
}