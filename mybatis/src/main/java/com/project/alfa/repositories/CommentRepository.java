package com.project.alfa.repositories;

import com.project.alfa.entities.Comment;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    
    Comment save(Comment comment);
    
    Optional<Comment> findById(Long id);
    
    Optional<Comment> findById(Long id, boolean deleteYn);
    
    List<Comment> findAll();
    
    List<Comment> findAll(boolean deleteYn);
    
    List<Comment> findAll(List<Long> ids);
    
    List<Comment> findAll(List<Long> ids, boolean deleteYn);
    
    List<Comment> findAllByWriter(Long writerId);
    
    List<Comment> findAllByWriter(Long writerId, boolean deleteYn);
    
    List<Comment> findAllByPost(Long postId);
    
    List<Comment> findAllByPost(Long postId, boolean deleteYn);
    
    List<Comment> findAllByWriter(Long writerId, Pageable pageable);
    
    List<Comment> findAllByWriter(Long writerId, boolean deleteYn, Pageable pageable);
    
    List<Comment> findAllByPost(Long postId, Pageable pageable);
    
    List<Comment> findAllByPost(Long postId, boolean deleteYn, Pageable pageable);
    
    void update(Comment param);
    
    void deleteById(Long id, Long writerId);
    
    void permanentlyDeleteById(Long id);
    
    void deleteAllByIds(List<Long> ids, Long writerId);
    
    void permanentlyDeleteAllByIds(List<Long> ids);
    
}
