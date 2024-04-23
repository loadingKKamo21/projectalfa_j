package com.project.alfa.repositories.v3;

import com.project.alfa.entities.Attachment;
import com.project.alfa.repositories.v3.querydsl.AttachmentRepositoryV3Custom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepositoryV3 extends JpaRepository<Attachment, Long>, AttachmentRepositoryV3Custom {
    
    Optional<Attachment> findById(Long id);
    
    @Query("SELECT a FROM Attachment a WHERE a.id = :id AND a.deleteYn = :deleteYn")
    Optional<Attachment> findById(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT a FROM Attachment a WHERE a.deleteYn = :deleteYn")
    List<Attachment> findAll(@Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT a FROM Attachment a WHERE a.id IN :ids")
    List<Attachment> findAll(@Param("ids") List<Long> ids);
    
    @Query("SELECT a FROM Attachment a WHERE a.id IN :ids AND a.deleteYn = :deleteYn")
    List<Attachment> findAll(@Param("ids") List<Long> ids, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT a FROM Attachment a WHERE a.post.id = :postId")
    List<Attachment> findAll(@Param("postId") Long postId);
    
    @Query("SELECT a FROM Attachment a WHERE a.post.id = :postId AND a.deleteYn = :deleteYn")
    List<Attachment> findAll(@Param("postId") Long postId, @Param("deleteYn") boolean deleteYn);
    
}
