package com.project.alfa;

import com.project.alfa.entities.AuthInfo;
import com.project.alfa.entities.Comment;
import com.project.alfa.entities.Member;
import com.project.alfa.entities.Post;
import com.project.alfa.repositories.v1.CommentRepositoryV1;
import com.project.alfa.repositories.v1.MemberRepositoryV1;
import com.project.alfa.repositories.v1.PostRepositoryV1;
import com.project.alfa.repositories.v2.CommentRepositoryV2;
import com.project.alfa.repositories.v2.MemberRepositoryV2;
import com.project.alfa.repositories.v2.PostRepositoryV2;
import com.project.alfa.repositories.v3.CommentRepositoryV3;
import com.project.alfa.repositories.v3.MemberRepositoryV3;
import com.project.alfa.repositories.v3.PostRepositoryV3;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.project.alfa.utils.RandomGenerator.randomHangul;
import static com.project.alfa.utils.RandomGenerator.randomNumber;

//@Component
@RequiredArgsConstructor
public class InitDb {
    
    static final Random RANDOM = new Random();
    
    private final MemberRepositoryV1  memberRepository;
    //private final MemberRepositoryV2  memberRepository;
    //private final MemberRepositoryV3  memberRepository;
    private final PostRepositoryV1    postRepository;
    //private final PostRepositoryV2    postRepository;
    //private final PostRepositoryV3    postRepository;
    private final CommentRepositoryV1 commentRepository;
    //private final CommentRepositoryV2 commentRepository;
    //private final CommentRepositoryV3 commentRepository;
    private final PasswordEncoder     passwordEncoder;
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() throws InterruptedException {
        createMembers(1000);
        createPosts(memberRepository.findAll(), 5000);
        createComments(memberRepository.findAll(), postRepository.findAll(), 10000);
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
                                                    .build())
                                  .nickname("user" + num)
                                  .build();
            memberRepository.save(member);
            member.authenticate();
        }
    }
    
    private void createPosts(final List<Member> writers, final int size) throws InterruptedException {
        for (int i = 1; i <= size; i++) {
            Thread.sleep(1);
            Post post = Post.builder()
                            .writer(writers.get(RANDOM.nextInt(writers.size())))
                            .title(randomHangul(randomNumber(1, 100)))
                            .content(randomHangul(randomNumber(100, 500)))
                            .noticeYn(false)
                            .build();
            postRepository.save(post);
        }
    }
    
    private void createComments(final List<Member> writers, final List<Post> posts, final int size)
    throws InterruptedException {
        for (int i = 1; i <= size; i++) {
            Thread.sleep(1);
            Comment comment = Comment.builder()
                                     .writer(writers.get(RANDOM.nextInt(writers.size())))
                                     .post(posts.get(RANDOM.nextInt(posts.size())))
                                     .content(randomHangul(randomNumber(1, 100)))
                                     .build();
            commentRepository.save(comment);
        }
    }
    
}
