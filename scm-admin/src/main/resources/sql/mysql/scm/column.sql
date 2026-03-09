-- ========== SCM 模块 增量字段（含 add_table_column 存储过程） ==========
-- 建议在 table.sql 之后执行；按「/」分段执行。新环境若已执行 table.sql 完整建表，本脚本中与 table 中已存在字段的 CALL 会跳过。
-- 本脚本已包含：add_*.sql/alter_*.sql 中的新增列、常见系统表扩展列。若数据库还有其它已存在但 table.sql 未定义的列，可在此追加 CALL add_table_column(表名, 列名, 类型, 注释, 默认值)。
-- 先删除再创建，保证可重复执行（MySQL 无 CREATE PROCEDURE IF NOT EXISTS）
/
DROP PROCEDURE IF EXISTS `add_table_column`;
/
/*
 * 存储过程：add_table_column
 * 功能：安全地为指定数据表添加新字段，避免重复添加
 */
CREATE PROCEDURE `add_table_column`(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_type VARCHAR(64),
    IN p_column_comment VARCHAR(256),
    IN p_default_value VARCHAR(256)
)
add_column_block:
BEGIN
    DECLARE v_column_exists INT DEFAULT 0;
    SET p_default_value = IFNULL(p_default_value, NULL);
    SET @dynamic_sql = '';
    IF p_table_name IS NULL OR p_table_name = ''
        OR p_column_name IS NULL OR p_column_name = ''
        OR p_column_type IS NULL OR p_column_type = ''
        OR p_column_comment IS NULL OR p_column_comment = '' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '错误：表名、字段名、字段类型、字段注释为必填参数，不能为空！';
    END IF;
    SELECT COUNT(*) INTO v_column_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;
    IF v_column_exists > 0 THEN
        SELECT CONCAT('提示：字段【', p_column_name, '】已存在于表【', p_table_name, '】，无需重复添加') AS 执行结果;
        LEAVE add_column_block;
    END IF;
    SET @dynamic_sql = CONCAT(
        'ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_column_type, ' '
    );
    IF p_default_value IS NOT NULL AND p_default_value != '' THEN
        SET @dynamic_sql = CONCAT(@dynamic_sql, 'DEFAULT ', QUOTE(p_default_value), ' ');
    END IF;
    SET @dynamic_sql = CONCAT(@dynamic_sql, 'COMMENT ', QUOTE(p_column_comment));
    PREPARE stmt FROM @dynamic_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SELECT CONCAT('成功：字段【', p_column_name, '】已成功添加到表【', p_table_name, '】') AS 执行结果;
    SET @dynamic_sql = '';
END;
/
-- ========== 供应商表新增字段 ==========
CALL add_table_column('scm_supplier', 'tax_number', 'varchar(50)', '税号', NULL);
/
CALL add_table_column('scm_supplier', 'qualification_expiry_date', 'date', '资质有效期', NULL);
/
-- ========== 产品证件表新增字段 ==========
CALL add_table_column('scm_product_certificate', 'udi_code', 'varchar(100)', '条码号(UDI)', NULL);
/
CALL add_table_column('scm_product_certificate', 'pinyin_code', 'varchar(50)', '拼音简码', NULL);
/
CALL add_table_column('scm_product_certificate', 'register_name', 'varchar(200)', '注册证名称', NULL);
/
CALL add_table_column('scm_product_certificate', 'register_valid_date', 'date', '注册有效期', NULL);
/
CALL add_table_column('scm_product_certificate', 'register_issue_date', 'date', '注册证发证日期', NULL);
/
CALL add_table_column('scm_product_certificate', 'bid_price', 'decimal(18,2)', '中标价格', '0');
/
CALL add_table_column('scm_product_certificate', 'sale_price', 'decimal(18,2)', '销售价格', '0');
/
CALL add_table_column('scm_product_certificate', 'hospital_code', 'varchar(50)', '医院编码', NULL);
/
CALL add_table_column('scm_product_certificate', 'sale_customer', 'varchar(200)', '销售客户', NULL);
/
CALL add_table_column('scm_product_certificate', 'product_category', 'varchar(20)', '产品类别（高值、低值）', NULL);
/
-- ========== 字段改为可空（已有表结构升级） ==========
ALTER TABLE scm_product_certificate MODIFY COLUMN supplier_id bigint(20) DEFAULT NULL COMMENT '供应商ID';
/
ALTER TABLE scm_order MODIFY COLUMN hospital_id bigint(20) DEFAULT NULL COMMENT '医院ID';
/
ALTER TABLE scm_order MODIFY COLUMN supplier_id bigint(20) DEFAULT NULL COMMENT '供应商ID';
/
ALTER TABLE scm_delivery MODIFY COLUMN hospital_id bigint(20) DEFAULT NULL COMMENT '医院ID';
/
ALTER TABLE scm_delivery MODIFY COLUMN supplier_id bigint(20) DEFAULT NULL COMMENT '供应商ID';
/
-- ========== 系统表常见增量列（若数据库已有则跳过，用于与已有库对齐） ==========
-- sys_menu 部分版本有 status 字段用于菜单启用/停用
CALL add_table_column('sys_menu', 'status', 'char(1)', '菜单状态（0正常 1停用）', '0');
/
