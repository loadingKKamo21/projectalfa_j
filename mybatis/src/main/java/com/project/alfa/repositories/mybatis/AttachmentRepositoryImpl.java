package com.project.alfa.repositories.mybatis;

import com.project.alfa.entities.Attachment;
import com.project.alfa.repositories.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttachmentRepositoryImpl implements AttachmentRepository {
    
    private final AttachmentMapper attachmentMapper;
    
    /**
     * 첨부파일 저장
     *
     * @param attachment - 첨부파일 정보
     * @return 첨부파일 정보
     */
    @Override
    public Attachment save(Attachment attachment) {
        attachmentMapper.save(attachment);
        return attachment;
    }
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    @Override
    public List<Attachment> saveAll(List<Attachment> attachments) {
        attachmentMapper.saveAll(attachments);
//        for (Attachment attachment : attachments)
//            attachmentMapper.save(attachment);
        return attachments;
    }
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보
     */
    @Override
    public Optional<Attachment> findById(Long id) {
        return Optional.ofNullable(attachmentMapper.findById(id));
    }
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보
     */
    @Override
    public Optional<Attachment> findById(Long id, boolean deleteYn) {
        return Optional.ofNullable(attachmentMapper.findByIdAndDeleteYn(id, deleteYn));
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @return 첨부파일 정보 목록
     */
    @Override
    public List<Attachment> findAll() {
        return attachmentMapper.findAll();
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    @Override
    public List<Attachment> findAll(boolean deleteYn) {
        return attachmentMapper.findAllByDeleteYn(deleteYn);
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 첨부파일 정보 목록
     */
    @Override
    public List<Attachment> findAll(List<Long> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return attachmentMapper.findAllByIds(ids);
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    @Override
    public List<Attachment> findAll(List<Long> ids, boolean deleteYn) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return attachmentMapper.findAllByIdsAndDeleteYn(ids, deleteYn);
    }
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부파일 정보 목록
     */
    @Override
    public List<Attachment> findAll(Long postId) {
        return attachmentMapper.findAllByPost(postId);
    }
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    @Override
    public List<Attachment> findAll(Long postId, boolean deleteYn) {
        return attachmentMapper.findAllByPostAndDeleteYn(postId, deleteYn);
    }
    
    /**
     * 첨부파일 삭제
     *
     * @param id     - PK
     * @param postId - 게시글 FK
     */
    @Override
    public void deleteById(Long id, Long postId) {
        attachmentMapper.deleteById(id, postId);
    }
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param id - PK
     */
    @Override
    public void permanentlyDeleteById(Long id) {
        attachmentMapper.permanentlyDeleteById(id);
    }
    
    /**
     * 첨부파일 목록 삭제
     *
     * @param ids    - PK 목록
     * @param postId - 게시글 FK
     */
    @Override
    public void deleteAllByIds(List<Long> ids, Long postId) {
        if (ids.isEmpty())
            return;
        attachmentMapper.deleteAllByIds(ids, postId);
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    @Override
    public void permanentlyDeleteAllByIds(List<Long> ids) {
        if (ids.isEmpty())
            return;
        attachmentMapper.permanentlyDeleteAllByIds(ids);
    }
    
    /**
     * 모든 첨부파일 정보 영구 삭제
     */
    @Override
    public void deleteAll() {
        attachmentMapper.deleteAll();
    }
    
}
