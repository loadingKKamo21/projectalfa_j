package com.project.alfa.repositories.v2;

import com.project.alfa.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PostJpaRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    Optional<Post> findByIdAndDeleteYn(Long id, boolean deleteYn);
    
    List<Post> findAllByDeleteYn(boolean deleteYn);
    
    List<Post> findAllByIdIn(List<Long> ids);
    
    List<Post> findAllByIdInAndDeleteYn(List<Long> ids, boolean deleteYn);
    
    List<Post> findAllByWriter_Id(Long writerId);
    
    List<Post> findAllByWriter_IdAndDeleteYn(Long writerId, boolean deleteYn);
    
    Page<Post> findAllByOrderByCreatedDateDesc(Pageable pageable);
    
    Page<Post> findAllByDeleteYn(boolean deleteYn, Pageable pageable);
    
    Page<Post> findAllByDeleteYnOrderByCreatedDateDesc(boolean deleteYn, Pageable pageable);
    
    Page<Post> findAllByWriter_Id(Long writerId, Pageable pageable);
    
    Page<Post> findAllByWriter_IdOrderByCreatedDateDesc(Long writerId, Pageable pageable);
    
    Page<Post> findAllByWriter_IdAndDeleteYn(Long writerId, boolean deleteYn, Pageable pageable);
    
    Page<Post> findAllByWriter_IdAndDeleteYnOrderByCreatedDateDesc(Long writerId, boolean deleteYn, Pageable pageable);
    
}
