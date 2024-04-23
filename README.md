# Project alfa

## 개요
Java / Spring Boot 기반 CRUD 프로젝트

## 목차
1. [개발 환경](#개발-환경)
2. [설계 목표](#설계-목표)
3. [프로젝트 정보](#프로젝트-정보)
	- [구조](#-구조-)
		- [공통](#-공통-)
		- [JPA](#-JPA-)
		- [MyBatis](#-MyBatis-)
	- [코드 예시](#-코드-예시-)
	- [설명](#-설명-)
4. [프로젝트 실행](#프로젝트-실행)

## 개발 환경

#### Backend
- Java, Spring Framework, JPA(+ Querydsl) or MyBatis

#### DB
- H2 Database, Redis
- Embedded Redis (for Test)

#### Tool
- IntelliJ IDEA, Gradle

## 설계 목표
- 스프링 부트를 활용한 CRUD API 애플리케이션
- 같은 기능/동작을 목표로 JPA, MyBatis 프로젝트 분리
	- JPA: Data JPA 및 Querydsl 활용
		- V1: EntityManager 사용
		- V2: JpaRepository + Specification 사용
		- V3: JpaRepository + Querydsl 사용
	- MyBatis: Mapper 활용
- 게시글/댓글 페이징, 게시글 검색 기능
- 스프링 시큐리티 활용 계정 2타입 설계: 이메일 인증 계정, OAuth2 계정
- 파일 업로드/다운로드
- 스프링 캐시 + Redis: 게시글 조회 수 증가 로직
- Repository / Service / Controller JUnit5 테스트 코드 작성

## 프로젝트 정보

#### [ 구조 ]

###### [ 공통 ]
```
+---main
|   +---java
|   |   \---com
|   |       \---project
|   |           \---alfa
|   |               |   AlfaApplication.java
|   |               |   InitDb.java	-> 더미 데이터 생성/저장
|   |               +---aop	//로깅 AOP
|   |               |   |   Pointcuts.java
|   |               |   |   ProjectAspects.java
|   |               |   +---annotation
|   |               |   |       ClassAop.java
|   |               |   |       MethodAop.java
|   |               |   \---trace
|   |               |       |   TraceId.java
|   |               |       |   TraceStatus.java
|   |               |       \---logtrace
|   |               |               LogTrace.java
|   |               |               ThreadLocalLogTrace.java
|   |               +---config	//설정
|   |               |       AopConfig.java
|   |               |       CacheConfig.java	-> 캐시 설정
|   |               |       ProjectConfig.java
|   |               |       RedisConfig.java	-> Redis 설정
|   |               |       SecurityConfig.java	-> 시큐리티 설정
|   |               |       WebConfig.java
|   |               +---controllers	//컨트롤러
|   |               |   |   MainController.java
|   |               |   \---api
|   |               |           AttachmentApiController.java
|   |               |           CommentApiController.java
|   |               |           MemberApiController.java
|   |               |           PostApiController.java
|   |               +---entities	//엔티티
|   |               |       Attachment.java	-> 첨부파일(UploadFile 구현체)
|   |               |       AuthInfo.java	-> 인증 정보(Member 필드)
|   |               |       Comment.java	-> 댓글
|   |               |       Member.java	-> 계정
|   |               |       Post.java	-> 게시글
|   |               |       Role.java	-> 계정 유형(enum)
|   |               |       UploadFile.java	-> 업로드 파일(abstract)
|   |               +---error	//에러 or 예외 관련
|   |               |   |   ErrorResponse.java	->에러 정보 전달
|   |               |   |   GlobalExceptionHandler.java
|   |               |   \---exception
|   |               |           BusinessException.java
|   |               |           EntityNotFoundException.java
|   |               |           ErrorCode.java	->에러 코드
|   |               |           InvalidValueException.java
|   |               +---interceptor	//인터셉터
|   |               |       LogInterceptor.java
|   |               +---repositories	//리포지토리
|   |               |   +---dto
|   |               |   |       SearchParam.java	-> 검색 파라미터
|   |               +---security	//시큐리티
|   |               |   |   CustomAuthenticationFailureHandler.java
|   |               |   |   CustomAuthenticationProvider.java
|   |               |   |   CustomUserDetails.java	-> UserDetails, OAuth2User 구현체
|   |               |   |   CustomUserDetailsService.java	-> UserDetailsService 구현체
|   |               |   \---oauth2
|   |               |       |   CustomOAuth2UserService.java	-> OAuth2 인증 서비스
|   |               |       \---provider
|   |               |               GoogleUserInfo.java
|   |               |               OAuth2UserInfo.java -> OAuth2 인증 정보 인터페이스
|   |               +---services	//서비스
|   |               |   |   AttachmentService.java
|   |               |   |   CommentService.java
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
|   |               |           RegEx.java	-> 필드값 확인용 정규표현식 모음
|   |               \---utils
|   |                       EmailSender.java	-> 이메일 전송
|   |                       FileUtil.java	-> 업로드 파일 관련
|   |                       RandomGenerator.java	-> 랜덤 데이터 생성
|   \---resources
|       |   application.yml
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
|   |               |       PersistentLogins.java	-> 시큐리티 remember-me
|   |               +---repositories	//JPA Repository
|   |               |   +---v1	//EntityManager 사용
|   |               |   |       AttachmentRepositoryV1.java
|   |               |   |       CommentRepositoryV1.java
|   |               |   |       MemberRepositoryV1.java
|   |               |   |       PostRepositoryV1.java
|   |               |   +---v2	//JpaRepository + Specification 사용
|   |               |   |   |   AttachmentJpaRepository.java
|   |               |   |   |   AttachmentRepositoryV2.java
|   |               |   |   |   CommentJpaRepository.java
|   |               |   |   |   CommentRepositoryV2.java
|   |               |   |   |   MemberJpaRepository.java
|   |               |   |   |   MemberRepositoryV2.java
|   |               |   |   |   PostJpaRepository.java
|   |               |   |   |   PostRepositoryV2.java
|   |               |   |   \---specification
|   |               |   |           PostSpecification.java	//게시글 검색 및 페이징
|   |               |   \---v3	//JpaRepository + Querydsl 사용
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
```

###### [ MyBatis ]
```
+---main
|   +---java
|   |   \---com
|   |       \---project
|   |           \---alfa
|   |               +---entities
|   |               |       EnumTypeHandler.java	-> Java-MySQL 간 enum 타입 변환
|   |               +---repositories
|   |               |   |   AttachmentRepository.java
|   |               |   |   CommentRepository.java
|   |               |   |   MemberRepository.java
|   |               |   |   PostRepository.java
|   |               |   \---mybatis	//MyBatis Repository
|   |               |           AttachmentMapper.java
|   |               |           AttachmentRepositoryImpl.java
|   |               |           CommentMapper.java
|   |               |           CommentRepositoryImpl.java
|   |               |           MemberMapper.java
|   |               |           MemberRepositoryImpl.java
|   |               |           MyBatisTokenRepositoryImpl.java
|   |               |           PersistentTokenMapper.java
|   |               |           PostMapper.java
|   |               |           PostRepositoryImpl.java
|   \---resources
|       +---mappers	//MyBatis Mapper.xml
|       |       AttachmentMapper.xml
|       |       CommentMapper.xml
|       |       MemberMapper.xml
|       |       PersistentTokenMapper.xml	-> 시큐리티 remember-me
|       |       PostMapper.xml
```

#### [ 코드 예시 ]
![jpa4](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/0dc56740-3a24-4724-8c76-e6f7ad34b3b8)
![jpa3](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/b2161756-e064-4ee5-8631-e76b22bca6f4)
![jpa2](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/31ef5f20-74df-4b96-a5b0-d969d02fe0c7)
![jpa1](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/58cac080-c815-4723-a7bd-753d01f478af)
![mybatis2](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/a94cb7ac-4d00-47d5-bb6e-c40254e8aed1)
![mybatis1](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/dbf12360-ed8e-42e6-a50f-a14b5ffa97e3)
![controller-test3](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/d2d9d6e2-3e13-44bf-bbd3-52182424bbff)
![controller-test2](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/344689f1-81b7-4ad0-a351-f183e7c69036)
![controller-test1](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/b1223d36-6fd9-41ad-9a10-2a733a48d0d5)
![jpa-test3](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/c3ef41d5-6876-44ff-87af-f23864bbc489)
![jpa-test2](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/5830528f-2e66-4309-8d61-83235bc14dea)
![jpa-test1](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/c24376c5-b1b1-402e-a45c-04a18f19e111)
![jpa7](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/0004fcfd-cce8-4ab8-9f75-35dd85238382)
![jpa6](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/82e753d6-5913-4b10-99f6-c9dbf1719ea4)
![jpa5](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/5173ab8e-bc7a-4979-87d0-a9ebf22d342a)

#### [ 설명 ]
- 계정, 게시글, 댓글, 첨부파일 Create / Read / Update / Delete
- API 기반 설계
- 게시글/댓글 조회 목록 페이징, 게시글 검색 기능
- 스프링 시큐리티 연동 2가지 계정 가입 방식
	- 아이디(이메일) + 이메일 인증
	- OAuth2
- JPA 프로젝트 OSIV OFF 설정
	- 서비스 레이어 외부로 엔티티 노출 억제
	- 컨트롤러-서비스 전송 간 DTO 사용(MyBatis 프로젝트도 동일한 방식 적용)
    ![OSIV](https://github.com/loadingKKamo21/projectalfa_j/assets/90470901/eaff89eb-82e5-4d9c-8a15-e6b38122ea4c)
- Redis 캐시 사용
	- 게시글 조회수 증가 로직
- 컨트롤러/서비스/리포지토리 테스트 코드 작성

## 프로젝트 실행
- 기본 설정값 기반
- application.yml 설정
	- DB: [H2 Database](https://www.h2database.com/html/main.html)와 [Redis](https://redis.io/) 설치/실행
	```
	...
	spring:
		datasource:
			driver-class-name: org.h2.Driver
			url: jdbc:h2:tcp://localhost/~/test;MODE=MYSQL;DATABASE_TO_LOWER=TRUE
			username: sa
			password:
	...
	redis:
		host: localhost
		port: 6379
		password:
		lettuce:
			pool:
				min-idle: 0
				max-idle: 8
				max-active: 8
	...
	```
	- SMTP: 이메일 전송 시 사용, 기본값 Google SMTP
	```
	...
	spring:
		mail:
			host: smtp.gmail.com
			port: 587
			username: { Google Username }
			password: { Google Password }
			properties:
				...
	```
	- OAuth2: 기본값 Google, 타 OAuth2 사용 시 OAuth2UserInfo 구현체 추가 설정 필요
	```
	...
	spring:
		security:
			oauth2:
				client:
					registration:
						google:
							client-id: { Google OAuth 2.0 Client-Id }
							client-secret: { Google OAuth 2.0 Client-Secret }
							scope:
								- email
								- profile
	...
	```
	- File Upload Path: 파일 업로드 경로 등록
	```
	file:
		upload:
			location: { Upload Path }
	```
	- 더미 데이터 추가: InitDb.class
