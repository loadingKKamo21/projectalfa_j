# Project alfa

## 개요
Java / Spring Boot 기반 CRUD 프로젝트 \+ [Kotlin 버전](https://github.com/loadingKKamo21/projectalfa_k)

## 목차
1. [개발 환경](#개발-환경)
2. [설계 목표](#설계-목표)
3. [프로젝트 정보](#프로젝트-정보)
	- [구조](#-구조-)
		- [공통](#-공통-)
		- [JPA](#-JPA-)
		- [MyBatis](#-MyBatis-)
	- [클래스 다이어그램](#-클래스-다이어그램-)
	- [코드 예시](#-코드-예시-)
	- [설명](#-설명-)
4. [프로젝트 실행](#프로젝트-실행)

## 개발 환경

#### Backend
- Java, Spring Framework, JPA (+Querydsl), MyBatis
- AWS EC2

#### DB
- H2 Database, AWS RDS(MySQL), AWS S3, Redis
- Embedded Redis (for Test)

#### Tool
- IntelliJ IDEA, SmartGit, Gradle, DBeaver
- smtp4dev, Postman
- PuTTY, FileZilla

## 설계 목표
- 스프링 부트를 활용한 CRUD API 애플리케이션
- 같은 기능/동작을 목표로 JPA, MyBatis 프로젝트 분리
	- JPA: Data JPA 및 Querydsl 활용
		- V1: EntityManager 사용
		- V2: JpaRepository + Specification 사용
		- V3: JpaRepository + Querydsl 사용
	- MyBatis: Mapper 활용
- 계정 기능
    - 스프링 시큐리티 활용 2타입 설계: 이메일 계정, OAuth2 계정
        - ~~HTTP Basic 인증~~(주석)
        - JWT 인증: Refresh 토큰 Redis 서버 저장, 갱신 시 활용, 로그아웃 시 삭제
- 게시글 기능
    - READ: 제한 없음
    - CREATE, UPDATE, DELETE: 인증된 계정 및 작성자만 가능
    - 검색 및 페이징
    - 스프링 캐시 및 Redis를 활용한 조회 수 증가 로직
- 댓글 기능
    - READ: 제한 없음
    - CREATE, UPDATE, DELETE: 인증된 계정 및 작성자만 가능
    - 게시글 기준 페이징
- 첨부파일 기능
    - 파일 업로드/다운로드
    - 업로드 파일 정보 DB 저장 및 실제 파일 ~~로컬~~ AWS S3 저장
- UPDATE/DELETE 과정의 동시성 처리를 위한 락 추가
    - Service 계층 비즈니스 로직에 적용
    - JUnit5 테스트 코드 작성
- Repository / Service / Controller JUnit5 테스트 코드 작성

## 프로젝트 정보

#### [ 구조 ]

###### [ API ]
![OpenAPI UI](https://github.com/user-attachments/assets/2328157d-33d4-43c3-b599-26669bdaa459)

###### [ 공통 ]
```
+---main
|   +---java
|   |   \---com
|   |       \---project
|   |           \---alfa
|   |               |   AlfaApplication.java
|   |               |   InitDb.java            -> 더미 데이터 생성/저장
|   |               +---aop            //로깅 AOP
|   |               |   |   Pointcuts.java
|   |               |   |   ProjectAspects.java
|   |               |   +---annotation
|   |               |   |       ClassAop.java
|   |               |   |       LockAop.java            -> 동시성 처리 락 AOP
|   |               |   |       MethodAop.java
|   |               |   \---trace
|   |               |       |   TraceId.java
|   |               |       |   TraceStatus.java
|   |               |       \---logtrace
|   |               |               LogTrace.java
|   |               |               ThreadLocalLogTrace.java
|   |               +---config            //설정
|   |               |       AopConfig.java
|   |               |       AwsS3Config.java               -> AWS S3 설
|   |               |       CacheConfig.java               -> 캐시 설정
|   |               |       ProjectConfig.java             -> 빈 등록
|   |               |       RedisConfig.java               -> Redis 설정
|   |               |       SecurityConfig.java            -> 시큐리티 설정
|   |               |       WebConfig.java
|   |               +---controllers            //컨트롤러
|   |               |   |   MainController.java
|   |               |   \---api
|   |               |           AttachmentApiController.java
|   |               |           AuthApiController.java
|   |               |           CommentApiController.java
|   |               |           MemberApiController.java
|   |               |           PostApiController.java
|   |               +---entities            //엔티티
|   |               |       Attachment.java            -> 첨부파일(UploadFile 구현)
|   |               |       AuthInfo.java              -> 인증 정보(Member 필드)
|   |               |       Comment.java               -> 댓글
|   |               |       Member.java                -> 계정
|   |               |       Post.java                  -> 게시글
|   |               |       Role.java                  -> 계정 유형(enum)
|   |               |       UploadFile.java            -> 업로드 파일(abstract)
|   |               +---error            //예외
|   |               |   |   ErrorResponse.java            -> 에러 정보 전달
|   |               |   |   GlobalExceptionHandler.java
|   |               |   \---exception
|   |               |           BusinessException.java
|   |               |           EntityNotFoundException.java
|   |               |           ErrorCode.java            -> 에러 코드(enum)
|   |               |           InvalidValueException.java
|   |               +---interceptor            //인터셉터
|   |               |       LogInterceptor.java
|   |               +---repositories            //리포지토리(DAO)
|   |               |   +---dto
|   |               |   |       SearchParam.java            -> 검색 파라미터 DTO
|   |               +---security            //시큐리티
|   |               |   |   CustomAuthenticationFailureHandler.java
|   |               |   |   CustomAuthenticationProvider.java
|   |               |   |   CustomUserDetails.java                   -> UserDetails, OAuth2User 구현
|   |               |   |   CustomUserDetailsService.java            -> UserDetailsService 구현
|   |               |   +---jwt            //JWT 인증
|   |               |   |   +---entrypoint
|   |               |   |   |       JwtAuthenticationEntryPoint.java
|   |               |   |   \---filter
|   |               |   |       |   JwtAuthenticationFilter.java
|   |               |   |       |   JwtRequestFilter.java
|   |               |   |       \---dto
|   |               |   |               LoginBody.java            -> JWT 로그인 DTO
|   |               |   \---oauth2            //OAuth 2.0
|   |               |       |   CustomOAuth2UserService.java            -> DefaultOAuth2UserService 구현
|   |               |       \---provider
|   |               |               GoogleUserInfo.java                 -> Google용 인증 정보(OAuth2UserInfo 구현)
|   |               |               OAuth2UserInfo.java                 -> OAuth2 인증 정보(interface)
|   |               +---services            //서비스
|   |               |   |   AttachmentService.java
|   |               |   |   CommentService.java
|   |               |   |   JwtService.java
|   |               |   |   MemberService.java
|   |               |   |   PostService.java
|   |               |   \---dto
|   |               |           AttachmentResponseDto.java
|   |               |           CommentRequestDto.java
|   |               |           CommentResponseDto.java
|   |               |           MemberInfoResponseDto.java
|   |               |           MemberJoinRequestDto.java
|   |               |           MemberUpdateRequestDto.java
|   |               |           PostRequestDto.java
|   |               |           PostResponseDto.java
|   |               |           RegEx.java            -> 필드값 확인용 정규식 모음
|   |               \---utils            //유틸
|   |                       EmailSender.java                -> 이메일 전송 관련
|   |                       FileUtil.java                   -> 업로드 파일 관련
|   |                       RandomGenerator.java            -> 랜덤 데이터 생성(문자열, 숫자 등)
|   \---resources
|           application.yml
|           schema.sql
\---test
    \---java
        \---com
            \---project
                \---alfa
                    |   AlfaApplicationTests.java
                    +---config            //테스트 설정
                    |   |   DummyGenerator.java            -> 테스트용 더미 데이터 생성
                    |   |   TestConfig.java
                    |   +---redis
                    |   |       EmbeddedRedisConfig.java            -> 테스트용 EmbeddedRedis 설정
                    |   |       RandomPort.java
                    |   \---security
                    |           TestSecurityConfig.java            -> 테스트용 시큐리티 설정
                    |           WithCustomMockUser.java            -> 테스트용 인증 객체(annotation)
                    |           WithCustomSecurityContextFactory.java
                    +---controllers
                    |   |   MainControllerTest.java
                    |   \---api
                    |           AttachmentApiControllerTest.java
                    |           AuthApiControllerTest.java
                    |           CommentApiControllerTest.java
                    |           MemberApiControllerTest.java
                    |           PostApiControllerTest.java
                    +---services
                    |       AttachmentServiceConcurrencyTest.java            -> 동시성 처리 락 AOP 테스트
                    |       AttachmentServiceTest.java
                    |       CommentServiceConcurrencyTest.java               -> 동시성 처리 락 AOP 테스트
                    |       CommentServiceTest.java
                    |       JwtServiceConcurrencyTest.java                   -> 동시성 처리 락 AOP 테스트
                    |       JwtServiceTest.java
                    |       MemberServiceConcurrencyTest.java                -> 동시성 처리 락 AOP 테스트
                    |       MemberServiceTest.java
                    |       PostServiceConcurrencyTest.java                  -> 동시성 처리 락 AOP 테스트
                    |       PostServiceTest.java
                    \---utils
                            EmailSenderTest.java
```

###### [ JPA ]
```
+---main
|   +---java
|   |   \---com
|   |       \---project
|   |           \---alfa
|   |               +---entities
|   |               |       BaseTimeEntity.java
|   |               |       PersistentLogins.java            -> 시큐리티 remember-me
|   |               +---repositories
|   |               |   +---v1            //EntityManager 사용
|   |               |   |       AttachmentRepositoryV1.java
|   |               |   |       CommentRepositoryV1.java
|   |               |   |       MemberRepositoryV1.java
|   |               |   |       PostRepositoryV1.java
|   |               |   +---v2            //JpaRepository + Specification 사용
|   |               |   |   |   AttachmentJpaRepository.java
|   |               |   |   |   AttachmentRepositoryV2.java
|   |               |   |   |   CommentJpaRepository.java
|   |               |   |   |   CommentRepositoryV2.java
|   |               |   |   |   MemberJpaRepository.java
|   |               |   |   |   MemberRepositoryV2.java
|   |               |   |   |   PostJpaRepository.java
|   |               |   |   |   PostRepositoryV2.java
|   |               |   |   \---specification
|   |               |   |           PostSpecification.java
|   |               |   \---v3            //JpaRepository + Querydsl 사용
|   |               |       |   AttachmentRepositoryV3.java
|   |               |       |   CommentRepositoryV3.java
|   |               |       |   MemberRepositoryV3.java
|   |               |       |   PostRepositoryV3.java
|   |               |       \---querydsl
|   |               |               AttachmentRepositoryV3Custom.java
|   |               |               AttachmentRepositoryV3Impl.java
|   |               |               CommentRepositoryV3Custom.java
|   |               |               CommentRepositoryV3Impl.java
|   |               |               MemberRepositoryV3Custom.java
|   |               |               MemberRepositoryV3Impl.java
|   |               |               PostRepositoryV3Custom.java
|   |               |               PostRepositoryV3Impl.java
\---test
    \---java
        \---com
            \---project
                \---alfa
                    +---repositories
                    |   +---v1
                    |   |       AttachmentRepositoryV1Test.java
                    |   |       CommentRepositoryV1Test.java
                    |   |       MemberRepositoryV1Test.java
                    |   |       PostRepositoryV1Test.java
                    |   +---v2
                    |   |       AttachmentRepositoryV2Test.java
                    |   |       CommentRepositoryV2Test.java
                    |   |       MemberRepositoryV2Test.java
                    |   |       PostRepositoryV2Test.java
                    |   \---v3
                    |           AttachmentRepositoryV3Test.java
                    |           CommentRepositoryV3Test.java
                    |           MemberRepositoryV3Test.java
                    |           PostRepositoryV3Test.java
```

###### [ MyBatis ]
```
+---main
|   +---java
|   |   \---com
|   |       \---project
|   |           \---alfa
|   |               +---entities
|   |               |       EnumTypeHandler.java            -> Java-MySQL 간 enum 타입 변환
|   |               +---repositories
|   |               |   |   AttachmentRepository.java
|   |               |   |   CommentRepository.java
|   |               |   |   MemberRepository.java
|   |               |   |   PostRepository.java
|   |               |   \---mybatis            //MyBatis DAO
|   |               |           AttachmentMapper.java
|   |               |           AttachmentRepositoryImpl.java
|   |               |           CommentMapper.java
|   |               |           CommentRepositoryImpl.java
|   |               |           MemberMapper.java
|   |               |           MemberRepositoryImpl.java
|   |               |           MyBatisTokenRepositoryImpl.java            -> 시큐리티 remember-me
|   |               |           PersistentTokenMapper.java                 -> 시큐리티 remember-me
|   |               |           PostMapper.java
|   |               |           PostRepositoryImpl.java
|   \---resources
|       \---mappers            //MyBatis Mapper
|               AttachmentMapper.xml
|               CommentMapper.xml
|               MemberMapper.xml
|               PersistentTokenMapper.xml
|               PostMapper.xml
\---test
    \---java
        \---com
            \---project
                \---alfa
                    +---repositories
                    |       AttachmentRepositoryTest.java
                    |       CommentRepositoryTest.java
                    |       MemberRepositoryTest.java
                    |       PostRepositoryTest.java
```

#### [ 클래스 다이어그램 ]
- [JPA 프로젝트](./README-JPA-DIAGRAM.md)
- [MyBatis 프로젝트](./README-MYBATIS-DIAGRAM.md)

#### [ 코드 예시 ]
- [코드 예시 보기](./README-SAMPLE.md)

#### [ 설명 ]
- API 기반 설계
- JPA 프로젝트 OSIV OFF
    - Service 계층 외부로 엔티티 노출 억제
    - Controller-Service 전송 간 DTO 사용(MyBatis 프로젝트도 동일한 방식 적용)
    ![OSIV](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/eaff89eb-82e5-4d9c-8a15-e6b38122ea4c)
- 수정/삭제 로직에 동시성 처리 락 AOP 추가
    - 락 미획득 스레드의 경우 100ms 간격 최대 5회까지 재획득 시도
- 기능: 계정 관련
    - 스프링 시큐리티 기반 JWT 활용~~(HTTP Basic 인증 주석 처리)~~
    - JWT
        - 로그인 시 Access/Refresh 2가지 토큰 발급
        - Refresh 토큰은 Redis 서버 저장, Access 토큰 만료에 따른 재발급 시 활용
        - 필터, 엔드포인트로 Request 토큰 검증
        - 로그아웃 시 Redis 서버에 저장된 Refresh 토큰 삭제
        - Refresh 토큰은 HTTP 헤더, 쿠키, JSON 3가지 경우 모두 사용 가능
    - 회원 가입
        - 이메일 계정: SMTP를 통한 이메일 검증
        - Google OAuth2 계정
        - 이메일, Google 계정 중복 확인
        - 닉네임 중복 확인
    - SMTP를 통한 이메일 검증
        - 인증 제한 시간 존재
        - 전송된 메일에 포함된 이메일 정보 및 토큰으로 인증 확인
        - 제한 시간 초과 및 인증 토큰 오류로 인증 실패 시 새로운 토큰 발급 및 인증 메일 재발송
        - 미인증 시 로그인 불가
    - 비밀번호 찾기
        - 가입 시 사용한 이메일(아이디) 확인 후 임시 비밀번호 메일 발송
    - 회원 정보 조회
    - 회원 정보 수정
        - 비밀번호, 닉네임, 서명 수정 가능
        - 닉네임 중복 확인 및 이메일 미인증 계정 접근 시 인증 메일 발송
    - 회원 탈퇴
- 기능: 게시글 관련
    - 검색 페이징 목록 조회
        - 검색 조건(제목, 내용, 제목 또는 내용, 작성자), 검색 키워드
    - 작성자 기준 페이징 목록 조회
        - 로그인 된 계정 기준 작성한 게시글 페이징 조회
    - 게시글 조회
        - 조회 수 증가 캐싱, Redis 서버 저장
        - 중복 조회에 따른 조회 수 증가 방지
    - 게시글 작성
        - 인증된 계정만 접근 가능
    - 게시글 수정
        - 인증된 계정 + 게시글 작성자만 접근 가능
    - 게시글 삭제
        - 인증된 계정 + 게시글 작성자만 접근 가능
- 기능: 댓글 관련
    - 게시글 기준 페이징 목록 조회
    - 작성자 기준 페이징 목록 조회
        - 로그인 된 계정 기준 작성한 댓글 페이징 조회
    - 댓글 작성
        - 인증된 계정만 접근 가능
    - 댓글 수정
        - 인증된 계정 + 댓글 작성자만 접근 가능
    - 댓글 삭제
        - 인증된 계정 + 댓글 작성자만 접근 가능
- 기능: 첨부파일
    - 게시글 작성/수정 시 첨부파일 추가/삭제 기능
    - 첨부파일 다운로드

## 프로젝트 실행
- 기본 설정 구성
  ![img](https://github.com/user-attachments/assets/a3926c72-fcd9-450f-b35c-b347608ba902)
- DB 테이블(schema.sql) 생성 또는 spring.sql.init.mode 설정 활용
- .env 파일 내 환경변수 입력(application.yml 참고)
    ```
    AWS_RDS_ENDPOINT=
    AWS_RDS_DATABASE=
    AWS_RDS_USERNAME=
    AWS_RDS_PASSWORD= 
    OAUTH_GOOGLE_CLIENT_ID=
    OAUTH_GOOGLE_CLIENT_SECRET= 
    SMTP_GOOGLE_USERNAME=
    SMTP_GOOGLE_PASSWORD=
    AWS_ACCESS_KEY= 
    AWS_SECRET_KEY=
    AWS_REGION=
    FRONTEND_URL=
    JWT_SECRET=
    JWT_ISSUER= 
    AWS_S3_BUCKET=
    AWS_S3_UPLOAD_DIR=
    ```
    ```
    spring:
      datasource:
        url: jdbc:mysql://${AWS_RDS_ENDPOINT}:3306/${AWS_RDS_DATABASE}
        username: ${AWS_RDS_USERNAME}
        password: ${AWS_RDS_PASSWORD}
      security:
        oauth2:
          client:
            registration:
              google:
                client-id: ${OAUTH_GOOGLE_CLIENT_ID}
                client-secret: ${OAUTH_GOOGLE_CLIENT_SECRET}
      mail:
        host: smtp.gmail.com
        port: 587
        username: ${SMTP_GOOGLE_USERNAME}
        password: ${SMTP_GOOGLE_PASSWORD}
    cloud:
      aws:
        credentials:
          access-key: ${AWS_ACCESS_KEY}
          secret-key: ${AWS_SECRET_KEY}
        region:
          static: ${AWS_REGION}
    app:
      frontend:
        url: ${FRONTEND_URL}
    jwt:
      secret: ${JWT_SECRET}
      issuer: ${JWT_ISSUER}
    email:
      from: no-reply@${FRONTEND_URL}
    aws:
      s3:
        bucket: ${AWS_S3_BUCKET}
        upload-dir: ${AWS_S3_UPLOAD_DIR}
    ```
- 필요 시 더미 데이터 추가: InitDb.class