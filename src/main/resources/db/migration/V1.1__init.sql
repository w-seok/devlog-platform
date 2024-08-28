-- 유저 테이블 생성
CREATE TABLE account
(
    id                BIGINT    NOT NULL GENERATED ALWAYS AS IDENTITY,
    user_id      VARCHAR UNIQUE NOT NULL,               -- 유저 아이디
    password     VARCHAR,
    role         VARCHAR,
    active       BOOLEAN,
    created_at   TIMESTAMP      NOT NULL default NOW(),
    updated_at   TIMESTAMP      NOT NULL default NOW() -- 수정일
);
