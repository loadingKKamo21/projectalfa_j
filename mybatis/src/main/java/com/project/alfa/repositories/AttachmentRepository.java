package com.project.alfa.repositories;

import com.project.alfa.entities.Attachment;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository {
    
    Attachment save(Attachment attachment);
    
    List<Attachment> saveAll(List<Attachment> attachments);
    
    Optional<Attachment> findById(Long id);
    
    Optional<Attachment> findById(Long id, boolean deleteYn);
    
    List<Attachment> findAll();
    
    List<Attachment> findAll(boolean deleteYn);
    
    List<Attachment> findAll(List<Long> ids);
    
    List<Attachment> findAll(List<Long> ids, boolean deleteYn);
    
    List<Attachment> findAll(Long postId);
    
    List<Attachment> findAll(Long postId, boolean deleteYn);
    
    void deleteById(Long id, Long postId);
    
    void permanentlyDeleteById(Long id);
    
    void deleteAllByIds(List<Long> ids, Long postId);
    
    void permanentlyDeleteAllByIds(List<Long> ids);
    
}
