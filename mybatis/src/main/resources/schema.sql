DROP TABLE IF EXISTS tbl_members CASCADE;
DROP TABLE IF EXISTS tbl_posts CASCADE;
DROP TABLE IF EXISTS tbl_comments CASCADE;
DROP TABLE IF EXISTS tbl_persistent_logins CASCADE;
DROP TABLE IF EXISTS tbl_post_attachments CASCADE;

CREATE TABLE tbl_members
(
    member_id              BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'PK',
    username               VARCHAR UNIQUE NOT NULL COMMENT '아이디',
    password               VARCHAR        NOT NULL COMMENT '비밀번호',
    auth                   TINYINT(1) NOT NULL DEFAULT 0 COMMENT '인증 여부',
    authenticated_time     DATETIME COMMENT '계정 인증 시각',
    email_auth_token       VARCHAR COMMENT '이메일 인증 토큰',
    email_auth_expire_time DATETIME COMMENT '이메일 인증 만료 시간',
    o_auth_provider        VARCHAR COMMENT 'OAuth 2.0 Provider',
    o_auth_provider_id     VARCHAR COMMENT 'OAuth 2.0 Provider Id',
    nickname               VARCHAR UNIQUE NOT NULL COMMENT '닉네임',
    signature              VARCHAR COMMENT '서명',
    role                   TINYINT(1) NOT NULL COMMENT '계정 유형',
    created_date           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    last_modified_date     DATETIME                DEFAULT NULL COMMENT '최종 수정일시',
    delete_yn              TINYINT(1) NOT NULL DEFAULT 0 COMMENT '탈퇴 여부',
    PRIMARY KEY (member_id)
) COMMENT '계정';

CREATE TABLE tbl_posts
(
    post_id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'PK',
    member_id          BIGINT   NOT NULL COMMENT '작성자(계정) FK',
    title              VARCHAR  NOT NULL COMMENT '제목',
    content            LONGTEXT NOT NULL COMMENT '내용',
    view_count         INT      NOT NULL DEFAULT 0 COMMENT '조회수',
    notice_yn          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '공지 여부',
    created_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    last_modified_date DATETIME          DEFAULT NULL COMMENT '최종 수정일시',
    delete_yn          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    PRIMARY KEY (post_id),
    FOREIGN KEY (member_id) REFERENCES tbl_members (member_id)
) COMMENT '게시글';

CREATE TABLE tbl_comments
(
    comment_id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'PK',
    member_id          BIGINT   NOT NULL COMMENT '작성자(계정) FK',
    post_id            BIGINT   NOT NULL COMMENT '게시글 FK',
    content            TEXT     NOT NULL COMMENT '내용',
    created_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    last_modified_date DATETIME          DEFAULT NULL COMMENT '최종 수정일시',
    delete_yn          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    PRIMARY KEY (comment_id),
    FOREIGN KEY (member_id) REFERENCES tbl_members (member_id),
    FOREIGN KEY (post_id) REFERENCES tbl_posts (post_id)
) COMMENT '댓글';

CREATE TABLE tbl_persistent_logins
(
    username  VARCHAR(64) NOT NULL,
    series    VARCHAR(64) PRIMARY KEY,
    token     VARCHAR(64) NOT NULL,
    last_used TIMESTAMP   NOT NULL
);

CREATE TABLE tbl_post_attachments
(
    post_attachment_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'PK',
    post_id            BIGINT   NOT NULL COMMENT '게시글 FK',
    original_file_name VARCHAR  NOT NULL COMMENT '원본 파일명',
    store_file_name    VARCHAR  NOT NULL COMMENT '저장 파일명',
    store_file_path    VARCHAR  NOT NULL COMMENT '저장 경로',
    file_size          BIGINT   NOT NULL COMMENT '파일 크기',
    created_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    last_modified_date DATETIME          DEFAULT NULL COMMENT '최종 수정일시',
    delete_yn          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    PRIMARY KEY (post_attachment_id),
    FOREIGN KEY (post_id) REFERENCES tbl_posts (post_id)
) COMMENT '첨부파일';