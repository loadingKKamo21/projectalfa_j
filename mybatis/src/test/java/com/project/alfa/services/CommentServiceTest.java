package com.project.alfa.services;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Comment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.CommentRepository;
import com.project.alfa.repositories.mybatis.CommentMapper;
import com.project.alfa.services.dto.CommentRequestDto;
import com.project.alfa.services.dto.CommentResponseDto;
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
    CommentService    commentService;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CommentMapper     commentMapper;
    @Autowired
    DummyGenerator    dummy;
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 작성")
    void create() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        Member       writer  = writers.get(0);
        CommentRequestDto dto = new CommentRequestDto(null,
                                                      writer.getId(),
                                                      post.getId(),
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        Long id = commentService.create(dto);
        
        //Then
        Comment findComment = commentMapper.findById(id);
        
        assertThat(dto.getWriterId()).isEqualTo(findComment.getWriterId());
        assertThat(dto.getPostId()).isEqualTo(findComment.getPostId());
        assertThat(dto.getContent()).isEqualTo(findComment.getContent());
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 작성, 존재하지 않는 계정")
    void create_unknownWriter() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        
        Long writerId;
        do {
            writerId = new Random().nextLong();
        } while (writers.get(0).getId().equals(writerId));
        
        CommentRequestDto dto = new CommentRequestDto(null,
                                                      writerId,
                                                      posts.get(0).getId(),
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        
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
        Long writerId = dummy.createMembers(1, true).get(0).getId();
        Long postId   = new Random().nextLong();
        CommentRequestDto dto = new CommentRequestDto(null,
                                                      writerId,
                                                      postId,
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        
        //Then
        assertThatThrownBy(() -> commentService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: " + postId);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK로 댓글 상세 조회")
    void read() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id      = comment.getId();
        
        //When
        CommentResponseDto dto = commentService.read(id);
        
        //Then
        Comment findComment = commentMapper.findById(id);
        
        assertThat(findComment.getId()).isEqualTo(dto.getId());
        assertThat(findComment.getNickname()).isEqualTo(dto.getWriter());
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
        
        //Then
        assertThatThrownBy(() -> commentService.read(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: " + id);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK로 댓글 상세 조회, 이미 삭제된 댓글")
    void read_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id      = comment.getId();
        commentMapper.deleteById(id, comment.getWriterId());
        
        //When
        
        //Then
        assertThatThrownBy(() -> commentService.read(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: " + id);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 정보 수정")
    void update() {
        //Given
        List<Member> writers       = dummy.createMembers(1, true);
        List<Post>   posts         = dummy.createPosts(writers, 1, true);
        Comment      comment       = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id            = comment.getId();
        Long         writerId      = comment.getWriterId();
        Long         postId        = comment.getPostId();
        String       beforeContent = comment.getContent();
        
        String afterContent;
        do {
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
        } while (beforeContent.equals(afterContent));
        
        CommentRequestDto dto = new CommentRequestDto(id, writerId, postId, afterContent);
        
        //When
        commentService.update(dto);
        
        //Then
        Comment findComment = commentMapper.findById(id);
        
        assertThat(findComment.getContent()).isEqualTo(afterContent);
        assertThat(findComment.getContent()).isNotEqualTo(beforeContent);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 정보 수정, 접근 권한 없는 계정")
    void update_notWriter() {
        //Given
        List<Member> writers = dummy.createMembers(2, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id      = comment.getId();
        Long         postId  = comment.getPostId();
        
        Long anotherWriterId = null;
        for (Member writer : writers)
            if (!comment.getWriterId().equals(writer.getId())) {
                anotherWriterId = writer.getId();
                break;
            }
        
        CommentRequestDto dto = new CommentRequestDto(id,
                                                      anotherWriterId,
                                                      postId,
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        
        //Then
        assertThatThrownBy(() -> commentService.update(dto))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.");
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 정보 수정, 이미 삭제된 댓글")
    void update_alreadyDeleted() {
        //Given
        List<Member> writers  = dummy.createMembers(1, true);
        List<Post>   posts    = dummy.createPosts(writers, 1, true);
        Comment      comment  = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id       = comment.getId();
        Long         writerId = comment.getWriterId();
        Long         postId   = comment.getPostId();
        commentMapper.deleteById(id, writerId);
        CommentRequestDto dto = new CommentRequestDto(id,
                                                      writerId,
                                                      postId,
                                                      dummy.generateRandomString(dummy.generateRandomNumber(1, 100)));
        
        //When
        
        //Then
        assertThatThrownBy(() -> commentService.update(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: " + id);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 삭제")
    void delete() {
        //Given
        List<Member> writers  = dummy.createMembers(1, true);
        List<Post>   posts    = dummy.createPosts(writers, 1, true);
        Comment      comment  = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id       = comment.getId();
        Long         writerId = comment.getWriterId();
        
        //When
        commentService.delete(id, writerId);
        
        //Then
        Comment deletedComment = commentMapper.findById(id);
        
        assertThat(deletedComment.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 삭제, 접근 권한 없는 계정")
    void delete_notWriter() {
        //Given
        List<Member> writers = dummy.createMembers(2, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Comment      comment = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id      = comment.getId();
        
        Long anotherWriterId = null;
        for (Member writer : writers)
            if (!comment.getWriterId().equals(writer.getId())) {
                anotherWriterId = writer.getId();
                break;
            }
        Long unknownWriterId = anotherWriterId;
        
        //When
        
        //Then
        assertThatThrownBy(() -> commentService.delete(id, unknownWriterId))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.");
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 삭제, 이미 삭제된 댓글")
    void delete_alreadyDeleted() {
        //Given
        List<Member> writers  = dummy.createMembers(1, true);
        List<Post>   posts    = dummy.createPosts(writers, 1, true);
        Comment      comment  = dummy.createComments(writers, posts, 1, true).get(0);
        Long         id       = comment.getId();
        Long         writerId = comment.getWriterId();
        commentMapper.deleteById(id, writerId);
        
        //When
        
        //Then
        assertThatThrownBy(() -> commentService.delete(id, writerId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Comment' by id: " + id);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 목록 삭제")
    void deleteAll() {
        //Given
        List<Member>  writers  = dummy.createMembers(1, true);
        List<Post>    posts    = dummy.createPosts(writers, 1, true);
        int           total    = dummy.generateRandomNumber(10, 50);
        List<Comment> comments = dummy.createComments(writers, posts, total, true);
        Long          writerId = writers.get(0).getId();
        List<Long> ids = comments.stream().filter(comment -> comment.getWriterId().equals(writerId))
                                 .map(Comment::getId).collect(toList());
        
        //When
        commentService.deleteAll(ids, writerId);
        
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
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("댓글 목록 삭제, 접근 권한 없는 계정")
    void deleteAll_notWriter() {
        //Given
        List<Member>  writers  = dummy.createMembers(2, true);
        List<Post>    posts    = dummy.createPosts(writers, 1, true);
        int           total    = dummy.generateRandomNumber(10, 50);
        List<Comment> comments = dummy.createComments(writers, posts, total, true);
        Long          writerId = writers.get(new Random().nextInt(writers.size())).getId();
        List<Long> ids = comments.stream().filter(comment -> !comment.getWriterId().equals(writerId))
                                 .map(Comment::getId).collect(toList());
        
        //When
        
        //Then
        assertThatThrownBy(() -> commentService.deleteAll(ids, writerId))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_WRITER_OF_COMMENT)
                .hasMessage("Member do not have access.");
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
        List<CommentResponseDto> findComments = commentService.findAllPageByWriter(writerId, pageRequest);
        
        //Then
        List<CommentResponseDto> comments = commentMapper.findAll().stream()
                                                         .filter(comment -> comment.getWriterId().equals(writerId))
                                                         .sorted(comparing(Comment::getCreatedDate).reversed())
                                                         .limit(10).map(CommentResponseDto::new).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            CommentResponseDto commentDto     = comments.get(i);
            CommentResponseDto findCommentDto = findComments.get(i);
            
            assertThat(findCommentDto.getId()).isEqualTo(commentDto.getId());
            assertThat(findCommentDto.getWriter()).isEqualTo(commentDto.getWriter());
            assertThat(findCommentDto.getContent()).isEqualTo(commentDto.getContent());
            assertThat(findCommentDto.getCreatedDate()).isEqualTo(commentDto.getCreatedDate());
            assertThat(findCommentDto.getLastModifiedDate()).isEqualTo(commentDto.getLastModifiedDate());
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
        List<CommentResponseDto> findComments = commentService.findAllPageByPost(postId, pageRequest);
        
        //Then
        List<CommentResponseDto> comments = commentMapper.findAll().stream()
                                                         .filter(comment -> comment.getPostId().equals(postId))
                                                         .sorted(comparing(Comment::getCreatedDate).reversed())
                                                         .limit(10).map(CommentResponseDto::new).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            CommentResponseDto commentDto     = comments.get(i);
            CommentResponseDto findCommentDto = findComments.get(i);
            
            assertThat(findCommentDto.getId()).isEqualTo(commentDto.getId());
            assertThat(findCommentDto.getWriter()).isEqualTo(commentDto.getWriter());
            assertThat(findCommentDto.getContent()).isEqualTo(commentDto.getContent());
            assertThat(findCommentDto.getCreatedDate()).isEqualTo(commentDto.getCreatedDate());
            assertThat(findCommentDto.getLastModifiedDate()).isEqualTo(commentDto.getLastModifiedDate());
        }
    }
    
}