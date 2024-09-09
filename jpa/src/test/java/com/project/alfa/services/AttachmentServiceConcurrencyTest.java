package com.project.alfa.services;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Attachment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.UploadFile;
import com.project.alfa.repositories.v1.AttachmentRepositoryV1;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
import com.project.alfa.repositories.v1.PostRepositoryV1;
import com.project.alfa.utils.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
class AttachmentServiceConcurrencyTest {
    
    static final int THREAD_COUNT = 100;
    
    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    AttachmentService          attachmentService;
    @Autowired
    AttachmentRepositoryV1     attachmentRepository;
    //@Autowired
    //AttachmentRepositoryV2 attachmentRepository;
    //@Autowired
    //AttachmentRepositoryV3 attachmentRepository;
    @Autowired
    MemberRepositoryV1         memberRepository;
    //@Autowired
    //MemberRepositoryV2     memberRepository;
    //@Autowired
    //MemberRepositoryV3     memberRepository;
    @Autowired
    PostRepositoryV1           postRepository;
    //@Autowired
    //PostRepositoryV2       postRepository;
    //@Autowired
    //PostRepositoryV3       postRepository;
    @Autowired
    FileUtil                   fileUtil;
    @PersistenceContext
    EntityManager              em;
    @Autowired
    DummyGenerator             dummy;
    @Value("${file.upload.location}")
    String                     fileDir;
    String uploadPath;
    
    @AfterEach
    void clear() {
        em.flush();
        em.clear();
    }
    
    @BeforeEach
    void setup() {
        uploadPath = fileDir + File.separator + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {
                try {
                    attachmentRepository.deleteAll();
                    postRepository.deleteAll();
                    memberRepository.deleteAll();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
                return null;
            });
        });
    }
    
    private List<Attachment> uploadFilesToAttachments(final Post post, final List<UploadFile> uploadFiles) {
        List<Attachment> attachments = new ArrayList<>();
        for (UploadFile uploadFile : uploadFiles) {
            Attachment attachment = Attachment.builder()
                                              .post(post)
                                              .originalFilename(uploadFile.getOriginalFilename())
                                              .storeFilename(uploadFile.getStoreFilename())
                                              .storeFilePath(uploadFile.getStoreFilePath())
                                              .fileSize(uploadFile.getFileSize())
                                              .build();
            attachments.add(attachment);
        }
        return attachments;
    }
    
    private String getStoreFilePath(final String storeFilename) {
        return uploadPath + File.separator + storeFilename;
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 다중 삭제, 멀티 스레드 락 적용")
    void deleteAllFilesByIds_multiThreads() {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT + 1);
        
        int total = dummy.generateRandomNumber(5, 10);
        
        AtomicLong                  postIdRef = new AtomicLong();
        AtomicReference<List<Long>> idsRef    = new AtomicReference<>();
        
        executorService.execute(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                List<Member> writers = dummy.createMembers(1);
                for (Member writer : writers)
                    em.persist(writer);
                List<Post> posts = dummy.createPosts(writers, 1);
                for (Post post : posts)
                    em.persist(post);
                
                Post post = posts.get(0);
                postIdRef.set(post.getId());
                
                List<MultipartFile> multipartFiles = new ArrayList<>();
                for (int i = 1; i <= total; i++) {
                    MockMultipartFile multipartFile = new MockMultipartFile("filename" + i,
                                                                            "originalFilename" + i,
                                                                            "application/octet-stream",
                                                                            UUID.randomUUID().toString()
                                                                                .getBytes());
                    multipartFiles.add(multipartFile);
                }
                List<UploadFile> uploadFiles = fileUtil.storeFiles(multipartFiles);
                List<Attachment> attachments = uploadFilesToAttachments(post, uploadFiles);
                attachments.forEach(attachment -> em.persist(attachment));
                transactionManager.commit(transactionStatus);
                idsRef.set(attachments.stream().map(Attachment::getId).collect(toList()));
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            } finally {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1, TimeUnit.SECONDS);
        
        Long       postId = postIdRef.get();
        List<Long> ids    = idsRef.get();
        
        //When
        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    attachmentService.deleteAllFilesByIds(ids, postId);
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
        List<Attachment> findAttachments = attachmentRepository.findAll().stream()
                                                               .filter(attachment -> ids.contains(
                                                                       attachment.getId()))
                                                               .collect(toList());
        
        for (int i = 0; i < total; i++) {
            Attachment findAttachment = findAttachments.get(i);
            
            String storeFilePath = getStoreFilePath(findAttachment.getStoreFilename());
            
            assertThat(findAttachment.isDeleteYn()).isTrue();
            assertThat(new File(storeFilePath)).doesNotExist();
        }
    }
    
}