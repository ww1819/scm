-- 存量库一次性：将 sys_role_menu 主键由 (role_id, menu_id) 迁为 id + 院/商范围唯一约束
-- 先执行 column.sql 中「sys_role_menu：增量列」段，再执行本脚本
-- 新装环境仅执行 scm/table.sql 即可，无需本脚本

ALTER TABLE sys_role_menu MODIFY COLUMN id varchar(36) NOT NULL COMMENT '主键 UUID7 风格';

ALTER TABLE sys_role_menu DROP PRIMARY KEY;

ALTER TABLE sys_role_menu ADD PRIMARY KEY (id);

ALTER TABLE sys_role_menu ADD UNIQUE KEY uk_role_menu_scope (role_id, menu_id, hospital_id, supplier_id);
