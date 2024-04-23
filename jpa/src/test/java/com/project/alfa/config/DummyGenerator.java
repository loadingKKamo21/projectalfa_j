package com.project.alfa.config;

import com.project.alfa.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DummyGenerator {
    
    static final Random RANDOM     = new Random();
    static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    @Autowired
    PasswordEncoder passwordEncoder;
    
    public String generateRandomString(final int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        return sb.toString();
    }
    
    public int generateRandomNumber(final int min, final int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }
    
    public List<Member> createMembers(final int size) {
        List<Member> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            String num = String.format("%0" + String.valueOf(size).length() + "d", i);
            Member member = Member.builder()
                                  .username("user" + num + "@mail.com")
                                  .password(passwordEncoder.encode("Password" + num + "!@"))
                                  .authInfo(AuthInfo.builder().emailAuthToken(UUID.randomUUID().toString()).build())
                                  .nickname("user" + num)
                                  .build();
            list.add(member);
        }
        return list;
    }
    
    public List<Post> createPosts(final List<Member> writers, final int size) {
        List<Post> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            Post post = Post.builder()
                            .writer(writers.get(RANDOM.nextInt(writers.size())))
                            .title(generateRandomString(generateRandomNumber(1, 100)))
                            .content(generateRandomString(generateRandomNumber(100, 500)))
                            .noticeYn(false)
                            .build();
            list.add(post);
        }
        return list;
    }
    
    public List<Comment> createComments(final List<Member> writers, final List<Post> posts, final int size) {
        List<Comment> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            Comment comment = Comment.builder()
                                     .writer(writers.get(RANDOM.nextInt(writers.size())))
                                     .post(posts.get(RANDOM.nextInt(posts.size())))
                                     .content(generateRandomString(generateRandomNumber(1, 100)))
                                     .build();
            list.add(comment);
        }
        return list;
    }
    
    public List<Attachment> createAttachments(final List<Post> posts, final int size) {
        List<Attachment> list = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            Attachment attachment = Attachment.builder()
                                              .post(posts.get(RANDOM.nextInt(posts.size())))
                                              .originalFilename(generateRandomString(generateRandomNumber(1, 10)))
                                              .storeFilename(generateRandomString(generateRandomNumber(1, 10)))
                                              .storeFilePath(generateRandomString(generateRandomNumber(10, 100)))
                                              .fileSize((long) generateRandomNumber(1, 1000000))
                                              .build();
            list.add(attachment);
        }
        return list;
    }
    
}
