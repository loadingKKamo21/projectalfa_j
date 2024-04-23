package com.project.alfa.repositories.v2;

import com.project.alfa.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {
    
    Optional<Comment> findByIdAndDeleteYn(Long id, boolean deleteYn);
    
    List<Comment> findAllByDeleteYn(boolean deleteYn);
    
    List<Comment> findAllByIdIn(List<Long> ids);
    
    List<Comment> findAllByIdInAndDeleteYn(List<Long> ids, boolean deleteYn);
    
    List<Comment> findAllByWriter_Id(Long writerId);
    
    List<Comment> findAllByWriter_IdAndDeleteYn(Long writerId, boolean deleteYn);
    
    List<Comment> findAllByPost_Id(Long postId);
    
    List<Comment> findAllByPost_IdAndDeleteYn(Long postId, boolean deleteYn);
    
    Page<Comment> findAllByWriter_Id(Long writerId, Pageable pageable);
    
    Page<Comment> findAllByWriter_IdOrderByCreatedDateDesc(Long writerId, Pageable pageable);
    
    Page<Comment> findAllByWriter_IdAndDeleteYn(Long writerId, boolean deleteYn, Pageable pageable);
    
    Page<Comment> findAllByWriter_IdAndDeleteYnOrderByCreatedDateDesc(Long writerId,
                                                                      boolean deleteYn,
                                                                      Pageable pageable);
    
    Page<Comment> findAllByPost_Id(Long postId, Pageable pageable);
    
    Page<Comment> findAllByPost_IdOrderByCreatedDateDesc(Long postId, Pageable pageable);
    
    Page<Comment> findAllByPost_IdAndDeleteYn(Long postId, boolean deleteYn, Pageable pageable);
    
    Page<Comment> findAllByPost_IdAndDeleteYnOrderByCreatedDateDesc(Long postId, boolean deleteYn, Pageable pageable);
    
}
