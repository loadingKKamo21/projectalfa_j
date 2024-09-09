package com.project.alfa.repositories.mybatis;

import com.project.alfa.entities.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttachmentMapper {
    
    void save(Attachment param);
    
    void saveAll(@Param("params") List<Attachment> params);
    
    Attachment findById(Long id);
    
    Attachment findByIdAndDeleteYn(@Param("id") Long id, @Param("deleteYn") boolean deleteYn);
    
    List<Attachment> findAll();
    
    List<Attachment> findAllByDeleteYn(boolean deleteYn);
    
    List<Attachment> findAllByIds(@Param("ids") List<Long> ids);
    
    List<Attachment> findAllByIdsAndDeleteYn(@Param("ids") List<Long> ids, @Param("deleteYn") boolean deleteYn);
    
    List<Attachment> findAllByPost(Long postId);
    
    List<Attachment> findAllByPostAndDeleteYn(@Param("postId") Long postId, @Param("deleteYn") boolean deleteYn);
    
    void deleteById(@Param("id") Long id, @Param("postId") Long postId);
    
    void permanentlyDeleteById(Long id);
    
    void deleteAllByIds(@Param("ids") List<Long> ids, @Param("postId") Long postId);
    
    void permanentlyDeleteAllByIds(@Param("ids") List<Long> ids);
    
    void deleteAll();
    
}
