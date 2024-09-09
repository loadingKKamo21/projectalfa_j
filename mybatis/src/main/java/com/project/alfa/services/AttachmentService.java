package com.project.alfa.services;

import com.project.alfa.aop.annotation.LockAop;
import com.project.alfa.entities.Attachment;
import com.project.alfa.entities.UploadFile;
import com.project.alfa.error.exception.EntityNotFoundException;
import com.project.alfa.repositories.AttachmentRepository;
import com.project.alfa.repositories.PostRepository;
import com.project.alfa.services.dto.AttachmentResponseDto;
import com.project.alfa.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttachmentService {
    
    private final PostRepository       postRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileUtil             fileUtil;
    
    /**
     * 첨부파일 다중 저장
     *
     * @param postId         - 게시글 FK
     * @param multipartFiles
     */
    @Transactional
    public List<Long> saveAllFiles(final Long postId, final List<MultipartFile> multipartFiles) {
        if (multipartFiles.isEmpty())
            return new ArrayList<>();
        
        if (!validatePostExist(postId))
            throw new EntityNotFoundException("Could not found 'Post' by id: " + postId);
        
        List<UploadFile> uploadFiles = fileUtil.storeFiles(multipartFiles);
        List<Attachment> attachments = uploadFilesToAttachments(postId, uploadFiles);
        
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
        if (!validatePostExist(postId))
            throw new EntityNotFoundException("Could not found 'Post' by id: " + postId);
        
        return attachmentRepository.findAll(postId, false).stream().map(AttachmentResponseDto::new).collect(toList());
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
        List<UploadFile> uploadFiles = new ArrayList<>(attachmentRepository.findAll(ids));
        
        if (!uploadFiles.isEmpty()) {
            fileUtil.deleteFiles(uploadFiles);
            attachmentRepository.deleteAllByIds(ids, postId);
        }
    }
    
    //==================== 변환 메서드 ====================//
    
    /**
     * 업로드 파일 목록 -> 첨부파일 목록 변환
     *
     * @param postId      - 게시글 FK
     * @param uploadFiles - 업로드 파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    private List<Attachment> uploadFilesToAttachments(final Long postId, final List<UploadFile> uploadFiles) {
        List<Attachment> attachments = new ArrayList<>();
        for (UploadFile uploadFile : uploadFiles)
            attachments.add(uploadFileToAttachment(postId, uploadFile));
        return attachments;
    }
    
    /**
     * 업로드 파일 -> 첨부파일 변환
     *
     * @param postId     - 게시글 FK
     * @param uploadFile - 업로드 파일 정보
     * @return 첨부파일 정보
     */
    private Attachment uploadFileToAttachment(final Long postId, final UploadFile uploadFile) {
        return Attachment.builder()
                         .postId(postId)
                         .originalFilename(uploadFile.getOriginalFilename())
                         .storeFilename(uploadFile.getStoreFilename())
                         .storeFilePath(uploadFile.getStoreFilePath())
                         .fileSize(uploadFile.getFileSize())
                         .build();
    }
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 게시글 FK로 게시글 엔티티 존재 검증
     *
     * @param postId - 게시글 FK
     * @return 존재 여부
     */
    private boolean validatePostExist(final Long postId) {
        return postRepository.existsById(postId, false);
    }
    
}
