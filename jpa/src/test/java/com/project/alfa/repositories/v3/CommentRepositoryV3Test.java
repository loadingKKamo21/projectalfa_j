package com.project.alfa.repositories.v3;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Comment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import lombok.SneakyThrows;
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
class CommentRepositoryV3Test {
    
    @Autowired
    CommentRepositoryV3 commentRepository;
    @PersistenceContext
    EntityManager       em;
    @Autowired
    DummyGenerator      dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    private void randomlyDeleteComments(final List<Comment> comments, final int count) {
        Random random      = new Random();
        int    deleteCount = 0;
        while (count > 0) {
            if (count == deleteCount)
                break;
            Comment comment = comments.get(random.nextInt(comments.size()));
            if (comment.isDeleteYn())
                continue;
            comment.isDelete(true);
            deleteCount++;
        }
    }
    
    @Test
    @DisplayName("댓글 저장")
    void save() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Comment comment = dummy.createComments(writers, posts, 1).get(0);
        
        //When
        Long id = commentRepository.save(comment).getId();
        
        //Then
        Comment findComment = em.find(Comment.class, id);
        
        assertThat(findComment).isEqualTo(comment);
    }
    
    @Test
    @DisplayName("PK로 조회")
    void findById() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Comment comment = dummy.createComments(writers, posts, 1).get(0);
        em.persist(comment);
        Long id = comment.getId();
        
        //When
        Comment findComment = commentRepository.findById(id).get();
        
        //Then
        assertThat(findComment).isEqualTo(comment);
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
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    void findByIdAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Comment comment = dummy.createComments(writers, posts, 1).get(0);
        em.persist(comment);
        Long id = comment.getId();
        
        //When
        Comment findComment = commentRepository.findById(id, false).get();
        
        //Then
        assertThat(findComment).isEqualTo(comment);
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
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 댓글")
    void findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Comment comment = dummy.createComments(writers, posts, 1).get(0);
        em.persist(comment);
        Long id = comment.getId();
        comment.isDelete(true);
        
        //When
        Optional<Comment> deletedComment = commentRepository.findById(id, false);
        
        //Then
        assertThat(deletedComment.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("댓글 목록 조회")
    void findAll() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        
        //When
        List<Comment> findComments = commentRepository.findAll();
        
        //Then
        comments = comments.stream().sorted(comparing(Comment::getId)).collect(toList());
        findComments = findComments.stream().sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(total);
        for (int i = 0; i < total; i++)
            assertThat(findComments.get(i)).isEqualTo(comments.get(i));
    }
    
    @Test
    @DisplayName("삭제 여부로 댓글 목록 조회")
    void findAllByDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100));
        
        //When
        List<Comment> findComments = commentRepository.findAll(false);
        
        //Then
        comments = comments.stream().filter(comment -> !comment.isDeleteYn()).sorted(comparing(Comment::getId))
                           .collect(toList());
        findComments = findComments.stream().sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.get(i)).isEqualTo(comments.get(i));
    }
    
    @Test
    @DisplayName("PK 목록으로 댓글 목록 조회")
    void findAllByIds() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        List<Long> ids = comments.stream().map(Comment::getId).collect(toList());
        
        //When
        List<Comment> findComments = commentRepository.findAll(ids);
        
        //Then
        comments = comments.stream().sorted(comparing(Comment::getId)).collect(toList());
        findComments = findComments.stream().sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(ids.size());
        for (int i = 0; i < ids.size(); i++)
            assertThat(findComments.get(i)).isEqualTo(comments.get(i));
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 댓글 목록 조회")
    void findAllByIdsAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100));
        List<Long> ids = comments.stream().filter(comment -> !comment.isDeleteYn()).map(Comment::getId)
                                 .collect(toList());
        
        //When
        List<Comment> findComments = commentRepository.findAll(ids, false);
        
        //Then
        comments = comments.stream().filter(comment -> ids.contains(comment.getId()) && !comment.isDeleteYn())
                           .sorted(comparing(Comment::getId)).collect(toList());
        findComments = findComments.stream().sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(ids.size());
        for (int i = 0; i < ids.size(); i++)
            assertThat(findComments.get(i)).isEqualTo(comments.get(i));
    }
    
    @Test
    @DisplayName("작성자 기준 댓글 목록 조회")
    void findAllByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        
        //When
        List<Comment> findComments = commentRepository.findAllByWriter(writerId);
        
        //Then
        comments = comments.stream().filter(comment -> comment.getWriter().getId().equals(writerId))
                           .sorted(comparing(Comment::getId)).collect(toList());
        findComments = findComments.stream().sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.get(i)).isEqualTo(comments.get(i));
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 댓글 목록 조회")
    void findAllByWriterAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100));
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        
        //When
        List<Comment> findComments = commentRepository.findAllByWriter(writerId, false);
        
        //Then
        comments = comments.stream()
                           .filter(comment -> comment.getWriter().getId().equals(writerId) && !comment.isDeleteYn())
                           .sorted(comparing(Comment::getId)).collect(toList());
        findComments = findComments.stream().sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.get(i)).isEqualTo(comments.get(i));
    }
    
    @Test
    @DisplayName("게시글 기준 댓글 목록 조회")
    void findAllByPost() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<Comment> findComments = commentRepository.findAllByPost(postId);
        
        //Then
        comments = comments.stream().filter(comment -> comment.getPost().getId().equals(postId))
                           .sorted(comparing(Comment::getId)).collect(toList());
        findComments = findComments.stream().sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.get(i)).isEqualTo(comments.get(i));
    }
    
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 댓글 목록 조회")
    void findAllByPostAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments)
            em.persist(comment);
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100));
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<Comment> findComments = commentRepository.findAllByPost(postId, false);
        
        //Then
        comments = comments.stream()
                           .filter(comment -> comment.getPost().getId().equals(postId) && !comment.isDeleteYn())
                           .sorted(comparing(Comment::getId)).collect(toList());
        findComments = findComments.stream().sorted(comparing(Comment::getId)).collect(toList());
        
        assertThat(findComments.size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.get(i)).isEqualTo(comments.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
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
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments) {
            Thread.sleep(1);
            em.persist(comment);
        }
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Comment> findComments = commentRepository.findAllByWriter(writerId, pageRequest);
        
        //Then
        comments = comments.stream().filter(comment -> comment.getWriter().getId().equals(writerId))
                           .sorted(comparing(Comment::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findComments.getContent().size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.getContent().get(i)).isEqualTo(comments.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageByWriterAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments) {
            Thread.sleep(1);
            em.persist(comment);
        }
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100));
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Comment> findComments = commentRepository.findAllByWriter(writerId, false, pageRequest);
        
        //Then
        comments = comments.stream()
                           .filter(comment -> comment.getWriter().getId().equals(writerId) && !comment.isDeleteYn())
                           .sorted(comparing(Comment::getCreatedDate).reversed())
                           .limit(10).collect(toList());
        
        assertThat(findComments.getContent().size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.getContent().get(i)).isEqualTo(comments.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
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
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments) {
            Thread.sleep(1);
            em.persist(comment);
        }
        Long        postId      = posts.get(new Random().nextInt(posts.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Comment> findComments = commentRepository.findAllByPost(postId, pageRequest);
        
        //Then
        comments = comments.stream().filter(comment -> comment.getPost().getId().equals(postId))
                           .sorted(comparing(Comment::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findComments.getContent().size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.getContent().get(i)).isEqualTo(comments.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageByPostAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int           total    = dummy.generateRandomNumber(100, 300);
        List<Comment> comments = dummy.createComments(writers, posts, total);
        for (Comment comment : comments) {
            Thread.sleep(1);
            em.persist(comment);
        }
        randomlyDeleteComments(comments, dummy.generateRandomNumber(1, 100));
        Long        postId      = posts.get(new Random().nextInt(posts.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Comment> findComments = commentRepository.findAllByPost(postId, false, pageRequest);
        
        //Then
        comments = comments.stream()
                           .filter(comment -> comment.getPost().getId().equals(postId) && !comment.isDeleteYn())
                           .sorted(comparing(Comment::getCreatedDate).reversed())
                           .limit(10).collect(toList());
        
        assertThat(findComments.getContent().size()).isEqualTo(comments.size());
        for (int i = 0; i < comments.size(); i++)
            assertThat(findComments.getContent().get(i)).isEqualTo(comments.get(i));
    }
    
    @Test
    @DisplayName("댓글 수정")
    void update() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Comment comment = dummy.createComments(writers, posts, 1).get(0);
        em.persist(comment);
        Long id = comment.getId();
        clear();
        
        //When
        String newContent;
        do {
            newContent = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
        } while (comment.getContent().equals(newContent));
        
        Comment findComment = commentRepository.findById(id).get();
        findComment.updateContent(newContent);
        clear();
        
        //Then
        Comment updatedComment = em.find(Comment.class, id);
        
        assertThat(updatedComment.getContent()).isEqualTo(newContent);
    }
    
    @Test
    @DisplayName("엔티티로 댓글 정보 영구 삭제")
    void delete() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Comment comment = dummy.createComments(writers, posts, 1).get(0);
        em.persist(comment);
        Long id = comment.getId();
        
        //When
        commentRepository.delete(comment);
        
        //Then
        Comment deletedComment = em.find(Comment.class, id);
        
        assertThat(deletedComment).isNull();
    }
    
    @Test
    @DisplayName("PK로 댓글 정보 영구 삭제")
    void deleteById() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Comment comment = dummy.createComments(writers, posts, 1).get(0);
        em.persist(comment);
        Long id = comment.getId();
        
        //When
        commentRepository.deleteById(id);
        
        //Then
        Comment deletedComment = em.find(Comment.class, id);
        
        assertThat(deletedComment).isNull();
    }
    
    @Test
    @DisplayName("엔티티 목록으로 댓글 정보 목록 영구 삭제")
    void deleteAll() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(50, 100));
        for (Comment comment : comments)
            em.persist(comment);
        
        //When
        commentRepository.deleteAll(comments);
        
        //Then
        List<Long> ids = comments.stream().map(Comment::getId).collect(toList());
        
        for (Long id : ids)
            assertThat(em.find(Comment.class, id)).isNull();
    }
    
    @Test
    @DisplayName("엔티티 목록으로 댓글 정보 목록 영구 삭제")
    void deleteAllInBatch() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(50, 100));
        for (Comment comment : comments)
            em.persist(comment);
        
        //When
        commentRepository.deleteAllInBatch(comments);
        clear();
        
        //Then
        List<Long> ids = comments.stream().map(Comment::getId).collect(toList());
        
        for (Long id : ids)
            assertThat(em.find(Comment.class, id)).isNull();
    }
    
    @Test
    @DisplayName("PK 목록으로 댓글 정보 목록 영구 삭제")
    void deleteAllById() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(50, 100));
        for (Comment comment : comments)
            em.persist(comment);
        List<Long> ids = comments.stream().map(Comment::getId).collect(toList());
        
        //When
        commentRepository.deleteAllById(ids);
        
        //Then
        for (Long id : ids)
            assertThat(em.find(Comment.class, id)).isNull();
    }
    
    @Test
    @DisplayName("PK 목록으로 댓글 정보 목록 영구 삭제")
    void deleteAllByIdInBatch() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Comment> comments = dummy.createComments(writers, posts, dummy.generateRandomNumber(50, 100));
        for (Comment comment : comments)
            em.persist(comment);
        List<Long> ids = comments.stream().map(Comment::getId).collect(toList());
        
        //When
        commentRepository.deleteAllByIdInBatch(ids);
        clear();
        
        //Then
        for (Long id : ids)
            assertThat(em.find(Comment.class, id)).isNull();
    }
    
}