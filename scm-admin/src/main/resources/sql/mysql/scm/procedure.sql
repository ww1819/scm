-- ========== SCM 模块 存储过程（SqlInitRunner 须在 column.sql 之前执行本文件） ==========
-- add_table_column、upgrade_uuid_column_if_varchar32；按「/」分段执行
-- 从 column.sql 拆出：保证先建过程再执行 column.sql 中的 CALL，避免出现「PROCEDURE scm.add_table_column does not exist」
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
DROP PROCEDURE IF EXISTS `upgrade_uuid_column_if_varchar32`;
/
CREATE PROCEDURE `upgrade_uuid_column_if_varchar32`(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_comment VARCHAR(256)
)
upgrade_uuid_block:
BEGIN
    DECLARE v_data_type VARCHAR(64) DEFAULT NULL;
    DECLARE v_len INT DEFAULT NULL;
    DECLARE v_table_exists INT DEFAULT 0;
    IF p_table_name IS NULL OR p_table_name = ''
        OR p_column_name IS NULL OR p_column_name = ''
        OR p_column_comment IS NULL OR p_column_comment = '' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '错误：表名、字段名、字段注释为必填参数，不能为空！';
    END IF;
    SELECT COUNT(*) INTO v_table_exists
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name;
    IF v_table_exists = 0 THEN
        SELECT CONCAT('跳过：表【', p_table_name, '】不存在') AS upgrade_uuid_column_result;
        LEAVE upgrade_uuid_block;
    END IF;
    SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH INTO v_data_type, v_len
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;
    IF v_data_type IS NULL THEN
        SELECT CONCAT('跳过：列【', p_table_name, '】.【', p_column_name, '】不存在') AS upgrade_uuid_column_result;
        LEAVE upgrade_uuid_block;
    END IF;
    IF v_data_type = 'varchar' AND v_len IS NOT NULL AND v_len >= 36 THEN
        SELECT CONCAT('跳过：【', p_table_name, '】.【', p_column_name, '】已为 varchar(', v_len, ')，无需变更') AS upgrade_uuid_column_result;
        LEAVE upgrade_uuid_block;
    END IF;
    SET @dynamic_sql = CONCAT(
        'ALTER TABLE `', p_table_name, '` MODIFY COLUMN `', p_column_name, '` varchar(36) NOT NULL COMMENT ', QUOTE(p_column_comment)
    );
    PREPARE stmt FROM @dynamic_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SET @dynamic_sql = NULL;
    SELECT CONCAT('成功：【', p_table_name, '】.【', p_column_name, '】已调整为 varchar(36)（原类型=', v_data_type, IF(v_len IS NULL, '', CONCAT(' 长度=', v_len)), '）') AS upgrade_uuid_column_result;
END;
/
