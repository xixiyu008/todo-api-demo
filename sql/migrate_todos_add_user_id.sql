-- 适用于已经存在 app_users / todos 表的 MySQL 库。
-- 这个版本会删除所有历史任务数据，但保留用户账号。

START TRANSACTION;

DELETE FROM todos;

ALTER TABLE todos
    ADD COLUMN user_id BIGINT NULL;

ALTER TABLE todos
    MODIFY COLUMN user_id BIGINT NOT NULL;

CREATE INDEX idx_todos_user_id ON todos (user_id);

ALTER TABLE todos
    ADD CONSTRAINT fk_todos_user
    FOREIGN KEY (user_id) REFERENCES app_users(id);

COMMIT;
