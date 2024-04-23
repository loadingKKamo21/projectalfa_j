package com.project.alfa.repositories.v2;

import com.project.alfa.entities.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttachmentRepositoryV2 {
    
    private final AttachmentJpaRepository attachmentJpaRepository;
    
    /**
     * 첨부파일 저장
     *
     * @param attachment - 첨부파일 정보
     * @return 첨부파일 정보
     */
    public Attachment save(final Attachment attachment) {
        return attachmentJpaRepository.save(attachment);
    }
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> saveAll(final List<Attachment> attachments) {
        return attachmentJpaRepository.saveAll(attachments);
    }
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> saveAllAndFlush(final List<Attachment> attachments) {
        return attachmentJpaRepository.saveAllAndFlush(attachments);
    }
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보
     */
    public Optional<Attachment> findById(final Long id) {
        return attachmentJpaRepository.findById(id);
    }
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보
     */
    public Optional<Attachment> findById(final Long id, final boolean deleteYn) {
        return attachmentJpaRepository.findByIdAndDeleteYn(id, deleteYn);
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll() {
        return attachmentJpaRepository.findAll();
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final boolean deleteYn) {
        return attachmentJpaRepository.findAllByDeleteYn(deleteYn);
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final List<Long> ids) {
        return attachmentJpaRepository.findAllById(ids);
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final List<Long> ids, final boolean deleteYn) {
        return attachmentJpaRepository.findAllByIdInAndDeleteYn(ids, deleteYn);
    }
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final Long postId) {
        return attachmentJpaRepository.findAllByPost_Id(postId);
    }
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final Long postId, final boolean deleteYn) {
        return attachmentJpaRepository.findAllByPost_IdAndDeleteYn(postId, deleteYn);
    }
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param attachment - 첨부파일 정보
     */
    public void delete(final Attachment attachment) {
        attachmentJpaRepository.delete(attachment);
    }
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param id - PK
     */
    public void deleteById(final Long id) {
        attachmentJpaRepository.deleteById(id);
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param attachments - 첨부파일 정보 목록
     */
    public void deleteAll(final List<Attachment> attachments) {
        attachmentJpaRepository.deleteAll(attachments);
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param attachments - 첨부파일 정보 목록
     */
    public void deleteAllInBatch(final List<Attachment> attachments) {
        attachmentJpaRepository.deleteAllInBatch(attachments);
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllById(final List<Long> ids) {
        attachmentJpaRepository.deleteAllById(ids);
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllByIdInBatch(final List<Long> ids) {
        attachmentJpaRepository.deleteAllByIdInBatch(ids);
    }
    
}
