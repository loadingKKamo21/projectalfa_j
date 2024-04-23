package com.project.alfa.services;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Comment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.v1.CommentRepositoryV1;
import com.project.alfa.services.dto.CommentRequestDto;
import com.project.alfa.services.dto.CommentResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Random;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentServiceTest {
    
    @Autowired
    CommentService      commentService;
    @Autowired
    CommentRepositoryV1 commentRepository;
    //@Autowired
    //CommentRepositoryV2 commentRepository;
    //@Autowired
    //CommentRepositoryV3 commentRepository;
    @PersistenceContext
    EntityManager       em;
    @Autowired
    DummyGenerator      dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    @Test
    @DisplayName("댓글 작성")
    void create() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        posts.forEach(post -> em.persist(post));
        CommentRequestDto dto = new CommentRequestDto(null,
                                                      writers.get(0).getId(),
                                                      posts.get(0).getId(),
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        Long id = commentService.create(dto);
        clear();
        
        //Then
        Comment findComment = em.find(Comment.class, id);
        
        assertThat(dto.getWriterId()).isEqualTo(findComment.getWriter().getId());
        assertThat(dto.getPostId()).isEqualTo(findComment.getPost().getId());
        assertThat(dto.getContent()).isEqualTo(findComment.getContent());
    }
    
    @Test
    @DisplayName("댓글 작성, 존재하지 않는 계정")
    void create_unknownWriter() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        posts.forEach(post -> em.persist(post));
        
        Long writerId;
        do {
            writerId = new Random().nextLong();
        } while (writers.get(0).getId().equals(writerId));
        
        CommentRequestDto dto = new CommentRequestDto(null,
                                                      writerId,
                                                      posts.get(0).getId(),
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> commentService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Member' by id: " + writerId);
    }
    
    @Test
    @DisplayName("댓글 작성, 존재하지 않는 게시글")
    void create_unknownPost() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        Long postId = new Random().nextLong();
        
        CommentRequestDto dto = new CommentRequestDto(null,
                                                      writers.get(0).getId(),
                                                      postId,
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> commentService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: " + postId);
    }
    
    @Test
    @DisplayName("PK로 댓글 상세 조회")
    void read() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, 1);
        for (Comment comment : comments)
            em.persist(comment);
        Long id = comments.get(0).getId();
        
        //When
        CommentResponseDto dto = commentService.read(id);
        clear();
        
        //Then
        Comment findComment = em.find(Comment.class, id);
        
        assertThat(findComment.getId()).isEqualTo(dto.getId());
        assertThat(findComment.getWriter().getNickname()).isEqualTo(dto.getWriter());
        assertThat(findComment.getContent()).isEqualTo(dto.getContent());
        assertThat(findComment.getCreatedDate()).isEqualTo(dto.getCreatedDate());
        assertThat(findComment.getLastModifiedDate()).isEqualTo(dto.getLastModifiedDate());
    }
    
    @Test
    @DisplayName("PK로 댓글 상세 조회, 존재하지 않는 PK")
    void read_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> commentService.read(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: " + id);
    }
    
    @Test
    @DisplayName("PK로 댓글 상세 조회, 이미 삭제된 댓글")
    void read_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, 1);
        for (Comment comment : comments)
            em.persist(comment);
        Comment comment = comments.get(0);
        Long    id      = comment.getId();
        comment.isDelete(true);
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> commentService.read(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: " + id);
    }
    
    @Test
    @DisplayName("댓글 정보 수정")
    void update() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, 1);
        for (Comment comment : comments)
            em.persist(comment);
        Comment comment       = comments.get(0);
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
        commentService.update(dto);
        clear();
        
        //Then
        Comment findComment = em.find(Comment.class, id);
        
        assertThat(findComment.getContent()).isEqualTo(afterContent);
        assertThat(findComment.getContent()).isNotEqualTo(beforeContent);
    }
    
    @Test
    @DisplayName("댓글 정보 수정, 접근 권한 없는 계정")
    void update_notWriter() {
        //Given
        List<Member> writers = dummy.createMembers(2);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, 1);
        for (Comment comment : comments)
            em.persist(comment);
        Comment comment = comments.get(0);
        Long    id      = comment.getId();
        
        Long anotherWriterId = null;
        for (Member writer : writers)
            if (!comment.getWriter().getId().equals(writer.getId())) {
                anotherWriterId = writer.getId();
                break;
            }
        
        CommentRequestDto dto = new CommentRequestDto(id,
                                                      anotherWriterId,
                                                      comment.getPost().getId(),
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> commentService.update(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.");
    }
    
    @Test
    @DisplayName("댓글 정보 수정, 이미 삭제된 댓글")
    void update_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, 1);
        for (Comment comment : comments)
            em.persist(comment);
        Comment comment = comments.get(0);
        Long    id      = comment.getId();
        comment.isDelete(true);
        
        CommentRequestDto dto = new CommentRequestDto(id,
                                                      comment.getWriter().getId(),
                                                      comment.getPost().getId(),
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> commentService.update(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: " + id);
    }
    
    @Test
    @DisplayName("댓글 삭제")
    void delete() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, 1);
        for (Comment comment : comments)
            em.persist(comment);
        Comment comment  = comments.get(0);
        Long    id       = comment.getId();
        Long    writerId = comment.getWriter().getId();
        
        //When
        commentService.delete(id, writerId);
        clear();
        
        //Then
        Comment deletedComment = em.find(Comment.class, id);
        
        assertThat(deletedComment.isDeleteYn()).isTrue();
    }
    
    @Test
    @DisplayName("댓글 삭제, 접근 권한 없는 계정")
    void delete_notWriter() {
        //Given
        List<Member> writers = dummy.createMembers(2);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, 1);
        for (Comment comment : comments)
            em.persist(comment);
        Comment comment = comments.get(0);
        Long    id      = comment.getId();
        
        Long anotherWriterId = null;
        for (Member writer : writers)
            if (!comment.getWriter().getId().equals(writer.getId())) {
                anotherWriterId = writer.getId();
                break;
            }
        Long unknownWriterId = anotherWriterId;
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> commentService.delete(id, unknownWriterId))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.");
    }
    
    @Test
    @DisplayName("댓글 목록 삭제")
    void deleteAll() {
        //Given
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
        Long writerId = writers.get(0).getId();
        List<Long> ids = comments.stream().filter(comment -> comment.getWriter().getId().equals(writerId))
                                 .map(Comment::getId).collect(toList());
        
        //When
        commentService.deleteAll(ids, writerId);
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
    
    @Test
    @DisplayName("댓글 목록 삭제, 접근 권한 없는 계정")
    void deleteAll_notWriter() {
        //Given
        List<Member> writers = dummy.createMembers(2);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(10, 50);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        List<Long> ids = comments.stream().filter(comment -> !comment.getWriter().getId().equals(writerId))
                                 .map(Comment::getId).collect(toList());
        
        //When
        clear();
        
        //Then
        assertThatThrownBy(() -> commentService.deleteAll(ids, writerId))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.");
    }
    
    @Test
    @DisplayName("작성자 기준 댓글 페이징 목록 조회")
    void findAllPageByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int total = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total).forEach(comment -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            em.persist(comment);
        });
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<CommentResponseDto> findComments = commentService.findAllPageByWriter(writerId, pageRequest);
        clear();
        
        //Then
        List<CommentResponseDto> comments = commentRepository.findAll().stream()
                                                             .filter(comment -> comment.getWriter().getId()
                                                                                       .equals(writerId))
                                                             .sorted(comparing(Comment::getCreatedDate).reversed())
                                                             .limit(10).map(CommentResponseDto::new).collect(toList());
        
        assertThat(findComments.getContent().size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            CommentResponseDto commentDto     = comments.get(i);
            CommentResponseDto findCommentDto = findComments.getContent().get(i);
            
            assertThat(findCommentDto.getId()).isEqualTo(commentDto.getId());
            assertThat(findCommentDto.getWriter()).isEqualTo(commentDto.getWriter());
            assertThat(findCommentDto.getContent()).isEqualTo(commentDto.getContent());
            assertThat(findCommentDto.getCreatedDate()).isEqualTo(commentDto.getCreatedDate());
            assertThat(findCommentDto.getLastModifiedDate()).isEqualTo(commentDto.getLastModifiedDate());
        }
    }
    
    @Test
    @DisplayName("게시글 기준 댓글 페이징 목록 조회")
    void findAllPageByPost() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int total = dummy.generateRandomNumber(100, 300);
        dummy.createComments(writers, posts, total).forEach(comment -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            em.persist(comment);
        });
        Long        postId      = posts.get(new Random().nextInt(posts.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<CommentResponseDto> findComments = commentService.findAllPageByPost(postId, pageRequest);
        clear();
        
        //Then
        List<CommentResponseDto> comments = commentRepository.findAll().stream()
                                                             .filter(comment -> comment.getPost().getId()
                                                                                       .equals(postId))
                                                             .sorted(comparing(Comment::getCreatedDate).reversed())
                                                             .limit(10).map(CommentResponseDto::new).collect(toList());
        
        assertThat(findComments.getContent().size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            CommentResponseDto commentDto     = comments.get(i);
            CommentResponseDto findCommentDto = findComments.getContent().get(i);
            
            assertThat(findCommentDto.getId()).isEqualTo(commentDto.getId());
            assertThat(findCommentDto.getWriter()).isEqualTo(commentDto.getWriter());
            assertThat(findCommentDto.getContent()).isEqualTo(commentDto.getContent());
            assertThat(findCommentDto.getCreatedDate()).isEqualTo(commentDto.getCreatedDate());
            assertThat(findCommentDto.getLastModifiedDate()).isEqualTo(commentDto.getLastModifiedDate());
        }
    }
    
}