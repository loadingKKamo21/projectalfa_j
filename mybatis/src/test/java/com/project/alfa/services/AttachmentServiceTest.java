package com.project.alfa.services;

import com.project.alfa.config.DummyGenerator;
import com.project.alfa.config.TestConfig;
import com.project.alfa.entities.Attachment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.UploadFile;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.repositories.mybatis.AttachmentMapper;
import com.project.alfa.services.dto.AttachmentResponseDto;
import com.project.alfa.utils.FileUtil;
import lombok.SneakyThrows;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestConfig.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttachmentServiceTest {
    
    @Autowired
    AttachmentService attachmentService;
    @Autowired
    AttachmentMapper  attachmentMapper;
    @Autowired
    FileUtil          fileUtil;
    @Autowired
    DummyGenerator    dummy;
    @Value("${file.upload.location}")
    String            fileDir;
    String uploadPath;
    
    @BeforeEach
    void setup() {
        uploadPath = fileDir + File.separator + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    private List<Attachment> uploadFilesToAttachmentsAndSave(final Long postId, final List<UploadFile> uploadFiles) {
        List<Attachment> attachments = new ArrayList<>();
        for (UploadFile uploadFile : uploadFiles) {
            Attachment attachment = Attachment.builder()
                                              .postId(postId)
                                              .originalFilename(uploadFile.getOriginalFilename())
                                              .storeFilename(uploadFile.getStoreFilename())
                                              .storeFilePath(uploadFile.getStoreFilePath())
                                              .fileSize(uploadFile.getFileSize())
                                              .build();
            attachmentMapper.save(attachment);
            attachments.add(attachment);
            
        }
        return attachments;
    }
    
    private String getStoreFilePath(final String storeFilename) {
        return uploadPath + File.separator + storeFilename;
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 다중 저장")
    void saveAll() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        int          total   = dummy.generateRandomNumber(5, 10);
        Long         postId  = post.getId();
        
        List<MultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            MockMultipartFile multipartFile = new MockMultipartFile("filename" + i,
                                                                    "originalFilename" + i,
                                                                    "application/octet-stream",
                                                                    UUID.randomUUID().toString().getBytes());
            multipartFiles.add(multipartFile);
        }
        
        //When
        List<Long> ids = attachmentService.saveAllFiles(postId, multipartFiles);
        
        //Then
        List<Attachment> findAttachments = attachmentMapper.findAll().stream()
                                                           .filter(attachment -> ids.contains(attachment.getId()))
                                                           .collect(toList());
        
        assertThat(ids.size()).isEqualTo(total);
        for (int i = 0; i < total; i++) {
            Attachment    findAttachment = findAttachments.get(i);
            MultipartFile multipartFile  = multipartFiles.get(i);
            
            String storeFilePath = getStoreFilePath(findAttachment.getStoreFilename());
            
            assertThat(findAttachment).isNotNull();
            assertThat(findAttachment.getOriginalFilename()).isEqualTo(multipartFile.getOriginalFilename());
            assertThat(findAttachment.getFileSize()).isEqualTo(multipartFile.getSize());
            assertThat(new File(storeFilePath)).exists();
        }
    }
    
    @Test
    @DisplayName("첨부파일 다중 저장, 존재하지 않는 게시글")
    void saveAll_unknownPost() {
        //Given
        int  total  = dummy.generateRandomNumber(5, 10);
        Long postId = new Random().nextLong();
        
        List<MultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            MockMultipartFile multipartFile = new MockMultipartFile("filename" + i,
                                                                    "originalFilename" + i,
                                                                    "application/octet-stream",
                                                                    UUID.randomUUID().toString().getBytes());
            multipartFiles.add(multipartFile);
        }
        
        //When
        
        //Then
        assertThatThrownBy(() -> attachmentService.saveAllFiles(postId, multipartFiles))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Post' by id: " + postId);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("PK로 첨부파일 상세 정보 조회")
    void findFileById() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        List<Post>   posts   = dummy.createPosts(writers, 1, true);
        Long         id      = dummy.createAttachments(posts, 1, true).get(0).getId();
        
        //When
        AttachmentResponseDto dto = attachmentService.findFileById(id);
        
        //Then
        Attachment findAttachment = attachmentMapper.findById(id);
        
        assertThat(findAttachment.getId()).isEqualTo(dto.getId());
        assertThat(findAttachment.getPostId()).isEqualTo(dto.getPostId());
        assertThat(findAttachment.getOriginalFilename()).isEqualTo(dto.getOriginalFilename());
        assertThat(findAttachment.getStoreFilename()).isEqualTo(dto.getStoreFilename());
        assertThat(findAttachment.getFileSize()).isEqualTo(dto.getFileSize());
        assertThat(findAttachment.getCreatedDate()).isEqualTo(dto.getCreatedDate());
        assertThat(findAttachment.getLastModifiedDate()).isEqualTo(dto.getLastModifiedDate());
    }
    
    @Test
    @DisplayName("PK로 첨부파일 상세 정보 조회, 존재하지 않는 PK")
    void findFileById_unknown() {
        //Given
        Long id = new Random().nextLong();
        
        //When
        
        //Then
        assertThatThrownBy(() -> attachmentService.findFileById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND)
                .hasMessage("Could not found 'Attachment' by id: " + id);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("게시글 기준 첨부파일 정보 목록 조회")
    void findAllFilesByPost() {
        //Given
        List<Member> writers = dummy.createMembers(20, true);
        List<Post>   posts   = dummy.createPosts(writers, dummy.generateRandomNumber(50, 100), true);
        int          total   = dummy.generateRandomNumber(100, 300);
        dummy.createAttachments(posts, total, true);
        Long postId = posts.get(new Random().nextInt(posts.size())).getId();
        
        //When
        List<AttachmentResponseDto> findAttachments = attachmentService.findAllFilesByPost(postId);
        
        //Then
        List<AttachmentResponseDto> attachments = attachmentMapper.findAll().stream()
                                                                  .filter(attachment -> attachment.getPostId()
                                                                                                  .equals(postId))
                                                                  .map(AttachmentResponseDto::new).collect(toList());
        
        assertThat(findAttachments.size()).isEqualTo(attachments.size());
        for (int i = 0; i < attachments.size(); i++) {
            AttachmentResponseDto attachmentDto     = attachments.get(i);
            AttachmentResponseDto findAttachmentDto = findAttachments.get(i);
            
            assertThat(findAttachmentDto.getId()).isEqualTo(attachmentDto.getId());
            assertThat(findAttachmentDto.getPostId()).isEqualTo(attachmentDto.getPostId());
            assertThat(findAttachmentDto.getOriginalFilename()).isEqualTo(attachmentDto.getOriginalFilename());
            assertThat(findAttachmentDto.getStoreFilename()).isEqualTo(attachmentDto.getStoreFilename());
            assertThat(findAttachmentDto.getFileSize()).isEqualTo(attachmentDto.getFileSize());
            assertThat(findAttachmentDto.getCreatedDate()).isEqualTo(attachmentDto.getCreatedDate());
            assertThat(findAttachmentDto.getLastModifiedDate()).isEqualTo(attachmentDto.getLastModifiedDate());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    @Test
    @DisplayName("첨부파일 다중 삭제")
    void deleteAllFilesByIds() {
        //Given
        List<Member> writers = dummy.createMembers(1, true);
        Post         post    = dummy.createPosts(writers, 1, true).get(0);
        int          total   = dummy.generateRandomNumber(5, 10);
        Long         postId  = post.getId();
        
        List<MultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            MockMultipartFile multipartFile = new MockMultipartFile("filename" + i,
                                                                    "originalFilename" + i,
                                                                    "application/octet-stream",
                                                                    UUID.randomUUID().toString().getBytes());
            multipartFiles.add(multipartFile);
        }
        List<UploadFile> uploadFiles = fileUtil.storeFiles(multipartFiles);
        List<Attachment> attachments = uploadFilesToAttachmentsAndSave(postId, uploadFiles);
        List<Long>       ids         = attachments.stream().map(Attachment::getId).collect(toList());
        
        //When
        attachmentService.deleteAllFilesByIds(ids, postId);
        
        //Then
        List<Attachment> findAttachments = attachmentMapper.findAllByIds(ids);
        for (int i = 0; i < total; i++) {
            Attachment findAttachment = findAttachments.get(i);
            
            String storeFilePath = getStoreFilePath(findAttachment.getStoreFilename());
            
            assertThat(findAttachment.isDeleteYn()).isTrue();
            assertThat(new File(storeFilePath)).doesNotExist();
        }
    }
    
}