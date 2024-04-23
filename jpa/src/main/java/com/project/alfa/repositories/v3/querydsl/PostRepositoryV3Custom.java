package com.project.alfa.repositories.v3.querydsl;

import com.project.alfa.entities.Post;
import com.project.alfa.repositories.dto.SearchParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryV3Custom {
    
    Page<Post> findAll(SearchParam param, Pageable pageable);
    
    Page<Post> findAll(SearchParam param, boolean deleteYn, Pageable pageable);
    
}
