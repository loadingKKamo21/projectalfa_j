package com.project.alfa.repositories;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.repositories.dto.SearchParam;
import com.project.alfa.repositories.mybatis.PostMapper;
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
class PostRepositoryTest {
    
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostMapper     postMapper;
    @Autowired
    DummyGenerator dummy;
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 저장")
    void save() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, false).get(0);
        
        //When
        Post savedPost = postRepository.save(post);
        Long id        = savedPost.getId();
        
        //Then
        Post findPost = postMapper.findById(id);
        
        assertThat(findPost.getWriterId()).isEqualTo(savedPost.getWriterId());
        assertThat(findPost.getTitle()).isEqualTo(savedPost.getTitle());
        assertThat(findPost.getContent()).isEqualTo(savedPost.getContent());
        assertThat(findPost.isNoticeYn()).isEqualTo(savedPost.isNoticeYn());
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK로 조회")
    void findById() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        Long         id      = post.getId();
        
        //When
        Post findPost = postRepository.findById(id).get();
        
        //Then
        assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
        assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
        assertThat(findPost.getContent()).isEqualTo(post.getContent());
        assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
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
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    void findByIdAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        Long         id      = post.getId();
        
        //When
        Post findPost = postRepository.findById(id, false).get();
        
        //Then
        assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
        assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
        assertThat(findPost.getContent()).isEqualTo(post.getContent());
        assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
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
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 게시글")
    void findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        Long         id      = post.getId();
        postMapper.deleteById(id, post.getWriterId());
        
        //When
        Optional<Post> deletedPost = postRepository.findById(id, false);
        
        //Then
        assertThat(deletedPost.isPresent()).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 목록 조회")
    void findAll() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        List<Post>   posts   = dummy.createPosts(writers, total, true);
        
        //When
        List<Post> findPosts = postRepository.findAll().stream().sorted(comparing(Post::getId)).collect(toList());
        
        //Then
        assertThat(findPosts.size()).isEqualTo(total);
        for (int i = 0; i < total; i++) {
            Post post     = posts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("삭제 여부로 게시글 목록 조회")
    void findAllByDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100));
        
        //When
        List<Post> findPosts = postRepository.findAll(false).stream().sorted(comparing(Post::getId)).collect(toList());
        
        //Then
        List<Post> undeletedPosts = postMapper.findAll().stream().filter(post -> !post.isDeleteYn())
                                              .sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(undeletedPosts.size());
        for (int i = 0; i < undeletedPosts.size(); i++) {
            Post post     = undeletedPosts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
            assertThat(findPost.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK 목록으로 게시글 목록 조회")
    void findAllByIds() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        List<Long>   ids     = dummy.createPosts(writers, total, true).stream().map(Post::getId).collect(toList());
        
        //When
        List<Post> findPosts = postRepository.findAll(ids);
        
        //Then
        List<Post> posts = postMapper.findAll().stream().filter(post -> ids.contains(post.getId()))
                                     .sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post     = posts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK 목록, 삭제 여부로 게시글 목록 조회")
    void findAllByIdsAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        List<Post>   posts   = dummy.createPosts(writers, total, true);
        dummy.randomlyDeletePosts(posts, dummy.generateRandomNumber(1, 100));
        List<Long> ids = posts.stream().filter(post -> !post.isDeleteYn()).map(Post::getId).collect(toList());
        
        //When
        List<Post> findPosts = postRepository.findAll(ids, false);
        
        //Then
        List<Post> undeletedPosts = postMapper.findAll().stream()
                                              .filter(post -> ids.contains(post.getId()) && !post.isDeleteYn())
                                              .sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(undeletedPosts.size());
        for (int i = 0; i < undeletedPosts.size(); i++) {
            Post post     = undeletedPosts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
            assertThat(findPost.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준 게시글 목록 조회")
    void findAllByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        
        //When
        List<Post> findPosts = postRepository.findAll(writerId);
        
        //Then
        List<Post> posts = postMapper.findAll().stream().filter(post -> post.getWriterId().equals(writerId))
                                     .sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post     = posts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 목록 조회")
    void findAllByWriterAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100));
        Long writerId = writers.get(new Random().nextInt(writers.size())).getId();
        
        //When
        List<Post> findPosts = postRepository.findAll(writerId, false);
        
        //Then
        List<Post> posts = postMapper.findAll()
                                     .stream().filter(post -> post.getWriterId().equals(writerId) && !post.isDeleteYn())
                                     .sorted(comparing(Post::getId)).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post     = posts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
            assertThat(findPost.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 페이징 목록 조회")
    void findAllPage() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Post> findPosts = postRepository.findAll(pageRequest);
        
        //Then
        List<Post> posts = postMapper.findAll().stream().sorted(comparing(Post::getCreatedDate).reversed()).limit(10)
                                     .collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post     = posts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageByDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100));
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Post> findPosts = postRepository.findAll(false, pageRequest);
        
        //Then
        List<Post> undeletedPosts = postMapper.findAll().stream().filter(post -> !post.isDeleteYn())
                                              .sorted(comparing(Post::getCreatedDate).reversed()).limit(10)
                                              .collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(undeletedPosts.size());
        for (int i = 0; i < undeletedPosts.size(); i++) {
            Post post     = undeletedPosts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
            assertThat(findPost.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준 게시글 페이징 목록 조회")
    void findAllPageByWriter() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Post> findPosts = postRepository.findAll(writerId, pageRequest);
        
        //Then
        List<Post> posts = postMapper.findAll().stream().filter(post -> post.getWriterId().equals(writerId))
                                     .sorted(comparing(Post::getCreatedDate).reversed()).limit(10)
                                     .collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post     = posts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("작성자 기준, 삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageByWriterAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100));
        Long        writerId    = writers.get(new Random().nextInt(writers.size())).getId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Post> findPosts = postRepository.findAll(writerId, false, pageRequest);
        
        //Then
        List<Post> undeletedPosts = postMapper.findAll().stream()
                                              .filter(post -> post.getWriterId().equals(writerId) && !post.isDeleteYn())
                                              .sorted(comparing(Post::getCreatedDate).reversed()).limit(10)
                                              .collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(undeletedPosts.size());
        for (int i = 0; i < undeletedPosts.size(); i++) {
            Post post     = undeletedPosts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
            assertThat(findPost.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("검색 조건, 키워드('키워드')로 게시글 페이징 목록 조회")
    void findAllPageBySearchParam_OneWord() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        String      keyword     = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param       = new SearchParam("title", keyword);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Post> findPosts = postRepository.findAll(param, pageRequest);
        
        //Then
        List<Post> posts = postMapper.findAll().stream().filter(post -> post.getTitle().contains(keyword))
                                     .sorted(comparing(Post::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post     = posts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("검색 조건, 키워드('키워드1 키워드2')로 게시글 페이징 목록 조회")
    void findAllPageBySearchParam_MultiWord() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        String      keyword1    = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        String      keyword2    = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param       = new SearchParam("titleOrContent", keyword1 + " " + keyword2);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Post> findPosts = postRepository.findAll(param, pageRequest);
        
        //Then
        List<Post> posts = postMapper.findAll().stream().filter(post -> post.getTitle().contains(keyword1) ||
                                                                        post.getTitle().contains(keyword2) ||
                                                                        post.getContent().contains(keyword1) ||
                                                                        post.getContent().contains(keyword2))
                                     .sorted(comparing(Post::getCreatedDate).reversed()).limit(10).collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post     = posts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("검색 조건, 키워드('키워드'), 삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageBySearchParamAndDeleteYn_OneWord() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100));
        String      keyword     = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param       = new SearchParam("content", keyword);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Post> findPosts = postRepository.findAll(param, false, pageRequest);
        
        //Then
        List<Post> undeletedPosts = postMapper.findAll().stream()
                                              .filter(post -> post.getContent().contains(keyword) && !post.isDeleteYn())
                                              .sorted(comparing(Post::getCreatedDate).reversed()).limit(10)
                                              .collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(undeletedPosts.size());
        for (int i = 0; i < undeletedPosts.size(); i++) {
            Post post     = undeletedPosts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
            assertThat(findPost.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("검색 조건, 키워드('키워드1 키워드2'), 삭제 여부로 게시글 페이징 목록 조회")
    void findAllPageBySearchParamAndDeleteYn_MultiWords() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createPosts(writers, total, true);
        dummy.randomlyDeletePosts(postMapper.findAll(), dummy.generateRandomNumber(1, 100));
        String      keyword1    = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        String      keyword2    = dummy.generateRandomString(dummy.generateRandomNumber(3, 5));
        SearchParam param       = new SearchParam("titleOrContent", keyword1 + " " + keyword2);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        //When
        List<Post> findPosts = postRepository.findAll(param, false, pageRequest);
        
        //Then
        List<Post> undeletedPosts = postMapper.findAll().stream()
                                              .filter(post -> (post.getTitle().contains(keyword1) ||
                                                               post.getTitle().contains(keyword2) ||
                                                               post.getContent().contains(keyword1) ||
                                                               post.getContent().contains(keyword2)) &&
                                                              !post.isDeleteYn())
                                              .sorted(comparing(Post::getCreatedDate).reversed()).limit(10)
                                              .collect(toList());
        
        assertThat(findPosts.size()).isEqualTo(undeletedPosts.size());
        for (int i = 0; i < undeletedPosts.size(); i++) {
            Post post     = undeletedPosts.get(i);
            Post findPost = findPosts.get(i);
            
            assertThat(findPost.getWriterId()).isEqualTo(post.getWriterId());
            assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
            assertThat(findPost.getContent()).isEqualTo(post.getContent());
            assertThat(findPost.isNoticeYn()).isEqualTo(post.isNoticeYn());
            assertThat(findPost.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("조회수 증가")
    void addViewCount() {
        //Given
        List<Member> writers         = dummy.createMembers(1, true);
        Post         post            = dummy.createPosts(writers, 1, true).get(0);
        Long         id              = post.getId();
        int          beforeViewCount = postMapper.findById(id).getViewCount();
        
        //When
        postRepository.addViewCount(id);
        
        //Then
        int afterViewCount = postMapper.findById(id).getViewCount();
        
        assertThat(afterViewCount).isEqualTo(beforeViewCount + 1);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 수정")
    void update() {
        //Given
        List<Member> writers        = dummy.createMembers(1, true);
        Post         post           = dummy.createPosts(writers, 1, true).get(0);
        Long         id             = post.getId();
        String       beforeTitle    = post.getTitle();
        String       beforeContent  = post.getContent();
        boolean      beforeNoticeYn = post.isNoticeYn();
        
        String afterTitle;
        String afterContent;
        do {
            afterTitle = dummy.generateRandomString(dummy.generateRandomNumber(1, 100));
            afterContent = dummy.generateRandomString(dummy.generateRandomNumber(100, 500));
        } while (beforeTitle.equals(afterTitle) || beforeContent.equals(afterContent));
        
        Post param = Post.builder().id(id).writerId(post.getWriterId()).title(afterTitle).content(afterContent)
                         .noticeYn(true).build();
        
        //When
        postRepository.update(param);
        
        //Then
        Post findPost = postMapper.findById(id);
        
        assertThat(findPost.getTitle()).isEqualTo(afterTitle);
        assertThat(findPost.getTitle()).isNotEqualTo(beforeTitle);
        assertThat(findPost.getContent()).isEqualTo(afterContent);
        assertThat(findPost.getContent()).isNotEqualTo(beforeContent);
        assertThat(beforeNoticeYn).isFalse();
        assertThat(findPost.isNoticeYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK로 엔티티 존재 여부 확인")
    void existsById() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        Long         id      = post.getId();
        
        //When
        boolean exists = postRepository.existsById(id);
        
        //Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("PK로 엔티티 존재 여부 확인, 없음")
    void existsById_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        boolean exists = postRepository.existsById(id);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK, 삭제 여부로 엔티티 존재 여부 확인")
    void existsByIdAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        Long         id      = post.getId();
        
        //When
        boolean exists = postRepository.existsById(id, false);
        
        //Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 엔티티 존재 여부 확인, 없음")
    void existsByIdAndDeleteYn_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        boolean exists = postRepository.existsById(id, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK, 삭제 여부로 엔티티 존재 여부 확인, 이미 삭제된 게시글")
    void existsByIdAndDeleteYn_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        Long         id      = post.getId();
        postMapper.deleteById(id, post.getWriterId());
        
        //When
        boolean exists = postRepository.existsById(id, false);
        
        //Then
        assertThat(exists).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 삭제")
    void deleteById() {
        //Given
        List<Member> writers  = dummy.createMembers(1, true);
        Post         post     = dummy.createPosts(writers, 1, true).get(0);
        Long         id       = post.getId();
        Long         writerId = post.getWriterId();
        
        //When
        postRepository.deleteById(id, writerId);
        
        //Then
        Post deletedPost = postMapper.findById(id);
        
        assertThat(deletedPost.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 정보 영구 삭제")
    void permanentlyDeleteById() {
        //Given
        List<Member> writers  = dummy.createMembers(1, true);
        Post         post     = dummy.createPosts(writers, 1, true).get(0);
        Long         id       = post.getId();
        Long         writerId = post.getWriterId();
        postMapper.deleteById(id, writerId);
        
        //When
        postRepository.permanentlyDeleteById(id);
        
        //Then
        Post unknownPost = postMapper.findById(id);
        
        assertThat(unknownPost).isNull();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 목록 삭제")
    void deleteAllByIds() {
        //Given
        List<Member> writers  = dummy.createMembers(20, true);
        List<Post>   posts    = dummy.createPosts(writers, dummy.generateRandomNumber(100, 300), true);
        Long         writerId = writers.get(new Random().nextInt(writers.size())).getId();
        List<Long> ids = posts.stream().filter(post -> post.getWriterId().equals(writerId)).map(Post::getId)
                              .collect(toList());
        
        //When
        postRepository.deleteAllByIds(ids, writerId);
        
        //Then
        List<Post> deletedPosts = postMapper.findAll().stream().filter(post -> post.getWriterId().equals(writerId))
                                            .collect(toList());
        
        for (Post post : deletedPosts)
            assertThat(post.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 정보 목록 영구 삭제")
    void permanentlyDeleteAllByIds() {
        //Given
        List<Member> writers  = dummy.createMembers(20, true);
        List<Post>   posts    = dummy.createPosts(writers, dummy.generateRandomNumber(100, 300), true);
        Long         writerId = writers.get(new Random().nextInt(writers.size())).getId();
        List<Long> ids = posts.stream().filter(post -> post.getWriterId().equals(writerId)).map(Post::getId)
                              .collect(toList());
        postMapper.deleteAllByIds(ids, writerId);
        
        //When
        postRepository.permanentlyDeleteAllByIds(ids);
        
        //Then
        List<Post> unknownPosts = postMapper.findAll().stream().filter(post -> post.getWriterId().equals(writerId))
                                            .collect(toList());
        
        assertThat(unknownPosts).isEmpty();
    }
    
}