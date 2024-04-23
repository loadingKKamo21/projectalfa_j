package com.project.alfa;

import com.project.alfa.entities.*;
import com.project.alfa.repositories.mybatis.CommentMapper;
import com.project.alfa.repositories.mybatis.MemberMapper;
import com.project.alfa.repositories.mybatis.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.project.alfa.utils.RandomGenerator.randomHangul;
import static com.project.alfa.utils.RandomGenerator.randomNumber;

//@Component
@RequiredArgsConstructor
public class InitDb {
    
    static final Random RANDOM = new Random();
    
    private final MemberMapper    memberMapper;
    private final PostMapper      postMapper;
    private final CommentMapper   commentMapper;
    private final PasswordEncoder passwordEncoder;
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() throws InterruptedException {
        createMembers(1000);
        createPosts(memberMapper.findAll(), 5000);
        createComments(memberMapper.findAll(), postMapper.findAll(), 10000);
    }
    
    private void createMembers(final int size) throws InterruptedException {
        for (int i = 1; i <= size; i++) {
            Thread.sleep(1);
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
            memberMapper.save(member);
            memberMapper.authenticateEmail(member.getUsername(),
                                           member.getAuthInfo().getEmailAuthToken(),
                                           LocalDateTime.now());
        }
    }
    
    private void createPosts(final List<Member> writers, final int size) throws InterruptedException {
        for (int i = 1; i <= size; i++) {
            Thread.sleep(1);
            Post post = Post.builder()
                            .writerId(writers.get(RANDOM.nextInt(writers.size())).getId())
                            .title(randomHangul(randomNumber(1, 100)))
                            .content(randomHangul(randomNumber(100, 500)))
                            .noticeYn(false)
                            .build();
            postMapper.save(post);
        }
    }
    
    private void createComments(final List<Member> writers, final List<Post> posts, final int size)
    throws InterruptedException {
        for (int i = 1; i <= size; i++) {
            Thread.sleep(1);
            Comment comment = Comment.builder()
                                     .writerId(writers.get(RANDOM.nextInt(writers.size())).getId())
                                     .postId(posts.get(RANDOM.nextInt(posts.size())).getId())
                                     .content(randomHangul(randomNumber(1, 100)))
                                     .build();
            commentMapper.save(comment);
        }
    }
    
}
