package com.project.alfa.repositories.v2;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.Role;
import com.project.alfa.repositories.dto.SearchParam;
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
class PostRepositoryV2Test {
    
    @Autowired
    PostRepositoryV2 postRepository;
    @PersistenceContext
    EntityManager    em;
    @Autowired
    DummyGenerator   dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    private void randomlyDeletePosts(final List<Post> posts, final int count) {
        Random random      = new Random();
        int    deleteCount = 0;
        while (count > 0) {
            if (count == deleteCount)
                break;
            Post post = posts.get(random.nextInt(posts.size()));
            if (post.isDeleteYn())
                continue;
            post.isDelete(true);
            deleteCount++;
        }
    }
    
    @Test
    @DisplayName("게시글 저장")
    void save() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        Post post = dummy.createPosts(writers, 1).get(0);
        
        //When
        Long id = postRepository.save(post).getId();
        
        //Then
        Post findPost = em.find(Post.class, id);
        
        assertThat(findPost).isEqualTo(post);
    }
    
    @Test
    @DisplayName("PK로 조회")
    void findById() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        Post post = dummy.createPosts(writers, 1).get(0);
        em.persist(post);
        Long id = post.getId();
        
        //When
        Post findPost = postRepository.findById(id).get();
        
        //Then
        assertThat(findPost).isEqualTo(post);
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    void findById_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        Optional<Post> unknownPost = postRepository.findById(id);
        
        //Then
        assertThat(unknownPost.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    void findByIdAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        Post post = dummy.createPosts(writers, 1).get(0);
        em.persist(post);
        Long id = post.getId();
        
        //When
        Post findPost = postRepository.findById(id, false).get();
        
        //Then
        assertThat(findPost).isEqualTo(post);
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    void findByIdAndDeleteYn_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        Optional<Post> unknownPost = postRepository.findById(id, false);
        
        //Then
        assertThat(unknownPost.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 게시글")
    void findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        Post post = dummy.createPosts(writers, 1).get(0);
        em.persist(post);
        Long id = post.getId();
        post.isDelete(true);
        
        //When
        Optional<Post> deletedPost = postRepository.findById(id, false);
        
        //Then
        assertThat(deletedPost.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("게시글 목록 조회")
    void findAll() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts)
            em.persist(post);
        
        //When
        List<Post> findPosts = postRepository.findAll();
        
        //Then
        posts = posts.stream().sorted(comparing(Post::getId)).collect(toList());
        findPosts = findPosts.stream().sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(total);
        for (int i = 0; i < total; i++)
            assertThat(findPosts.get(i)).isEqualTo(posts.get(i));
    }
    
    @Test
    @DisplayName("삭제 여부로 게시글 목록 조회")
    void findAllByDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts)
            em.persist(post);
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100));
        
        //When
        List<Post> findPosts = postRepository.findAll(false);
        
        //Then
        posts = posts.stream().filter(post -> !post.isDeleteYn()).sorted(comparing(Post::getId)).collect(toList());
        findPosts = findPosts.stream().sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.get(i)).isEqualTo(posts.get(i));
    }
    
    @Test
    @DisplayName("PK 목록으로 게시글 목록 조회")
    void findAllByIds() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts)
            em.persist(post);
        List<Long> ids = posts.stream().map(Post::getId).collect(toList());
        
        //When
        List<Post> findPosts = postRepository.findAll(ids);
        
        //Then
        posts = posts.stream().sorted(comparing(Post::getId)).collect(toList());
        findPosts = findPosts.stream().sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(ids.size());
        for (int i = 0; i < ids.size(); i++)
            assertThat(findPosts.get(i)).isEqualTo(posts.get(i));
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 게시글 목록 조회")
    void findAllByIdsAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts)
            em.persist(post);
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100));
        List<Long> ids = posts.stream().filter(post -> !post.isDeleteYn()).map(Post::getId).collect(toList());
        
        //When
        List<Post> findPosts = postRepository.findAll(ids, false);
        
        //Then
        posts = posts.stream().filter(post -> ids.contains(post.getId()) && !post.isDeleteYn())
                     .sorted(comparing(Post::getId)).collect(toList());
        findPosts = findPosts.stream().sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(ids.size());
        for (int i = 0; i < ids.size(); i++)
            assertThat(findPosts.get(i)).isEqualTo(posts.get(i));
    }
    
    @Test
    @DisplayName("작성자 기준 게시글 목록 조회")
    void findAllByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts)
            em.persist(post);
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        
        //When
        List<Post> findPosts = postRepository.findAll(writerId);
        
        //Then
        posts = posts.stream().filter(post -> post.getWriter().getId().equals(writerId)).sorted(comparing(Post::getId))
                     .collect(toList());
        findPosts = findPosts.stream().sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.get(i)).isEqualTo(posts.get(i));
    }
    
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 목록 조회")
    void findAllByWriterAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts)
            em.persist(post);
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100));
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        
        //When
        List<Post> findPosts = postRepository.findAll(writerId, false);
        
        //Then
        posts = posts.stream().filter(post -> post.getWriter().getId().equals(writerId) && !post.isDeleteYn())
                     .sorted(comparing(Post::getId))
                     .collect(toList());
        findPosts = findPosts.stream().sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.get(i)).isEqualTo(posts.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 페이징 목록 조회")
    void findAllPage() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts) {
            Thread.sleep(1);
            em.persist(post);
        }
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Post> findPosts = postRepository.findAll(pageRequest);
        
        //Then
        posts = posts.stream().sorted(comparing(Post::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.getContent().get(i)).isEqualTo(posts.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageByDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts) {
            Thread.sleep(1);
            em.persist(post);
        }
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100));
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Post> findPosts = postRepository.findAll(false, pageRequest);
        
        //Then
        posts = posts.stream().filter(post -> !post.isDeleteYn()).sorted(comparing(Post::getCreatedDate).reversed())
                     .limit(10).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.getContent().get(i)).isEqualTo(posts.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준 게시글 페이징 목록 조회")
    void findAllPageByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts) {
            Thread.sleep(1);
            em.persist(post);
        }
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Post> findPosts = postRepository.findAll(writerId, pageRequest);
        
        //Then
        posts = posts.stream().filter(post -> post.getWriter().getId().equals(writerId))
                     .sorted(comparing(Post::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.getContent().get(i)).isEqualTo(posts.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageByWriterAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts) {
            Thread.sleep(1);
            em.persist(post);
        }
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100));
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Post> findPosts = postRepository.findAll(writerId, false, pageRequest);
        
        //Then
        posts = posts.stream().filter(post -> post.getWriter().getId().equals(writerId) && !post.isDeleteYn())
                     .sorted(comparing(Post::getCreatedDate).reversed())
                     .limit(10).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.getContent().get(i)).isEqualTo(posts.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("검색 조건, 키워드('키워드')로 게시글 페이징 목록 조회")
    void findAllPageBySearchParam_OneWord() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts) {
            Thread.sleep(1);
            em.persist(post);
        }
        String      keyword     = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param       = new SearchParam("title", keyword);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Post> findPosts = postRepository.findAll(param, pageRequest);
        
        //Then
        posts = posts.stream().filter(post -> post.getTitle().contains(keyword))
                     .sorted(comparing(Post::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.getContent().get(i)).isEqualTo(posts.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("검색 조건, 키워드('키워드1 키워드2')로 게시글 페이징 목록 조회")
    void findAllPageBySearchParam_MultiWord() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts) {
            Thread.sleep(1);
            em.persist(post);
        }
        String      keyword1    = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        String      keyword2    = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param       = new SearchParam("titleOrContent", keyword1 + " " + keyword2);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Post> findPosts = postRepository.findAll(param, pageRequest);
        
        //Then
        posts = posts.stream().filter(post -> post.getTitle().contains(keyword1) ||
                                              post.getTitle().contains(keyword2) ||
                                              post.getContent().contains(keyword1) ||
                                              post.getContent().contains(keyword2))
                     .sorted(comparing(Post::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.getContent().get(i)).isEqualTo(posts.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("검색 조건, 키워드('키워드'), 삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageBySearchParamAndDeleteYn_OneWord() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts) {
            Thread.sleep(1);
            em.persist(post);
        }
        String      keyword = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param   = new SearchParam("content", keyword);
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100));
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Post> findPosts = postRepository.findAll(param, false, pageRequest);
        
        //Then
        posts = posts.stream().filter(post -> post.getContent().contains(keyword) && !post.isDeleteYn())
                     .sorted(comparing(Post::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.getContent().get(i)).isEqualTo(posts.get(i));
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("검색 조건, 키워드('키워드1 키워드2'), 삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageBySearchParamAndDeleteYn_MultiWord() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        int        total = dummy.generateRandomNumber(100, 300);
        List<Post> posts = dummy.createPosts(writers, total);
        for (Post post : posts) {
            Thread.sleep(1);
            em.persist(post);
        }
        String      keyword1 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        String      keyword2 = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param    = new SearchParam("titleOrContent", keyword1 + " " + keyword2);
        randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100));
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        Page<Post> findPosts = postRepository.findAll(param, false, pageRequest);
        
        //Then
        posts = posts.stream().filter(post -> (post.getTitle().contains(keyword1) ||
                                               post.getTitle().contains(keyword2) ||
                                               post.getContent().contains(keyword1) ||
                                               post.getContent().contains(keyword2)) &&
                                              !post.isDeleteYn())
                     .sorted(comparing(Post::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findPosts.getContent().size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++)
            assertThat(findPosts.getContent().get(i)).isEqualTo(posts.get(i));
    }
    
    @Test
    @DisplayName("조회수 증가")
    void addViewCount() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        Post post = dummy.createPosts(writers, 1).get(0);
        em.persist(post);
        Long id              = post.getId();
        int  beforeViewCount = post.getViewCount();
        
        //When
        post.addViewCount();
        
        //Then
        int afterViewCount = em.find(Post.class, id).getViewCount();
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1);
    }
    
    @Test
    @DisplayName("게시글 수정")
    void update() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers) {
            em.persist(writer);
            writer.updateRole(Role.ADMIN);
        }
        Post post = dummy.createPosts(writers, 1).get(0);
        em.persist(post);
        Long id = post.getId();
        clear();
        
        //When
        String newTitle;
        String newContent;
        do {
            newTitle = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
            newContent = dummy.generateRandomString(dummy.generateRandomNumber(100, 500));
        } while (post.getTitle().equals(newTitle) || post.getContent().equals(newContent));
        
        Post findPost = postRepository.findById(id).get();
        findPost.updateTitle(newTitle);
        findPost.updateContent(newContent);
        findPost.updateNoticeYn(true);
        clear();
        
        //Then
        Post updatedPost = em.find(Post.class, id);
        
        assertThat(updatedPost.getTitle()).isEqualTo(newTitle);
        assertThat(updatedPost.getContent()).isEqualTo(newContent);
        assertThat(updatedPost.isNoticeYn()).isTrue();
    }
    
    @Test
    @DisplayName("엔티티로 게시글 정보 영구 삭제")
    void delete() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        Post post = dummy.createPosts(writers, 1).get(0);
        em.persist(post);
        Long id = post.getId();
        
        //When
        postRepository.delete(post);
        
        //Then
        Post deletedPost = em.find(Post.class, id);
        
        assertThat(deletedPost).isNull();
    }
    
    @Test
    @DisplayName("PK로 게시글 정보 영구 삭제")
    void deleteById() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        Post post = dummy.createPosts(writers, 1).get(0);
        em.persist(post);
        Long id = post.getId();
        
        //When
        postRepository.deleteById(id);
        
        //Then
        Post deletedPost = em.find(Post.class, id);
        
        assertThat(deletedPost).isNull();
    }
    
    @Test
    @DisplayName("엔티티 목록으로 게시글 정보 목록 영구 삭제")
    void deleteAll() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        
        //When
        postRepository.deleteAll(posts);
        
        //Then
        List<Long> ids = posts.stream().map(Post::getId).collect(toList());
        
        for (Long id : ids)
            assertThat(em.find(Post.class, id)).isNull();
    }
    
    @Test
    @DisplayName("엔티티 목록으로 게시글 정보 목록 영구 삭제")
    void deleteAllInBatch() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        
        //When
        postRepository.deleteAllInBatch(posts);
        clear();
        
        //Then
        List<Long> ids = posts.stream().map(Post::getId).collect(toList());
        
        for (Long id : ids)
            assertThat(em.find(Post.class, id)).isNull();
    }
    
    @Test
    @DisplayName("PK 목록으로 게시글 정보 목록 영구 삭제")
    void deleteAllById() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Long> ids = posts.stream().map(Post::getId).collect(toList());
        
        //When
        postRepository.deleteAllById(ids);
        
        //Then
        for (Long id : ids)
            assertThat(em.find(Post.class, id)).isNull();
    }
    
    @Test
    @DisplayName("PK 목록으로 게시글 정보 목록 영구 삭제")
    void deleteAllByIdInBatch() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Long> ids = posts.stream().map(Post::getId).collect(toList());
        
        //When
        postRepository.deleteAllByIdInBatch(ids);
        clear();
        
        //Then
        for (Long id : ids)
            assertThat(em.find(Post.class, id)).isNull();
    }
    
}