CREATE TABLE member
(
    member_id   bigint  NOT NULL AUTO_INCREMENT COMMENT '멤버를 식별하기 위한 식별자로 고유 번호를 갖는다.',
    member_name varchar(64) NOT NULL COMMENT '멤버의 이름을 나타낸다.',
    CONSTRAINT pk_member PRIMARY KEY (member_id)
);