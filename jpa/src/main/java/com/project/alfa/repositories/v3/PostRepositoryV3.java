package com.project.alfa.repositories.v3;

import com.project.alfa.entities.Post;
import com.project.alfa.repositories.v3.querydsl.PostRepositoryV3Custom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryV3 extends JpaRepository<Post, Long>, PostRepositoryV3Custom {
    
    Optional<Post> findById(Long id);
    
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deleteYn = :deleteYn")
    Optional<Post> findById(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT p FROM Post p WHERE p.deleteYn = :deleteYn")
    List<Post> findAll(@Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT p FROM Post p WHERE p.id IN :ids")
    List<Post> findAll(@Param("ids") List<Long> ids);
    
    @Query("SELECT p FROM Post p WHERE p.id IN :ids AND p.deleteYn = :deleteYn")
    List<Post> findAll(@Param("ids") List<Long> ids, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT p FROM Post p WHERE p.writer.id = :writerId")
    List<Post> findAll(@Param("writerId") Long writerId);
    
    @Query("SELECT p FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn")
    List<Post> findAll(@Param("writerId") Long writerId, @Param("deleteYn") boolean deleteYn);
    
    @Query("SELECT p FROM Post p ORDER BY p.createdDate DESC")
    Page<Post> findAll(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.deleteYn = :deleteYn ORDER BY p.createdDate DESC")
    Page<Post> findAll(@Param("deleteYn") boolean deleteYn, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.writer.id = :writerId ORDER BY p.createdDate DESC")
    Page<Post> findAll(@Param("writerId") Long writerId, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.writer.id = :writerId AND p.deleteYn = :deleteYn ORDER BY p.createdDate DESC")
    Page<Post> findAll(@Param("writerId") Long writerId, @Param("deleteYn") boolean deleteYn, Pageable pageable);
    
}
