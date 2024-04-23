package com.project.alfa.config;

import com.project.alfa.entities.*;
import com.project.alfa.repositories.mybatis.AttachmentMapper;
import com.project.alfa.repositories.mybatis.CommentMapper;
import com.project.alfa.repositories.mybatis.MemberMapper;
import com.project.alfa.repositories.mybatis.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DummyGenerator {
    
    static final Random RANDOM     = new Random();
    static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    @Autowired
    MemberMapper     memberMapper;
    @Autowired
    PostMapper       postMapper;
    @Autowired
    CommentMapper    commentMapper;
    @Autowired
    AttachmentMapper attachmentMapper;
    @Autowired
    PasswordEncoder  passwordEncoder;
    
    public String generateRandomString(final int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        return sb.toString();
    }
    
    public int generateRandomNumber(final int min, final int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }
    
    public List<Member> createMembers(final int size, final boolean save) {
        List<Member> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            String num = String.format("%0" + String.valueOf(size).length() + "d", i);
            Member member = Member.builder()
                                  .username("user" + num + "@mail.com")
                                  .password(passwordEncoder.encode("Password" + num + "!@"))
                                  .authInfo(AuthInfo.builder()
                                                    .emailAuthToken(UUID.randomUUID().toString())
                                                    .emailAuthExpireTime(LocalDateTime.now().withNano(0).plusMinutes(5))
                                                    .build())
                                  .nickname("user" + num)
                                  .role(Role.USER)
                                  .build();
            if (save)
                memberMapper.save(member);
            list.add(member);
        }
        return list;
    }
    
    public List<Post> createPosts(final List<Member> writers, final int size, final boolean save)
    throws InterruptedException {
        List<Post> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            Thread.sleep(1);
            Post post = Post.builder()
                            .writerId(writers.get(RANDOM.nextInt(writers.size())).getId())
                            .title(generateRandomString(generateRandomNumber(1, 100)))
                            .content(generateRandomString(generateRandomNumber(100, 500)))
                            .noticeYn(false)
                            .build();
            if (save)
                postMapper.save(post);
            list.add(post);
        }
        return list;
    }
    
    public void randomlyDeletePosts(final List<Post> posts, final int count) {
        int deleteCount = 0;
        while (count > 0) {
            if (count == deleteCount)
                break;
            Post post = posts.get(RANDOM.nextInt(posts.size()));
            if (post.isDeleteYn())
                continue;
            Long id       = post.getId();
            Long writerId = post.getWriterId();
            postMapper.deleteById(id, writerId);
            deleteCount++;
        }
    }
    
    public List<Comment> createComments(final List<Member> writers, final List<Post> posts, final int size,
                                        final boolean save) throws InterruptedException {
        List<Comment> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            Thread.sleep(1);
            Comment comment = Comment.builder()
                                     .writerId(writers.get(RANDOM.nextInt(writers.size())).getId())
                                     .postId(posts.get(RANDOM.nextInt(posts.size())).getId())
                                     .content(generateRandomString(generateRandomNumber(1, 100)))
                                     .build();
            if (save)
                commentMapper.save(comment);
            list.add(comment);
        }
        return list;
    }
    
    public void randomlyDeleteComments(final List<Comment> comments, final int count) {
        int deleteCount = 0;
        while (count > 0) {
            if (count == deleteCount)
                break;
            Comment comment = comments.get(RANDOM.nextInt(comments.size()));
            if (comment.isDeleteYn())
                continue;
            Long id       = comment.getId();
            Long writerId = comment.getWriterId();
            commentMapper.deleteById(id, writerId);
            deleteCount++;
        }
    }
    
    public List<Attachment> createAttachments(final List<Post> posts, final int size, final boolean save)
    throws InterruptedException {
        List<Attachment> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            Thread.sleep(1);
            Attachment attachment = Attachment.builder()
                                              .postId(posts.get(RANDOM.nextInt(posts.size())).getId())
                                              .originalFilename(generateRandomString(generateRandomNumber(1, 10)))
                                              .storeFilename(generateRandomString(generateRandomNumber(1, 10)))
                                              .storeFilePath(generateRandomString(generateRandomNumber(10, 100)))
                                              .fileSize((long) generateRandomNumber(1, 1000000))
                                              .build();
            if (save)
                attachmentMapper.save(attachment);
            list.add(attachment);
        }
        return list;
    }
    
    public void randomlyDeleteAttachments(final List<Attachment> attachments, final int count) {
        int deleteCount = 0;
        while (count > 0) {
            if (count == deleteCount)
                break;
            Attachment attachment = attachments.get(RANDOM.nextInt(attachments.size()));
            if (attachment.isDeleteYn())
                continue;
            Long id     = attachment.getId();
            Long postId = attachment.getPostId();
            attachmentMapper.deleteById(id, postId);
            deleteCount++;
        }
    }
    
}
