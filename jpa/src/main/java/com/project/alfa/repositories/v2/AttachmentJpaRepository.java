package com.project.alfa.repositories.v2;

import com.project.alfa.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentJpaRepository extends JpaRepository<Attachment, Long> {
    
    Optional<Attachment> findByIdAndDeleteYn(Long id, boolean deleteYn);
    
    List<Attachment> findAllByDeleteYn(boolean deleteYn);
    
    List<Attachment> findAllByIdInAndDeleteYn(List<Long> ids, boolean deleteYn);
    
    List<Attachment> findAllByPost_Id(Long postId);
    
    List<Attachment> findAllByPost_IdAndDeleteYn(Long postId, boolean deleteYn);
    
}
