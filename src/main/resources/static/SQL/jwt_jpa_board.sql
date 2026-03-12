show user;
-- USER이(가) "SYS"입니다.

alter session set "_ORACLE_SCRIPT"=true;
-- Session이(가) 변경되었습니다.

--  jwt_jpa_board_user 이라는 오라클 일반사용자 계정을 생성합니다. 암호는 sistsix 라고 하겠습니다.
create user jwt_jpa_board_user identified by sistsix default tablespace users;
-- User JWT_JPA_BOARD_USER이(가) 생성되었습니다.


-- 생성되어진 오라클 일반사용자 계정인 jwt_jpa_board_user 에게 오라클서버에 접속이 되어지고, 
-- 접속이 되어진 후 테이블 등을 생성할 수 있도록 권한을 부여해주겠다.
grant connect, resource, unlimited tablespace to jwt_jpa_board_user;
-- Grant을(를) 성공했습니다.

show user;
-- USER이(가) "JWT_JPA_BOARD_USER"입니다.

select * from tab;

desc tbl_prouct_image;

select * from tbl_prouct_image;

create sequence seq_prouct_image
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence SEQ_PROUCT_IMAGE이(가) 생성되었습니다.

insert into tbl_product_image(imgno, productname, imgfilename) values(seq_prouct_image.nextval, '미샤', '미샤.png');  
insert into tbl_product_image(imgno, productname, imgfilename) values(seq_prouct_image.nextval, '원더플레이스', '원더플레이스.png'); 
insert into tbl_product_image(imgno, productname, imgfilename) values(seq_prouct_image.nextval, '레노보', '레노보.png'); 
insert into tbl_product_image(imgno, productname, imgfilename) values(seq_prouct_image.nextval, '동원', '동원.png'); 

commit;

select imgno, productname, imgfilename
from tbl_product_image
order by imgno asc;


------ ====== *** 휴지통 조회하기 *** ====== ------
select *
from user_recyclebin;

purge recyclebin;  -- 휴지통에 있던 모든 테이블을 영구히 삭제하기
   -- Recyclebin이(가) 비워졌습니다.


select * from user_sequences;


------------------- **** >>> Spring Boot Security <<< **** -------------------
-- >>> 인증(로그인) + 권한(롤, 역할)

-----  로그인 처리  -----

-- >>> 회원 테이블(인증 테이블) 생성하기 <<< --
CREATE TABLE tbl_member (
   memberid          VARCHAR2(50)   NOT NULL              -- memberid 가 회원ID 임.
 , passwd            VARCHAR2(200)  NOT NULL              -- passwd 가 암호 임.  단방향 암호화 대상 이므로 컬럼의 길이가 커야함. 
 , enabled           CHAR(1)        DEFAULT '1' NOT NULL  -- enabled 컬럼의 값이 '1' 이면 접근가능
 , name              NVARCHAR2(30)  NOT NULL              -- 회원명  
 , registerday       DATE DEFAULT SYSDATE                 -- 회원가입 일자
 , last_login_date   DATE                                 -- 최근에 마지막으로 로그인한 일자
 , CONSTRAINT PK_tbl_member_memberid  PRIMARY KEY(memberid) 
);
-- Table TBL_MEMBER 이(가) 생성되었습니다.
/*
   메뉴 > Spring Security > 회원가입
   http://localhost:9082/member/memberRegister 
   에서 아래의 내용대로 성명은 name 의 값으로, 아이디는 memberid 값으로 비밀번호는 동일하게 qwer1234$ 로 6명의 회원가입을 시켜야 한다. 
   ---------------------------
    memberid       name
   --------------------------- 
    admin          관리자
    seoyh          서영학  ==> 자신의 이름의 해야함.
*/

SELECT *
FROM tbl_member
ORDER BY registerday ASC; 
/*

*/

SELECT memberid, name, 
       to_char(registerday, 'yyyy-mm-dd hh24:mi:ss') as registerday,
       to_char(last_login_date, 'yyyy-mm-dd hh24:mi:ss') as last_login_date
FROM tbl_member       
ORDER BY last_login_date DESC;
/*

*/

-- >>> 어쏘러티(권한, 역할) 테이블 생성하기 <<< --
CREATE TABLE tbl_authorities (
   num        NUMBER NOT NULL
 , memberid   VARCHAR2(50) NOT NULL  -- 회원아이디
 , authority  VARCHAR2(50) NOT NULL  -- authority 컬럼에 입력되는 값은 'ROLE_' 로 시작해야 한다. Spring Boot 프로그램에서는 자동적으로 'ROLE_' 를 붙여준다.  
 ,CONSTRAINT PK_tbl_authorities_num  PRIMARY KEY(num)
 ,CONSTRAINT UQ_tbl_authorities      UNIQUE(memberid, authority)
 ,CONSTRAINT FK_tbl_authorities_memberid  FOREIGN KEY(memberid) REFERENCES tbl_member(memberid) ON DELETE CASCADE
);
-- Table TBL_AUTHORITIES이(가) 생성되었습니다.

-- DROP SEQUENCE seq_tbl_authorities;
CREATE SEQUENCE seq_tbl_authorities
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;
-- Sequence SEQ_TBL_AUTHORITIES이(가) 생성되었습니다.

SELECT * 
FROM tbl_authorities
ORDER BY num ASC;


/*
    
*/

INSERT INTO tbl_authorities (num, memberid, authority) VALUES (seq_tbl_authorities.nextval, 'admin', 'ROLE_ADMIN');
COMMIT;

SELECT * 
FROM tbl_authorities
ORDER BY num ASC;
/*

4	admin	ROLE_USER
5	seoyh	ROLE_USER
9	admin	ROLE_ADMIN
    
*/


-- >>> refresh 토큰 테이블 생성하기 <<< --
CREATE TABLE tbl_refreshtoken
(memberid        VARCHAR2(50)   NOT NULL  
,rt_value        VARCHAR2(300)  NOT NULL   
,CONSTRAINT PK_refreshtoken_memberid PRIMARY KEY(memberid)
,CONSTRAINT FK_refreshtoken_memberid FOREIGN KEY(memberid) REFERENCES tbl_member(memberid) ON DELETE CASCADE
);
-- Table TBL_REFRESHTOKEN이(가) 생성되었습니다.

select *
from tbl_refreshtoken;


--- 게시글 관련 ---
CREATE SEQUENCE seq_board
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;
-- Sequence SEQ_BOARD이(가) 생성되었습니다.


CREATE TABLE tbl_board (
   num        NUMBER NOT NULL            -- 회원번호 
 , memberid   VARCHAR2(50)   NOT NULL    -- 회원아이디
 , subject    VARCHAR2(500)  NOT NULL    -- 글제목
 , content    VARCHAR2(4000) NOT NULL    -- 글내용
 , reg_date   DATE DEFAULT   SYSDATE NOT NULL  -- 작성일자 
 , read_count NUMBER DEFAULT 0 NOT NULL  -- 조회수 
 , CONSTRAINT PK_tbl_board_num  PRIMARY KEY(num)
 , CONSTRAINT FK_tbl_board_memberid  FOREIGN KEY(memberid) REFERENCES tbl_member(memberid)
);
-- Table TBL_BOARD이(가) 생성되었습니다.














