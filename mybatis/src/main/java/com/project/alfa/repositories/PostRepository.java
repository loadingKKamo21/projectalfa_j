package com.project.alfa.repositories;

import com.project.alfa.entities.Post;
import com.project.alfa.repositories.dto.SearchParam;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    
    Post save(Post post);
    
    Optional<Post> findById(Long id);
    
    Optional<Post> findById(Long id, boolean deleteYn);
    
    List<Post> findAll();
    
    List<Post> findAll(boolean deleteYn);
    
    List<Post> findAll(List<Long> ids);
    
    List<Post> findAll(List<Long> ids, boolean deleteYn);
    
    List<Post> findAll(Long writerId);
    
    List<Post> findAll(Long writerId, boolean deleteYn);
    
    List<Post> findAll(Pageable pageable);
    
    List<Post> findAll(boolean deleteYn, Pageable pageable);
    
    List<Post> findAll(Long writerId, Pageable pageable);
    
    List<Post> findAll(Long writerId, boolean deleteYn, Pageable pageable);
    
    List<Post> findAll(SearchParam param, Pageable pageable);
    
    List<Post> findAll(SearchParam param, boolean deleteYn, Pageable pageable);
    
    void addViewCount(Long id);
    
    void update(Post param);
    
    boolean existsById(Long id);
    
    boolean existsById(Long id, boolean deleteYn);
    
    void deleteById(Long id, Long writerId);
    
    void permanentlyDeleteById(Long id);
    
    void deleteAllByIds(List<Long> ids, Long writerId);
    
    void permanentlyDeleteAllByIds(List<Long> ids);
    
    void deleteAll();
    
}
