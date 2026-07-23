-- 订单查询：接收人 / 接收日期
-- 在目标库执行本脚本后重启后端

CALL add_table_column('scm_order', 'receive_by', 'varchar(64)', '接收人登录名', NULL);
/
CALL add_table_column('scm_order', 'receive_by_name_snapshot', 'varchar(64)', '接收人姓名快照（操作时落库，避免用户改名后不可追溯）', NULL);
/
CALL add_table_column('scm_order', 'receive_time', 'datetime', '接收时间', NULL);
/

-- 若库中无 add_table_column 存储过程，可改用：
-- ALTER TABLE scm_order
--   ADD COLUMN receive_by varchar(64) DEFAULT NULL COMMENT '接收人登录名' AFTER order_status,
--   ADD COLUMN receive_by_name_snapshot varchar(64) DEFAULT NULL COMMENT '接收人姓名快照' AFTER receive_by,
--   ADD COLUMN receive_time datetime DEFAULT NULL COMMENT '接收时间' AFTER receive_by_name_snapshot;

-- 历史已接收订单回填（仅空值）
UPDATE scm_order
SET receive_by = NULLIF(TRIM(update_by), ''),
    receive_by_name_snapshot = NULLIF(TRIM(update_by), ''),
    receive_time = update_time
WHERE order_status IN ('1', '2', '3')
  AND receive_time IS NULL
  AND update_time IS NOT NULL;
/
