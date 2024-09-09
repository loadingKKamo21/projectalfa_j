package com.project.alfa.services;

import com.project.alfa.aop.annotation.LockAop;
import com.project.alfa.entities.Attachment;
import com.project.alfa.entities.Post;
import com.project.alfa.entities.UploadFile;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.error.exception.ErrorCode;
import com.project.alfa.error.exception.InvalidValueException;
import com.project.alfa.repositories.v1.AttachmentRepositoryV1;
import com.project.alfa.repositories.v1.PostRepositoryV1;
import com.project.alfa.services.dto.AttachmentResponseDto;
import com.project.alfa.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttachmentService {
    
    private final AttachmentRepositoryV1 attachmentRepository;
    //private final AttachmentRepositoryV2 attachmentRepository;
    //private final AttachmentRepositoryV3 attachmentRepository;
    private final PostRepositoryV1       postRepository;
    //private final PostRepositoryV2       postRepository;
    //private final PostRepositoryV3       postRepository;
    private final FileUtil               fileUtil;
    
    /**
     * 첨부파일 다중 저장
     *
     * @param postId         - 게시글 FK
     * @param multipartFiles
     */
    @Transactional
    public List<Long> saveAllFiles(final Long postId, final List<MultipartFile> multipartFiles) {
        if (multipartFiles.isEmpty())
            return Collections.emptyList();
        
        Post post = postRepository.findById(postId, false)
                                  .orElseThrow(
                                          () -> new EntityNotFoundException("Could not found 'Post' by id: " + postId));
        
        List<UploadFile> uploadFiles = fileUtil.storeFiles(multipartFiles);
        List<Attachment> attachments = uploadFilesToAttachments(post, uploadFiles);
        
        return attachmentRepository.saveAll(attachments).stream().map(Attachment::getId).collect(toList());
    }
    
    /**
     * PK로 첨부파일 상세 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보 DTO
     */
    public AttachmentResponseDto findFileById(final Long id) {
        return new AttachmentResponseDto(attachmentRepository.findById(id, false).orElseThrow(
                () -> new EntityNotFoundException("Could not found 'Attachment' by id: " + id)));
    }
    
    /**
     * 게시글 기준 첨부파일 정보 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부 파일 목록
     */
    public List<AttachmentResponseDto> findAllFilesByPost(final Long postId) {
        Post post = postRepository.findById(postId, false)
                                  .orElseThrow(
                                          () -> new EntityNotFoundException("Could not found 'Post' by id: " + postId));
        return attachmentRepository.findAll(post.getId(), false).stream().map(AttachmentResponseDto::new).collect(toList());
    }
    
    /**
     * 첨부파일 다중 삭제
     *
     * @param ids    - PK 목록
     * @param postId - 게시글 FK
     */
    @LockAop
    @Transactional
    public void deleteAllFilesByIds(final List<Long> ids, final Long postId) {
        Post post = postRepository.findById(postId, false)
                                  .orElseThrow(
                                          () -> new EntityNotFoundException("Could not found 'Post' by id: " + postId));
        
        if (!post.getAttachments().stream().allMatch(attachment -> ids.contains(attachment.getId())))
            throw new InvalidValueException("Not the file attached on this post.", ErrorCode.NOT_ATTACHMENT_ON_POST);
        
        List<Attachment> attachments = attachmentRepository.findAll(ids);
        if (!attachments.isEmpty()) {
            fileUtil.deleteFiles(new ArrayList<UploadFile>(attachments));
            attachments.forEach(attachment -> attachment.isDelete(true));
        }
    }
    
    //==================== 변환 메서드 ====================//
    
    /**
     * 업로드 파일 목록 -> 첨부파일 목록 변환
     *
     * @param post        - 게시글
     * @param uploadFiles - 업로드 파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    private List<Attachment> uploadFilesToAttachments(final Post post, final List<UploadFile> uploadFiles) {
        List<Attachment> attachments = new ArrayList<>();
        for (UploadFile uploadFile : uploadFiles)
            attachments.add(uploadFileToAttachment(post, uploadFile));
        return attachments;
    }
    
    /**
     * 업로드 파일 -> 첨부파일 변환
     *
     * @param post       - 게시글
     * @param uploadFile - 업로드 파일 정보
     * @return 첨부파일 정보
     */
    private Attachment uploadFileToAttachment(final Post post, final UploadFile uploadFile) {
        return Attachment.builder()
                         .post(post)
                         .originalFilename(uploadFile.getOriginalFilename())
                         .storeFilename(uploadFile.getStoreFilename())
                         .storeFilePath(uploadFile.getStoreFilePath())
                         .fileSize(uploadFile.getFileSize())
                         .build();
    }
    
}
