package com.project.alfa.services;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Comment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.CommentRepository;
import com.project.alfa.repositories.MemberRepository;
import com.project.alfa.repositories.PostRepository;
import com.project.alfa.repositories.mybatis.CommentMapper;
import com.project.alfa.services.dto.CommentRequestDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
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
@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentServiceConcurrencyTest {
    
    static final int THREAD_COUNT = 100;
    
    @Autowired
    CommentService    commentService;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    MemberRepository  memberRepository;
    @Autowired
    PostRepository    postRepository;
    @Autowired
    CommentMapper     commentMapper;
    @Autowired
    DummyGenerator    dummy;
    
    @BeforeEach
    void setup() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            commentRepository.deleteAll();
            postRepository.deleteAll();
            memberRepository.deleteAll();
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
        
        AtomicLong              idRef            = new AtomicLong();
        AtomicLong              writerIdRef      = new AtomicLong();
        AtomicLong              postIdRef        = new AtomicLong();
        AtomicReference<String> beforeContentRef = new AtomicReference<>();
        
        executorService.execute(() -> {
            try {
                List<Member> writers = dummy.createMembers(1, true);
                List<Post>   posts   = dummy.createPosts(writers, 1, true);
                Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
                idRef.set(comment.getId());
                writerIdRef.set(comment.getWriterId());
                postIdRef.set(comment.getPostId());
                beforeContentRef.set(comment.getContent());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long   id            = idRef.get();
        Long   writerId      = writerIdRef.get();
        Long   postId        = postIdRef.get();
        String beforeContent = beforeContentRef.get();
        
        String afterContent;
        do {
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
        } while (beforeContent.equals(afterContent));
        
        CommentRequestDto dto = new CommentRequestDto(id, writerId, postId, afterContent);
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    commentService.update(dto);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        
        //Then
        Comment findComment = commentMapper.findById(id);
        
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
        
        AtomicLong idRef       = new AtomicLong();
        AtomicLong writerIdRef = new AtomicLong();
        
        executorService.execute(() -> {
            try {
                List<Member> writers = dummy.createMembers(1, true);
                List<Post>   posts   = dummy.createPosts(writers, 1, true);
                Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
                idRef.set(comment.getId());
                writerIdRef.set(comment.getWriterId());
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
                        commentService.delete(id, writerId);
                    else
                        assertThatThrownBy(() -> commentService.delete(id, writerId))
                                .isInstanceOf(EntityNotFoundException.class)
                                .hasMessage("Could not found 'Comment' by id: " + id);
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
        Comment deletedComment = commentMapper.findById(id);
        
        assertThat(deletedComment.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 목록 삭제, 멀티 스레드 락 적용")
    void deleteAll_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        int total = dummy.generateRandomNumber(10, 50);
        
        AtomicLong                  writerIdRef = new AtomicLong();
        AtomicReference<List<Long>> idsRef      = new AtomicReference<>();
        
        executorService.execute(() -> {
            try {
                List<Member>  writers  = dummy.createMembers(1, true);
                List<Post>    posts    = dummy.createPosts(writers, 1, true);
                List<Comment> comments = dummy.createComments(writers, posts, total, true);
                writerIdRef.set(writers.get(0).getId());
                idsRef.set(comments.stream().filter(comment -> comment.getWriterId().equals(writers.get(0).getId()))
                                   .map(Comment::getId).collect(toList()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
                    if (threadIndex == 0)
                        commentService.deleteAll(ids, writerId);
                    else
                        assertThatThrownBy(() -> commentService.deleteAll(ids, writerId))
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
        List<Comment> deletedComments = commentMapper.findAll().stream()
                                                     .filter(comment -> ids.contains(comment.getId()) &&
                                                                        comment.getWriterId()
                                                                               .equals(writerId) &&
                                                                        comment.isDeleteYn())
                                                     .collect(toList());
        
        for (Comment comment : deletedComments)
            assertThat(comment.isDeleteYn()).isTrue();
    }
    
}