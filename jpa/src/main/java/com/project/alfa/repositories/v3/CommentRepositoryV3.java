package com.project.alfa.repositories.v3;

import com.project.alfa.entities.Comment;
import com.project.alfa.repositories.v3.querydsl.CommentRepositoryV3Custom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepositoryV3 extends JpaRepository<Comment, Long>, CommentRepositoryV3Custom {
    
    Optional<Comment> findById(Long id);
    
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.deleteYn = :deleteYn")
    Optional<Comment> findById(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT c from Comment c WHERE c.deleteYn = :deleteYn")
    List<Comment> findAll(@Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT c FROM Comment c WHERE c.id IN :ids")
    List<Comment> findAll(@Param("ids") List<Long> ids);
    
    @Query("SELECT c FROM Comment c WHERE c.id IN :ids AND c.deleteYn = :deleteYn")
    List<Comment> findAll(@Param("ids") List<Long> ids, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT c FROM Comment c WHERE c.writer.id = :writerId")
    List<Comment> findAllByWriter(@Param("writerId") Long writerId);
    
    @Query("SELECT c FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn")
    List<Comment> findAllByWriter(@Param("writerId") Long writerId, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId")
    List<Comment> findAllByPost(@Param("postId") Long postId);
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn")
    List<Comment> findAllByPost(@Param("postId") Long postId, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT c FROM Comment c ORDER BY c.createdDate DESC")
    Page<Comment> findAll(Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.writer.id = :writerId ORDER BY c.createdDate DESC")
    Page<Comment> findAllByWriter(@Param("writerId") Long writerId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.writer.id = :writerId AND c.deleteYn = :deleteYn ORDER BY c.createdDate DESC")
    Page<Comment> findAllByWriter(@Param("writerId") Long writerId,
                                  @Param("deleteYn") boolean deleteYn,
                                  Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdDate DESC")
    Page<Comment> findAllByPost(@Param("postId") Long postId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deleteYn = :deleteYn ORDER BY c.createdDate DESC")
    Page<Comment> findAllByPost(@Param("postId") Long postId, @Param("deleteYn") boolean deleteYn, Pageable pageable);
    
}
