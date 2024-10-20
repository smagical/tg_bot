CREATE TABLE IF NOT EXISTS tg_messages(
    id BIGINT ,
    chat_id BIGINT NOT NULL ,
    album BIGINT DEFAULT 0,
    content TEXT,
    link TEXT,
    other TEXT,
    PRIMARY KEY (id,chat_id)
);