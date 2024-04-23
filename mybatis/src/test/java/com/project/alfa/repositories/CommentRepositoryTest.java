package com.project.alfa.repositories;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Comment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.repositories.mybatis.CommentMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {
    
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CommentMapper     commentMapper;
    @Autowired
    DummyGenerator    dummy;
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 저장")
    void save() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Comment      comment = dummy.createComments(writers, posts, 1, false).get(0);
        
        //When
        Comment savedComment = commentRepository.save(comment);
        Long    id           = savedComment.getId();
        
        //Then
        Comment findComment = commentMapper.findById(id);
        
        assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
        assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
        assertThat(findComment.getContent()).isEqualTo(comment.getContent());
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK로 조회")
    void findById() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id      = comment.getId();
        
        //When
        Comment findComment = commentRepository.findById(id).get();
        
        //Then
        assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
        assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
        assertThat(findComment.getContent()).isEqualTo(comment.getContent());
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    void findById_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        Optional<Comment> unknownComment = commentRepository.findById(id);
        
        //Then
        assertThat(unknownComment.isPresent()).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    void findByIdAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id      = comment.getId();
        
        //When
        Comment findComment = commentRepository.findById(id, false).get();
        
        //Then
        assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
        assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
        assertThat(findComment.getContent()).isEqualTo(comment.getContent());
        assertThat(findComment.isDeleteYn()).isFalse();
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    void findByIdAndDeleteYn_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        Optional<Comment> unknownComment = commentRepository.findById(id, false);
        
        //Then
        assertThat(unknownComment.isPresent()).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 댓글")
    void findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id      = comment.getId();
        commentMapper.deleteById(id, comment.getWriterId());
        
        //When
        Optional<Comment> deletedComment = commentRepository.findById(id, false);
        
        //Then
        assertThat(deletedComment.isPresent()).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 목록 조회")
    void findAll() {
        //Given
        List<Member>  writers  = dummy.createMembers(20, true);
        List<Post>    posts    = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total, true);
        
        //When
        List<Comment> findComments = commentRepository.findAll().stream().sorted(comparing(Comment::getId))
                                                      .collect(toList());
        
        //Then
        assertThat(findComments.size()).isEqualTo(total);
        for (int i = 0; i < total; i++) {
            Comment comment     = comments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("삭제 여부로 댓글 목록 조회")
    void findAllByDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100));
        
        //When
        List<Comment> findComments = commentRepository.findAll(false).stream().sorted(comparing(Comment::getId))
                                                      .collect(toList());
        
        //Then
        List<Comment> undeletedComments = commentMapper.findAll().stream().filter(comment -> !comment.isDeleteYn())
                                                       .sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(undeletedComments.size());
        for (int i = 0; i < undeletedComments.size(); i++) {
            Comment comment     = undeletedComments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
            assertThat(findComment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK 목록으로 댓글 목록 조회")
    void findAllByIds() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        List<Long> ids = dummy.createComments(writers, posts, total, true).stream().map(Comment::getId)
                              .collect(toList());
        
        //When
        List<Comment> findComments = commentRepository.findAll(ids);
        
        //Then
        List<Comment> comments = commentMapper.findAll().stream().filter(comment -> ids.contains(comment.getId()))
                                              .sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            Comment comment     = comments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK 목록, 삭제 여부로 댓글 목록 조회")
    void findAllByIdsAndDeleteYn() {
        //Given
        List<Member>  writers  = dummy.createMembers(20, true);
        List<Post>    posts    = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total, true);
        dummy.randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100));
        List<Long> ids = comments.stream().filter(comment -> !comment.isDeleteYn()).map(Comment::getId)
                                 .collect(toList());
        
        //When
        List<Comment> findComments = commentRepository.findAll(ids, false);
        
        //Then
        List<Comment> undeletedComments = commentMapper.findAll().stream()
                                                       .filter(comment -> ids.contains(comment.getId()) &&
                                                                          !comment.isDeleteYn())
                                                       .sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(undeletedComments.size());
        for (int i = 0; i < undeletedComments.size(); i++) {
            Comment comment     = undeletedComments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
            assertThat(findComment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준 댓글 목록 조회")
    void findAllByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        
        //When
        List<Comment> findComments = commentRepository.findAllByWriter(writerId);
        
        //Then
        List<Comment> comments = commentMapper.findAll().stream()
                                              .filter(comment -> comment.getWriterId().equals(writerId))
                                              .sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            Comment comment     = comments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 댓글 목록 조회")
    void findAllByWriterAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100));
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        
        //When
        List<Comment> findComments = commentRepository.findAllByWriter(writerId, false);
        
        //Then
        List<Comment> undeletedComments = commentMapper.findAll().stream()
                                                       .filter(comment -> comment.getWriterId().equals(writerId) &&
                                                                          !comment.isDeleteYn())
                                                       .sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(undeletedComments.size());
        for (int i = 0; i < undeletedComments.size(); i++) {
            Comment comment     = undeletedComments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
            assertThat(findComment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 기준 댓글 목록 조회")
    void findAllByPost() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<Comment> findComments = commentRepository.findAllByPost(postId);
        
        //Then
        List<Comment> comments = commentMapper.findAll().stream()
                                              .filter(comment -> comment.getPostId().equals(postId))
                                              .sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            Comment comment     = comments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 댓글 목록 조회")
    void findAllByPostAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100));
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<Comment> findComments = commentRepository.findAllByPost(postId, false);
        
        //Then
        List<Comment> undeletedComments = commentMapper.findAll().stream()
                                                       .filter(comment -> comment.getPostId().equals(postId) &&
                                                                          !comment.isDeleteYn())
                                                       .sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(undeletedComments.size());
        for (int i = 0; i < undeletedComments.size(); i++) {
            Comment comment     = undeletedComments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
            assertThat(findComment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준 댓글 페이징 목록 조회")
    void findAllPageByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Comment> findComments = commentRepository.findAllByWriter(writerId, pageRequest);
        
        //Then
        List<Comment> comments = commentMapper.findAll().stream()
                                              .filter(comment -> comment.getWriterId().equals(writerId))
                                              .sorted(comparing(Comment::getCreatedDate).reversed()).limit(10)
                                              .collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            Comment comment     = comments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 댓글 페이징 목록 조회")
    void findAllPageByWriterAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100));
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Comment> findComments = commentRepository.findAllByWriter(writerId, false, pageRequest);
        
        //Then
        List<Comment> undeletedComments = commentMapper.findAll().stream()
                                                       .filter(comment -> comment.getWriterId().equals(writerId) &&
                                                                          !comment.isDeleteYn())
                                                       .sorted(comparing(Comment::getCreatedDate).reversed()).limit(10)
                                                       .collect(toList());
        
        assertThat(findComments.size()).isEqualTo(undeletedComments.size());
        for (int i = 0; i < undeletedComments.size(); i++) {
            Comment comment     = undeletedComments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
            assertThat(findComment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 기준 댓글 페이징 목록 조회")
    void findAllPageByPost() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        Long        postId      = posts.get(new Random().nextInt(posts.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Comment> findComments = commentRepository.findAllByPost(postId, pageRequest);
        
        //Then
        List<Comment> comments = commentMapper.findAll().stream()
                                              .filter(comment -> comment.getPostId().equals(postId))
                                              .sorted(comparing(Comment::getCreatedDate).reversed()).limit(10)
                                              .collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            Comment comment     = comments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 댓글 페이징 목록 조회")
    void findAllPageByPostAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total, true);
        dummy.randomlyDeleteComments(commentMapper.findAll(), dummy.generateRandomNumber(1, 100));
        Long        postId      = posts.get(new Random().nextInt(posts.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Comment> findComments = commentRepository.findAllByPost(postId, false, pageRequest);
        
        //Then
        List<Comment> undeletedComments = commentMapper.findAll().stream()
                                                       .filter(comment -> comment.getPostId().equals(postId) &&
                                                                          !comment.isDeleteYn())
                                                       .sorted(comparing(Comment::getCreatedDate).reversed()).limit(10)
                                                       .collect(toList());
        
        assertThat(findComments.size()).isEqualTo(undeletedComments.size());
        for (int i = 0; i < undeletedComments.size(); i++) {
            Comment comment     = undeletedComments.get(i);
            Comment findComment = findComments.get(i);
            
            assertThat(findComment.getWriterId()).isEqualTo(comment.getWriterId());
            assertThat(findComment.getPostId()).isEqualTo(comment.getPostId());
            assertThat(findComment.getContent()).isEqualTo(comment.getContent());
            assertThat(findComment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 수정")
    void update() {
        //Given
        List<Member> writers       = dummy.createMembers(1, true);
        List<Post>   posts         = dummy.createPosts(writers, 1, true);
        Comment      comment       = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id            = comment.getId();
        String       beforeContent = comment.getContent();
        
        String afterContent;
        do {
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
        } while (beforeContent.equals(afterContent));
        
        Comment param = Comment.builder().id(id).writerId(comment.getWriterId()).postId(comment.getPostId())
                               .content(afterContent).build();
        
        //When
        commentRepository.update(param);
        
        //Then
        Comment findComment = commentMapper.findById(id);
        
        assertThat(findComment.getContent()).isEqualTo(afterContent);
        assertThat(findComment.getContent()).isNotEqualTo(beforeContent);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 삭제")
    void deleteById() {
        //Given
        List<Member> writers  = dummy.createMembers(1, true);
        List<Post>   posts    = dummy.createPosts(writers, 1, true);
        Comment      comment  = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id       = comment.getId();
        Long         writerId = comment.getWriterId();
        
        //When
        commentRepository.deleteById(id, writerId);
        
        //Then
        Comment deletedComment = commentMapper.findById(id);
        
        assertThat(deletedComment.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 정보 영구 삭제")
    void permanentlyDeleteById() {
        //Given
        List<Member> writers  = dummy.createMembers(1, true);
        List<Post>   posts    = dummy.createPosts(writers, 1, true);
        Comment      comment  = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id       = comment.getId();
        Long         writerId = comment.getWriterId();
        commentMapper.deleteById(id, writerId);
        
        //When
        commentRepository.permanentlyDeleteById(id);
        
        //Then
        Comment unknownComment = commentMapper.findById(id);
        
        assertThat(unknownComment).isNull();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 목록 삭제")
    void deleteAllByIds() {
        //Given
        List<Member>  writers  = dummy.createMembers(20, true);
        List<Post>    posts    = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        List<Comment> comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(100, 300), true);
        Long          writerId = writers.get(new Random().nextInt(writers.size())).getId();
        List<Long> ids = comments.stream().filter(comment -> comment.getWriterId().equals(writerId))
                                 .map(Comment::getId).collect(toList());
        
        //When
        commentRepository.deleteAllByIds(ids, writerId);
        
        //Then
        List<Comment> deletedComments = commentMapper.findAll().stream()
                                                     .filter(comment -> comment.getWriterId().equals(writerId))
                                                     .collect(toList());
        
        for (Comment comment : deletedComments)
            assertThat(comment.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 정보 목록 영구 삭제")
    void permanentlyDeleteAllByIds() {
        //Given
        List<Member>  writers  = dummy.createMembers(20, true);
        List<Post>    posts    = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        List<Comment> comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(100, 300), true);
        Long          writerId = writers.get(new Random().nextInt(writers.size())).getId();
        List<Long> ids = comments.stream().filter(comment -> comment.getWriterId().equals(writerId))
                                 .map(Comment::getId).collect(toList());
        commentMapper.deleteAllByIds(ids, writerId);
        
        //When
        commentRepository.permanentlyDeleteAllByIds(ids);
        
        //Then
        List<Comment> unknownComments = commentMapper.findAll().stream()
                                                     .filter(comment -> comment.getWriterId().equals(writerId))
                                                     .collect(toList());
        
        assertThat(unknownComments).isEmpty();
    }
    
}