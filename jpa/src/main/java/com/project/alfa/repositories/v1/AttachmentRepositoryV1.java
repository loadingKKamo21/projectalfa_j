package com.project.alfa.repositories.v1;

import com.project.alfa.entities.Attachment;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
public class AttachmentRepositoryV1 {
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * 첨부파일 저장
     *
     * @param attachment - 첨부파일 정보
     * @return 첨부파일 정보
     */
    public Attachment save(final Attachment attachment) {
        em.persist(attachment);
        return attachment;
    }
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> saveAll(final List<Attachment> attachments) {
        for (Attachment attachment : attachments)
            em.persist(attachment);
        return attachments;
    }
    
    /**
     * 첨부파일 다중 저장
     *
     * @param attachments - 첨부파일 정보 목록
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> saveAllAndFlush(final List<Attachment> attachments) {
        for (Attachment attachment : attachments)
            em.persist(attachment);
        em.flush();
        return attachments;
    }
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id - PK
     * @return 첨부파일 정보
     */
    public Optional<Attachment> findById(final Long id) {
        return Optional.ofNullable(em.createQuery("SELECT a FROM Attachment a WHERE a.id = :id", Attachment.class)
                                     .setParameter("id", id)
                                     .getResultList().stream().findFirst().orElse(null));
    }
    
    /**
     * 첨부파일 정보 조회
     *
     * @param id       - PK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보
     */
    public Optional<Attachment> findById(final Long id, final boolean deleteYn) {
        return Optional.ofNullable(
                em.createQuery("SELECT a FROM Attachment a WHERE a.id = :id AND a.deleteYn = :deleteYn",
                               Attachment.class)
                  .setParameter("id", id)
                  .setParameter("deleteYn", deleteYn)
                  .getResultList().stream().findFirst().orElse(null)
        );
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll() {
        return em.createQuery("SELECT a FROM Attachment a", Attachment.class).getResultList();
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final boolean deleteYn) {
        return em.createQuery("SELECT a FROM Attachment a WHERE a.deleteYn = :deleteYn", Attachment.class)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids - PK 목록
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final List<Long> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return em.createQuery("SELECT a FROM Attachment a WHERE a.id IN :ids", Attachment.class)
                 .setParameter("ids", ids)
                 .getResultList();
    }
    
    /**
     * 첨부파일 정보 목록 조회
     *
     * @param ids      - PK 목록
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final List<Long> ids, final boolean deleteYn) {
        if (ids.isEmpty())
            return Collections.emptyList();
        return em.createQuery("SELECT a FROM Attachment a WHERE a.id IN :ids AND a.deleteYn = :deleteYn",
                              Attachment.class)
                 .setParameter("ids", ids)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId - 게시글 FK
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final Long postId) {
        return em.createQuery("SELECT a FROM Attachment a WHERE a.post.id = :postId", Attachment.class)
                 .setParameter("postId", postId)
                 .getResultList();
    }
    
    /**
     * 게시글 기준 첨부파일 목록 조회
     *
     * @param postId   - 게시글 FK
     * @param deleteYn - 삭제 여부
     * @return 첨부파일 정보 목록
     */
    public List<Attachment> findAll(final Long postId, final boolean deleteYn) {
        return em.createQuery("SELECT a FROM Attachment a WHERE a.post.id = :postId AND a.deleteYn = :deleteYn",
                              Attachment.class)
                 .setParameter("postId", postId)
                 .setParameter("deleteYn", deleteYn)
                 .getResultList();
    }
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param attachment - 첨부파일 정보
     */
    public void delete(final Attachment attachment) {
        em.remove(em.find(Attachment.class, attachment.getId()));
    }
    
    /**
     * 첨부파일 정보 영구 삭제
     *
     * @param id - PK
     */
    public void deleteById(final Long id) {
        em.remove(em.find(Attachment.class, id));
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param attachments - 첨부파일 정보 목록
     */
    public void deleteAll(final List<Attachment> attachments) {
        for (Attachment attachment : attachments)
            em.remove(attachment);
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param attachments - 첨부파일 정보 목록
     */
    public void deleteAllInBatch(final List<Attachment> attachments) {
        List<Long> ids = attachments.stream().map(Attachment::getId).collect(toList());
        em.createQuery("DELETE FROM Attachment a WHERE a.id IN :ids").setParameter("ids", ids).executeUpdate();
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllById(final List<Long> ids) {
        for (Long id : ids)
            em.remove(em.find(Attachment.class, id));
    }
    
    /**
     * 첨부파일 정보 목록 영구 삭제
     *
     * @param ids - PK 목록
     */
    public void deleteAllByIdInBatch(final List<Long> ids) {
        em.createQuery("DELETE FROM Attachment a WHERE a.id IN :ids").setParameter("ids", ids).executeUpdate();
    }
    
}
