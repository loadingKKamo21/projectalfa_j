package com.project.alfa.repositories.mybatis;

import com.project.alfa.entities.Post;
import com.project.alfa.repositories.dto.SearchParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    
    void save(Post param);
    
    Post findById(Long id);
    
    Post findByIdAndDeleteYn(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    List<Post> findAll();
    
    List<Post> findAllByDeleteYn(boolean deleteYn);
    
    List<Post> findAllByIds(@Param("ids") List<Long> ids);
    
    List<Post> findAllByIdsAndDeleteYn(@Param("ids") List<Long> ids, @Param("deleteYn") boolean deleteYn);
    
    List<Post> findAllByWriter(Long writerId);
    
    List<Post> findAllByWriterAndDeleteYn(@Param("writerId") Long writerId, @Param("deleteYn") boolean deleteYn);
    
    List<Post> findAllBySearchParam(SearchParam param);
    
    List<Post> findAllBySearchParamAndDeleteYn(@Param("param") SearchParam param, @Param("deleteYn") boolean deleteYn);
    
    void addViewCount(Long id);
    
    void update(Post param);
    
    boolean existsById(Long id);
    
    boolean existsByIdAndDeleteYn(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    void deleteById(@Param("id") Long id, @Param("writerId") Long writerId);
    
    void permanentlyDeleteById(Long id);
    
    void deleteAllByIds(@Param("ids") List<Long> ids, @Param("writerId") Long writerId);
    
    void permanentlyDeleteAllByIds(@Param("ids") List<Long> ids);
    
    void deleteAll();
    
}
