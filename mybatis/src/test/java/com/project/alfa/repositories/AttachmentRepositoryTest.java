package com.project.alfa.repositories;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Attachment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.repositories.mybatis.AttachmentMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
class AttachmentRepositoryTest {
    
    @Autowired
    AttachmentRepository attachmentRepository;
    @Autowired
    AttachmentMapper     attachmentMapper;
    @Autowired
    DummyGenerator       dummy;
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 저장")
    void save() {
        //Given
        List<Member> writers    = dummy.createMembers(1, true);
        List<Post>   posts      = dummy.createPosts(writers, 1, true);
        Attachment   attachment = dummy.createAttachments(posts, 1, false).get(0);
        
        //When
        Attachment savedAttachment = attachmentRepository.save(attachment);
        Long       id              = savedAttachment.getId();
        
        //Then
        Attachment findAttachment = attachmentMapper.findById(id);
        
        assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
        assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
        assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
        assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
        assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 다중 저장")
    void saveAll() {
        //Given
        List<Member>     writers     = dummy.createMembers(1, true);
        List<Post>       posts       = dummy.createPosts(writers, 1, true);
        int              total       = dummy.generateRandomNumber(5, 10);
        List<Attachment> attachments = dummy.createAttachments(posts, total, false);
        
        //When
        List<Attachment> savedAttachments = attachmentRepository.saveAll(attachments);
        
        //Then
        List<Attachment> findAttachments = attachmentMapper.findAll();
        
        assertThat(findAttachments.size()).isEqualTo(total);
        for (int i = 0; i < total; i++) {
            Attachment attachment     = savedAttachments.get(i);
            Attachment findAttachment = findAttachments.get(i);
            
            assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
            assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
            assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
            assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
            assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK로 조회")
    void findById() {
        //Given
        List<Member> writers    = dummy.createMembers(1, true);
        List<Post>   posts      = dummy.createPosts(writers, 1, true);
        Attachment   attachment = dummy.createAttachments(posts, 1, true).get(0);
        Long         id         = attachment.getId();
        
        //When
        Attachment findAttachment = attachmentRepository.findById(id).get();
        
        //Then
        assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
        assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
        assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
        assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
        assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
    }
    
    @Test
    @DisplayName("PK로 조회, 존재하지 않는 PK")
    void findById_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        Optional<Attachment> unknownAttachment = attachmentRepository.findById(id);
        
        //Then
        assertThat(unknownAttachment.isPresent()).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK, 삭제 여부로 조회")
    void findByIdAndDeleteYn() {
        //Given
        List<Member> writers    = dummy.createMembers(1, true);
        List<Post>   posts      = dummy.createPosts(writers, 1, true);
        Attachment   attachment = dummy.createAttachments(posts, 1, true).get(0);
        Long         id         = attachment.getId();
        
        //When
        Attachment findAttachment = attachmentRepository.findById(id, false).get();
        
        //Then
        assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
        assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
        assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
        assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
        assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
        assertThat(findAttachment.isDeleteYn()).isFalse();
    }
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 존재하지 않는 PK")
    void findByIdAndDeleteYn_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        Optional<Attachment> unknownAttachment = attachmentRepository.findById(id, false);
        
        //Then
        assertThat(unknownAttachment.isPresent()).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 첨부파일")
    void findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        List<Member> writers    = dummy.createMembers(1, true);
        List<Post>   posts      = dummy.createPosts(writers, 1, true);
        Attachment   attachment = dummy.createAttachments(posts, 1, true).get(0);
        Long         id         = attachment.getId();
        attachmentMapper.deleteById(id, attachment.getPostId());
        
        //When
        Optional<Attachment> deletedAttachment = attachmentRepository.findById(id, false);
        
        //Then
        assertThat(deletedAttachment.isPresent()).isFalse();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 목록 조회")
    void findAll() {
        //Given
        List<Member>     writers     = dummy.createMembers(20, true);
        List<Post>       posts       = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int              total       = dummy.generateRandomNumber(100, 300);
        List<Attachment> attachments = dummy.createAttachments(posts, total, true);
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll().stream().sorted(comparing(Attachment::getId))
                                                               .collect(toList());
        
        //Then
        assertThat(findAttachments.size()).isEqualTo(total);
        for (int i = 0; i < total; i++) {
            Attachment attachment     = attachments.get(i);
            Attachment findAttachment = findAttachments.get(i);
            
            assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
            assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
            assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
            assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
            assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("삭제 여부로 첨부파일 목록 조회")
    void findAllByDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createAttachments(posts, total, true);
        dummy.randomlyDeleteAttachments(attachmentMapper.findAll(), dummy.generateRandomNumber(1, 100));
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(false).stream()
                                                               .sorted(comparing(Attachment::getId))
                                                               .collect(toList());
        
        //Then
        List<Attachment> undeletedAttachments = attachmentMapper.findAll().stream()
                                                                .filter(attachment -> !attachment.isDeleteYn())
                                                                .sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(undeletedAttachments.size());
        for (int i = 0; i < undeletedAttachments.size(); i++) {
            Attachment attachment     = undeletedAttachments.get(i);
            Attachment findAttachment = findAttachments.get(i);
            
            assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
            assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
            assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
            assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
            assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
            assertThat(findAttachment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK 목록으로 첨부파일 목록 조회")
    void findAllByIds() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        List<Long> ids = dummy.createAttachments(posts, total, true).stream().map(Attachment::getId)
                              .collect(toList());
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(ids);
        
        //Then
        List<Attachment> attachments = attachmentMapper.findAll().stream()
                                                       .filter(attachment -> ids.contains(attachment.getId()))
                                                       .sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(attachments.size());
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment     = attachments.get(i);
            Attachment findAttachment = findAttachments.get(i);
            
            assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
            assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
            assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
            assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
            assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK 목록, 삭제 여부로 첨부파일 목록 조회")
    void findAllByIdsAndDeleteYn() {
        //Given
        List<Member>     writers     = dummy.createMembers(20, true);
        List<Post>       posts       = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int              total       = dummy.generateRandomNumber(100, 300);
        List<Attachment> attachments = dummy.createAttachments(posts, total, true);
        dummy.randomlyDeleteAttachments(attachments, dummy.generateRandomNumber(1, 100));
        List<Long> ids = attachments.stream().filter(attachment -> !attachment.isDeleteYn()).map(Attachment::getId)
                                    .collect(toList());
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(ids, false);
        
        //Then
        List<Attachment> undeletedAttachments = attachmentMapper.findAll().stream()
                                                                .filter(attachment ->
                                                                                ids.contains(attachment.getId()) &&
                                                                                !attachment.isDeleteYn())
                                                                .sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(undeletedAttachments.size());
        for (int i = 0; i < undeletedAttachments.size(); i++) {
            Attachment attachment     = undeletedAttachments.get(i);
            Attachment findAttachment = findAttachments.get(i);
            
            assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
            assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
            assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
            assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
            assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
            assertThat(findAttachment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 기준 첨부파일 목록 조회")
    void findAllByPost() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createAttachments(posts, total, true);
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(postId);
        
        //Then
        List<Attachment> attachments = attachmentMapper.findAll().stream()
                                                       .filter(attachment -> attachment.getPostId().equals(postId))
                                                       .sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(attachments.size());
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment     = attachments.get(i);
            Attachment findAttachment = findAttachments.get(i);
            
            assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
            assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
            assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
            assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
            assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 기준, 삭제 여부 첨부파일 목록 조회")
    void findAllByPostAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createAttachments(posts, total, true);
        dummy.randomlyDeleteAttachments(attachmentMapper.findAll(), dummy.generateRandomNumber(1, 100));
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(postId, false);
        
        //Then
        List<Attachment> undeletedAttachments = attachmentMapper.findAll().stream()
                                                                .filter(attachment ->
                                                                                attachment.getPostId().equals(postId) &&
                                                                                !attachment.isDeleteYn())
                                                                .sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(undeletedAttachments.size());
        for (int i = 0; i < undeletedAttachments.size(); i++) {
            Attachment attachment     = undeletedAttachments.get(i);
            Attachment findAttachment = findAttachments.get(i);
            
            assertThat(findAttachment.getPostId()).isEqualTo(attachment.getPostId());
            assertThat(findAttachment.getOriginalFilename()).isEqualTo(attachment.getOriginalFilename());
            assertThat(findAttachment.getStoreFilename()).isEqualTo(attachment.getStoreFilename());
            assertThat(findAttachment.getStoreFilePath()).isEqualTo(attachment.getStoreFilePath());
            assertThat(findAttachment.getFileSize()).isEqualTo(attachment.getFileSize());
            assertThat(findAttachment.isDeleteYn()).isFalse();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 삭제")
    void deleteById() {
        //Given
        List<Member> writers    = dummy.createMembers(1, true);
        List<Post>   posts      = dummy.createPosts(writers, 1, true);
        Attachment   attachment = dummy.createAttachments(posts, 1, true).get(0);
        Long         id         = attachment.getId();
        Long         postId     = attachment.getPostId();
        
        //When
        attachmentRepository.deleteById(id, postId);
        
        //Then
        Attachment deletedAttachment = attachmentMapper.findById(id);
        
        assertThat(deletedAttachment.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 정보 영구 삭제")
    void permanentlyDeleteById() {
        //Given
        List<Member> writers    = dummy.createMembers(1, true);
        List<Post>   posts      = dummy.createPosts(writers, 1, true);
        Attachment   attachment = dummy.createAttachments(posts, 1, true).get(0);
        Long         id         = attachment.getId();
        Long         postId     = attachment.getPostId();
        attachmentMapper.deleteById(id, postId);
        
        //When
        attachmentRepository.permanentlyDeleteById(id);
        
        //Then
        Attachment unknownAttachment = attachmentMapper.findById(id);
        
        assertThat(unknownAttachment).isNull();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 목록 삭제")
    void deleteAllByIds() {
        //Given
        List<Member>     writers     = dummy.createMembers(20, true);
        List<Post>       posts       = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        List<Attachment> attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(100, 300), true);
        Long             postId      = posts.get(new Random().nextInt(posts.size())).getId();
        List<Long> ids = attachments.stream().filter(attachment -> attachment.getPostId().equals(postId))
                                    .map(Attachment::getId).collect(toList());
        
        //When
        attachmentRepository.deleteAllByIds(ids, postId);
        
        //Then
        List<Attachment> deletedAttachments = attachmentMapper.findAll().stream()
                                                              .filter(attachment -> attachment.getPostId()
                                                                                              .equals(postId))
                                                              .collect(toList());
        
        for (Attachment attachment : deletedAttachments)
            assertThat(attachment.isDeleteYn()).isTrue();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 정보 목록 영구 삭제")
    void permanentlyDeleteAllByIds() {
        //Given
        List<Member>     writers     = dummy.createMembers(20, true);
        List<Post>       posts       = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        List<Attachment> attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(100, 300), true);
        Long             postId      = posts.get(new Random().nextInt(posts.size())).getId();
        List<Long> ids = attachments.stream().filter(attachment -> attachment.getPostId().equals(postId))
                                    .map(Attachment::getId).collect(toList());
        attachmentMapper.deleteAllByIds(ids, postId);
        
        //When
        attachmentRepository.permanentlyDeleteAllByIds(ids);
        
        //Then
        List<Attachment> unknownAttachments = attachmentMapper.findAll().stream()
                                                              .filter(attachment -> attachment.getPostId()
                                                                                              .equals(postId))
                                                              .collect(toList());
        
        assertThat(unknownAttachments).isEmpty();
    }
    
}
