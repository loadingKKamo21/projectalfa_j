package com.project.alfa.repositories.mybatis;

import com.project.alfa.entities.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    
    void save(Comment param);
    
    Comment findById(Long id);
    
    Comment findByIdAndDeleteYn(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    List<Comment> findAll();
    
    List<Comment> findAllByDeleteYn(boolean deleteYn);
    
    List<Comment> findAllByIds(@Param("ids") List<Long> ids);
    
    List<Comment> findAllByIdsAndDeleteYn(@Param("ids") List<Long> ids, @Param("deleteYn") boolean deleteYn);
    
    List<Comment> findAllByWriter(Long writerId);
    
    List<Comment> findAllByWriterAndDeleteYn(@Param("writerId") Long writerId, @Param("deleteYn") boolean deleteYn);
    
    List<Comment> findAllByPost(Long postId);
    
    List<Comment> findAllByPostAndDeleteYn(@Param("postId") Long postId, @Param("deleteYn") boolean deleteYn);
    
    void update(Comment param);
    
    void deleteById(@Param("id") Long id, @Param("writerId") Long writerId);
    
    void permanentlyDeleteById(Long id);
    
    void deleteAllByIds(@Param("ids") List<Long> ids, @Param("writerId") Long writerId);
    
    void permanentlyDeleteAllByIds(@Param("ids") List<Long> ids);
    
}
