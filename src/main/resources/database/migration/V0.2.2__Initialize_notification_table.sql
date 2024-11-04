create table notification (
    notification_id bigint  NOT NULL AUTO_INCREMENT COMMENT '알림 결과를 식별하기 위한 식별자로 고유 번호를 갖는다.',
    member_id integer not null,
    message text not null,
    created_at timestamp not null,
    constraint pk_notification primary key (notification_id)
);