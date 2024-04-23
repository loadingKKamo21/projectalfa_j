package com.project.alfa.repositories.v1;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Attachment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
class AttachmentRepositoryV1Test {
    
    @Autowired
    AttachmentRepositoryV1 attachmentRepository;
    @PersistenceContext
    EntityManager          em;
    @Autowired
    DummyGenerator         dummy;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    private void randomlyDeleteAttachments(final List<Attachment> attachments, final int count) {
        Random random      = new Random();
        int    deleteCount = 0;
        while (count > 0) {
            if (count == deleteCount)
                break;
            Attachment attachment = attachments.get(random.nextInt(attachments.size()));
            if (attachment.isDeleteYn())
                continue;
            attachment.isDelete(true);
            deleteCount++;
        }
    }
    
    @Test
    @DisplayName("첨부파일 저장")
    void save() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Attachment attachment = dummy.createAttachments(posts, 1).get(0);
        
        //When
        Long id = attachmentRepository.save(attachment).getId();
        
        //Then
        Attachment findAttachment = em.find(Attachment.class, id);
        
        assertThat(findAttachment).isEqualTo(attachment);
    }
    
    @Test
    @DisplayName("첨부파일 다중 저장")
    void saveAll() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        int              total       = dummy.generateRandomNumber(5, 10);
        List<Attachment> attachments = dummy.createAttachments(posts, total);
        
        //When
        List<Attachment> savedAttachments = attachmentRepository.saveAll(attachments);
        
        //Then
        attachments = attachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        savedAttachments = savedAttachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(savedAttachments.size()).isEqualTo(total);
        for (int i = 0; i < total; i++)
            assertThat(savedAttachments.get(i)).isEqualTo(attachments.get(i));
    }
    
    @Test
    @DisplayName("첨부파일 다중 저장")
    void saveAllAndFlush() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        int              total       = dummy.generateRandomNumber(5, 10);
        List<Attachment> attachments = dummy.createAttachments(posts, total);
        
        //When
        List<Attachment> savedAttachments = attachmentRepository.saveAllAndFlush(attachments);
        
        //Then
        attachments = attachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        savedAttachments = savedAttachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(savedAttachments.size()).isEqualTo(total);
        for (int i = 0; i < total; i++)
            assertThat(savedAttachments.get(i)).isEqualTo(attachments.get(i));
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
        Attachment attachment = dummy.createAttachments(posts, 1).get(0);
        em.persist(attachment);
        Long id = attachment.getId();
        
        //When
        Attachment findAttachment = attachmentRepository.findById(id).get();
        
        //Then
        assertThat(findAttachment).isEqualTo(attachment);
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
        Attachment attachment = dummy.createAttachments(posts, 1).get(0);
        em.persist(attachment);
        Long id = attachment.getId();
        
        //When
        Attachment findAttachment = attachmentRepository.findById(id, false).get();
        
        //Then
        assertThat(findAttachment).isEqualTo(attachment);
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
    
    @Test
    @DisplayName("PK, 삭제 여부로 조회, 이미 삭제한 첨부파일")
    void findByIdAndDeleteYn_alreadyDeleted() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Attachment attachment = dummy.createAttachments(posts, 1).get(0);
        em.persist(attachment);
        Long id = attachment.getId();
        attachment.isDelete(true);
        
        //When
        Optional<Attachment> deletedAttachment = attachmentRepository.findById(id, false);
        
        //Then
        assertThat(deletedAttachment.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("첨부파일 목록조회")
    void findAll() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int              total       = dummy.generateRandomNumber(100, 300);
        List<Attachment> attachments = dummy.createAttachments(posts, total);
        for (Attachment attachment : attachments)
            em.persist(attachment);
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll();
        
        //Then
        attachments = attachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        findAttachments = findAttachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(total);
        for (int i = 0; i < total; i++)
            assertThat(findAttachments.get(i)).isEqualTo(attachments.get(i));
    }
    
    @Test
    @DisplayName("삭제 여부로 첨부파일 목록조회")
    void findAllByDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int              total       = dummy.generateRandomNumber(100, 300);
        List<Attachment> attachments = dummy.createAttachments(posts, total);
        for (Attachment attachment : attachments)
            em.persist(attachment);
        randomlyDeleteAttachments(attachments, dummy.generateRandomNumber(1, 100));
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(false);
        
        //Then
        attachments = attachments.stream().filter(attachment -> !attachment.isDeleteYn())
                                 .sorted(comparing(Attachment::getId)).collect(toList());
        findAttachments = findAttachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(attachments.size());
        for (int i = 0; i < attachments.size(); i++)
            assertThat(findAttachments.get(i)).isEqualTo(attachments.get(i));
    }
    
    @Test
    @DisplayName("PK 목록으로 첨부파일 목록조회")
    void findAllByIds() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int              total       = dummy.generateRandomNumber(100, 300);
        List<Attachment> attachments = dummy.createAttachments(posts, total);
        for (Attachment attachment : attachments)
            em.persist(attachment);
        List<Long> ids = attachments.stream().map(Attachment::getId).collect(toList());
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(ids);
        
        //Then
        attachments = attachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        findAttachments = findAttachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(ids.size());
        for (int i = 0; i < ids.size(); i++)
            assertThat(findAttachments.get(i)).isEqualTo(attachments.get(i));
    }
    
    @Test
    @DisplayName("PK 목록, 삭제 여부로 첨부파일 목록조회")
    void findAllByIdsAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int              total       = dummy.generateRandomNumber(100, 300);
        List<Attachment> attachments = dummy.createAttachments(posts, total);
        for (Attachment attachment : attachments)
            em.persist(attachment);
        randomlyDeleteAttachments(attachments, dummy.generateRandomNumber(1, 100));
        List<Long> ids = attachments.stream().filter(attachment -> !attachment.isDeleteYn()).map(Attachment::getId)
                                    .collect(toList());
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(ids, false);
        
        //Then
        attachments = attachments.stream()
                                 .filter(attachment -> ids.contains(attachment.getId()) && !attachment.isDeleteYn())
                                 .sorted(comparing(Attachment::getId)).collect(toList());
        findAttachments = findAttachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(ids.size());
        for (int i = 0; i < ids.size(); i++)
            assertThat(findAttachments.get(i)).isEqualTo(attachments.get(i));
    }
    
    @Test
    @DisplayName("게시글 기준 첨부파일 목록조회")
    void findAllByPost() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int              total       = dummy.generateRandomNumber(100, 300);
        List<Attachment> attachments = dummy.createAttachments(posts, total);
        for (Attachment attachment : attachments)
            em.persist(attachment);
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(postId);
        
        //Then
        attachments = attachments.stream().filter(attachment -> attachment.getPost().getId().equals(postId))
                                 .sorted(comparing(Attachment::getId)).collect(toList());
        findAttachments = findAttachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(attachments.size());
        for (int i = 0; i < attachments.size(); i++)
            assertThat(findAttachments.get(i)).isEqualTo(attachments.get(i));
    }
    
    @Test
    @DisplayName("게시글 기준, 삭제 여부로 첨부파일 목록조회")
    void findAllByPostAndDeleteYn() {
        //Given
        List<Member> writers = dummy.createMembers(20);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100));
        for (Post post : posts)
            em.persist(post);
        int              total       = dummy.generateRandomNumber(100, 300);
        List<Attachment> attachments = dummy.createAttachments(posts, total);
        for (Attachment attachment : attachments)
            em.persist(attachment);
        randomlyDeleteAttachments(attachments, dummy.generateRandomNumber(1, 100));
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<Attachment> findAttachments = attachmentRepository.findAll(postId, false);
        
        //Then
        attachments = attachments.stream()
                                 .filter(attachment -> attachment.getPost().getId().equals(postId) &&
                                                       !attachment.isDeleteYn())
                                 .sorted(comparing(Attachment::getId)).collect(toList());
        findAttachments = findAttachments.stream().sorted(comparing(Attachment::getId)).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(attachments.size());
        for (int i = 0; i < attachments.size(); i++)
            assertThat(findAttachments.get(i)).isEqualTo(attachments.get(i));
    }
    
    @Test
    @DisplayName("엔티티로 첨부파일 정보 영구 삭제")
    void delete() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Attachment attachment = dummy.createAttachments(posts, 1).get(0);
        em.persist(attachment);
        Long id = attachment.getId();
        
        //When
        attachmentRepository.delete(attachment);
        
        //Then
        Attachment deletedAttachment = em.find(Attachment.class, id);
        
        assertThat(deletedAttachment).isNull();
    }
    
    @Test
    @DisplayName("PK로 첨부파일 정보 영구 삭제")
    void deleteById() {
        //Given
        List<Member> writers = dummy.createMembers(1);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, 1);
        for (Post post : posts)
            em.persist(post);
        Attachment attachment = dummy.createAttachments(posts, 1).get(0);
        em.persist(attachment);
        Long id = attachment.getId();
        
        //When
        attachmentRepository.deleteById(id);
        
        //Then
        Attachment deletedAttachment = em.find(Attachment.class, id);
        
        assertThat(deletedAttachment).isNull();
    }
    
    @Test
    @DisplayName("엔티티 목록으로 첨부파일 정보 목록 영구 삭제")
    void deleteAll() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Attachment> attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(50, 100));
        for (Attachment attachment : attachments)
            em.persist(attachment);
        
        //When
        attachmentRepository.deleteAll(attachments);
        
        //Then
        List<Long> ids = attachments.stream().map(Attachment::getId).collect(toList());
        
        for (Long id : ids)
            assertThat(em.find(Attachment.class, id)).isNull();
    }
    
    @Test
    @DisplayName("엔티티 목록으로 첨부파일 정보 목록 영구 삭제")
    void deleteAllInBatch() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Attachment> attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(50, 100));
        for (Attachment attachment : attachments)
            em.persist(attachment);
        
        //When
        attachmentRepository.deleteAllInBatch(attachments);
        clear();
        
        //Then
        List<Long> ids = attachments.stream().map(Attachment::getId).collect(toList());
        
        for (Long id : ids)
            assertThat(em.find(Attachment.class, id)).isNull();
    }
    
    @Test
    @DisplayName("PK 목록으로 첨부파일 정보 목록 영구 삭제")
    void deleteAllById() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Attachment> attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(50, 100));
        for (Attachment attachment : attachments)
            em.persist(attachment);
        List<Long> ids = attachments.stream().map(Attachment::getId).collect(toList());
        
        //When
        attachmentRepository.deleteAllById(ids);
        
        //Then
        for (Long id : ids)
            assertThat(em.find(Attachment.class, id)).isNull();
    }
    
    @Test
    @DisplayName("PK 목록으로 첨부파일 정보 목록 영구 삭제")
    void deleteAllByIdInBatch() {
        //Given
        List<Member> writers = dummy.createMembers(10);
        for (Member writer : writers)
            em.persist(writer);
        List<Post> posts = dummy.createPosts(writers, dummy.generateRandomNumber(10, 50));
        for (Post post : posts)
            em.persist(post);
        List<Attachment> attachments = dummy.createAttachments(posts, dummy.generateRandomNumber(50, 100));
        for (Attachment attachment : attachments)
            em.persist(attachment);
        List<Long> ids = attachments.stream().map(Attachment::getId).collect(toList());
        
        //When
        attachmentRepository.deleteAllByIdInBatch(ids);
        clear();
        
        //Then
        for (Long id : ids)
            assertThat(em.find(Attachment.class, id)).isNull();
    }
    
}