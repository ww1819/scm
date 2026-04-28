-- ========== SCM 模块 增量字段（含 add_table_column 存储过程） ==========
-- 建议在 table.sql 之后执行；按「/」分段执行。新环境若已执行 table.sql 完整建表，本脚本中与 table 中已存在字段的 CALL 会跳过。
-- 本脚本已包含：add_*.sql 中的新增列、常见系统表扩展列，以及 UUID 主键列宽升级（存储过程 upgrade_uuid_column_if_varchar32：列类型非 varchar，或 varchar 长度小于 36 时改为 varchar(36)；已为 varchar 且长度不小于 36 则跳过）。订单/条码四表建表定义在 scm/table.sql 末尾；scminterface 侧副本见 scminterface-admin/src/main/resources/sql/mysql/scm/table.sql。scm_hospital_menu_auth / scm_supplier_menu_auth / scm_hospital_supplier_permission 全量建表在 spd/spd-admin/src/main/resources/sql/mysql/material/table.sql。
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
CALL add_table_column('sys_menu', 'del_flag', 'char(1)', '删除标志（0未删除 1已删除）', '0');
/
CALL add_table_column('sys_menu', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('sys_menu', 'del_time', 'datetime', '删除时间', NULL);
/

-- sys_menu 重复数据治理建议（可选执行，先备份）
-- 说明：按 parent_id + menu_name + menu_type + url + perms 判重，保留 menu_id 最小的一条，其余软删除
WITH dup AS (
    SELECT
        menu_id,
        ROW_NUMBER() OVER (
            PARTITION BY parent_id, menu_name, menu_type, IFNULL(url, ''), IFNULL(perms, '')
            ORDER BY menu_id
        ) AS rn
    FROM sys_menu
    WHERE del_flag = '0'
)
UPDATE sys_menu m
INNER JOIN dup d ON d.menu_id = m.menu_id
SET m.del_flag = '1',
    m.del_by = IFNULL(m.del_by, 'system'),
    m.del_time = IFNULL(m.del_time, NOW()),
    m.update_time = NOW()
WHERE d.rn > 1;
/
-- ========== 标准 8 字段：create_by, create_time, update_by, update_time, del_flag, del_time, del_by, tenant_id ==========
-- 以下为各表缺失的列（表已有则通过 add_table_column 跳过）
/
-- scm_certificate_config：缺 del_flag, del_by, del_time, tenant_id
CALL add_table_column('scm_certificate_config', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_certificate_config', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_certificate_config', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_certificate_config', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_certificate_type
CALL add_table_column('scm_certificate_type', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_certificate_type', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_certificate_type', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_certificate_type', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_delivery
CALL add_table_column('scm_delivery', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_delivery', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_delivery', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_delivery', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
CALL add_table_column('scm_delivery', 'zs_order_id', 'varchar(36)', '中设订单主键 zs_tp_order.id', NULL);
/
CALL add_table_column('scm_delivery', 'src_order_supplier_id', 'varchar(128)', '订单供应商ID(字符串快照)', NULL);
/
CALL add_table_column('scm_delivery', 'src_order_supplier_name', 'varchar(256)', '订单供应商名称', NULL);
/
CALL add_table_column('scm_delivery', 'src_order_warehouse_id', 'varchar(128)', '订单仓库ID(字符串快照)', NULL);
/
CALL add_table_column('scm_delivery', 'src_order_warehouse_name', 'varchar(256)', '订单仓库名称', NULL);
/
CALL add_table_column('scm_delivery', 'src_order_dept_id', 'varchar(128)', '订单科室ID(字符串快照)', NULL);
/
CALL add_table_column('scm_delivery', 'src_order_dept_name', 'varchar(256)', '订单科室名称', NULL);
/
CALL add_table_column('scm_delivery', 'zs_customer_id', 'varchar(128)', '中设客户ID', NULL);
/
CALL add_table_column('scm_delivery', 'audit_status', 'char(1)', '审核状态（0待审核 1已审核 2已拒绝）', '0');
/
CALL add_table_column('scm_delivery', 'audit_by', 'varchar(64)', '审核人', NULL);
/
CALL add_table_column('scm_delivery', 'audit_time', 'datetime', '审核时间', NULL);
/
CALL add_table_column('scm_delivery', 'audit_remark', 'varchar(500)', '审核备注', NULL);
/
-- scm_delivery_detail
CALL add_table_column('scm_delivery_detail', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_delivery_detail', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_delivery_detail', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_delivery_detail', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
CALL add_table_column('scm_delivery_detail', 'zs_order_detail_id', 'varchar(36)', '中设明细主键 zs_tp_order_detail.id', NULL);
/
CALL add_table_column('scm_delivery_detail', 'main_barcode', 'varchar(128)', '主条码', NULL);
/
CALL add_table_column('scm_delivery_detail', 'aux_barcode', 'varchar(128)', '辅条码', NULL);
/
CALL add_table_column('scm_delivery_detail', 'pack_coefficient', 'decimal(18,6)', '打包系数', NULL);
/
-- scm_delivery_invoice
CALL add_table_column('scm_delivery_invoice', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_delivery_invoice', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_delivery_invoice', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_delivery_invoice', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_hospital：已有 del_flag，补 del_by, del_time, tenant_id
CALL add_table_column('scm_hospital', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_hospital', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_hospital', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_hospital_supplier
CALL add_table_column('scm_hospital_supplier', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_hospital_supplier', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_hospital_supplier', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_hospital_supplier', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
CALL add_table_column('scm_hospital_supplier', 'audit_status', 'char(1)', '审核状态（0待审核 1已通过 2已拒绝）', '0');
/
CALL add_table_column('scm_hospital_supplier', 'audit_time', 'datetime', '审核时间', NULL);
/
CALL add_table_column('scm_hospital_supplier', 'audit_by', 'varchar(64)', '审核人', NULL);
/
CALL add_table_column('scm_hospital_supplier', 'disable_status', 'char(1)', '停用状态（0启用 1停用）', '0');
/
CALL add_table_column('scm_hospital_supplier', 'stop_time', 'datetime', '停用时间', NULL);
/
CALL add_table_column('scm_hospital_supplier', 'stop_by', 'varchar(64)', '停用人', NULL);
/
CALL add_table_column('scm_hospital_supplier', 'supply_start_date', 'date', '供货开始日期', NULL);
/
CALL add_table_column('scm_hospital_supplier', 'supply_end_date', 'date', '供货结束日期', NULL);
/
CREATE TABLE IF NOT EXISTS `scm_hospital_supplier_apply` (
  `apply_id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `supplier_id` varchar(36) NOT NULL COMMENT '供应商ID',
  `hospital_id` varchar(36) NOT NULL COMMENT '医院ID',
  `supplier_name` varchar(200) DEFAULT '' COMMENT '供应商名称快照',
  `hospital_name` varchar(200) DEFAULT '' COMMENT '医院名称快照',
  `supply_start_date` date DEFAULT NULL COMMENT '供货开始日期',
  `supply_end_date` date DEFAULT NULL COMMENT '供货结束日期',
  `contract_no` varchar(64) DEFAULT '' COMMENT '合同编号',
  `apply_reason` varchar(1000) DEFAULT '' COMMENT '申请说明',
  `contact_person` varchar(64) DEFAULT '' COMMENT '联系人',
  `contact_phone` varchar(32) DEFAULT '' COMMENT '联系电话',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态（0待审核 1已通过 2已拒绝）',
  `audit_by` varchar(64) DEFAULT '' COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT '' COMMENT '审核备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除状态（0存在 2删除）',
  `del_by` varchar(64) DEFAULT '' COMMENT '删除者',
  `del_time` datetime DEFAULT NULL COMMENT '删除时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`apply_id`),
  KEY `idx_hsa_hospital` (`hospital_id`),
  KEY `idx_hsa_supplier` (`supplier_id`),
  KEY `idx_hsa_audit` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商向医院关联申请表';
/
CREATE TABLE IF NOT EXISTS `scm_hospital_supplier_apply_log` (
  `log_id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `apply_id` varchar(36) NOT NULL COMMENT '申请ID',
  `supplier_id` varchar(36) NOT NULL COMMENT '供应商ID',
  `hospital_id` varchar(36) NOT NULL COMMENT '医院ID',
  `action_type` varchar(32) NOT NULL COMMENT '动作类型',
  `oper_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `snapshot` longtext COMMENT '快照JSON',
  PRIMARY KEY (`log_id`),
  KEY `idx_hsal_apply` (`apply_id`),
  KEY `idx_hsal_hospital_supplier` (`hospital_id`,`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关联申请变更记录表';
/
CREATE TABLE IF NOT EXISTS `scm_notice_receiver` (
  `id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `notice_id` bigint(20) NOT NULL COMMENT '公告ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收人用户ID',
  `read_flag` char(1) DEFAULT '0' COMMENT '是否已读（0未读 1已读）',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`,`user_id`),
  KEY `idx_notice_receiver_user` (`user_id`),
  KEY `idx_notice_receiver_notice` (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知定向接收人表';
/
-- scm_hospital_user
CALL add_table_column('scm_hospital_user', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_hospital_user', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_hospital_user', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_hospital_user', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_manufacturer：已有 del_flag，补 del_by, del_time, tenant_id
CALL add_table_column('scm_manufacturer', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_manufacturer', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_manufacturer', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_material_category
CALL add_table_column('scm_material_category', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_material_category', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_material_category', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_material_dict
CALL add_table_column('scm_material_dict', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_material_dict', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_material_dict', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_order
CALL add_table_column('scm_order', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_order', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_order', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_order', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
CALL add_table_column('scm_order', 'order_supplier_name', 'varchar(256)', '订单供应商名称', NULL);
/
CALL add_table_column('scm_order', 'warehouse_id', 'bigint(20)', '订单仓库ID', NULL);
/
CALL add_table_column('scm_order', 'order_dept_id', 'bigint(20)', '订单科室ID', NULL);
/
CALL add_table_column('scm_order', 'order_dept_name', 'varchar(200)', '订单科室名称', NULL);
/
-- scm_order_detail
CALL add_table_column('scm_order_detail', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_order_detail', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_order_detail', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_order_detail', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
CALL add_table_column('scm_order_detail', 'pack_coefficient', 'decimal(18,6)', '打包系数', NULL);
/
-- scm_product_certificate
CALL add_table_column('scm_product_certificate', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_product_certificate', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_product_certificate', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_product_certificate', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_purchase_statistics：缺 create_by, update_by, del_flag, del_time, del_by, tenant_id
CALL add_table_column('scm_purchase_statistics', 'create_by', 'varchar(64)', '创建者', NULL);
/
CALL add_table_column('scm_purchase_statistics', 'update_by', 'varchar(64)', '更新者', NULL);
/
CALL add_table_column('scm_purchase_statistics', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_purchase_statistics', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_purchase_statistics', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_purchase_statistics', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_settlement
CALL add_table_column('scm_settlement', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_settlement', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_settlement', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_settlement', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_settlement_detail
CALL add_table_column('scm_settlement_detail', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_settlement_detail', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_settlement_detail', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_settlement_detail', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_supplier：已有 del_flag，补 del_by, del_time, tenant_id
CALL add_table_column('scm_supplier', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_supplier', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_supplier', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_supplier_certificate
CALL add_table_column('scm_supplier_certificate', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_supplier_certificate', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_supplier_certificate', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_supplier_certificate', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_supplier_code_mapping
CALL add_table_column('scm_supplier_code_mapping', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_supplier_code_mapping', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_supplier_code_mapping', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_supplier_code_mapping', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- scm_supplier_user
CALL add_table_column('scm_supplier_user', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_supplier_user', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_supplier_user', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_supplier_user', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- sys_dept：已有 del_flag，补 del_by, del_time, tenant_id
CALL add_table_column('sys_dept', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('sys_dept', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('sys_dept', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- sys_dept：ancestors 原 varchar(50) 在部门层级较深时易超长（updateDeptChildren 报错），改为 varchar(500)
ALTER TABLE `sys_dept` MODIFY COLUMN `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表';
/
-- scm_tenant：主键为 tenant_id（uuid7），补 del_flag, del_by, del_time, pinyin_code, planned_stop_time
CALL add_table_column('scm_tenant', 'del_flag', 'char(1)', '删除标志（0存在 2删除）', '0');
/
CALL add_table_column('scm_tenant', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('scm_tenant', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('scm_tenant', 'pinyin_code', 'varchar(64)', '拼音简码（客户名首字母）', NULL);
/
CALL add_table_column('scm_tenant', 'planned_stop_time', 'datetime', '计划停用时间', NULL);
/
-- sys_user：已有 del_flag，补 del_by, del_time, tenant_id（user_source 若未加可另脚本）
CALL add_table_column('sys_user', 'del_time', 'datetime', '删除时间', NULL);
/
CALL add_table_column('sys_user', 'del_by', 'varchar(64)', '删除人', NULL);
/
CALL add_table_column('sys_user', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
CALL add_table_column('sys_user', 'user_source', 'varchar(32)', '用户来源（platform/supplier/delivery/hospital）', NULL);
/
-- sys_role：租户维度角色（医院管理员等）
CALL add_table_column('sys_role', 'tenant_id', 'varchar(64)', '租户ID', NULL);
/
-- sys_role：供应商维度角色（供应商管理员、供应商业务员）
CALL add_table_column('sys_role', 'supplier_id', 'bigint(20)', '供应商ID', NULL);
/
-- scm_tenant_menu_pause：暂停时间（最近一次设为暂停的时间）
CALL add_table_column('scm_tenant_menu_pause', 'pause_time', 'datetime', '暂停时间', NULL);
/
-- 订单明细与配送单关联、条码种子等四表：建表已合并至 scm-admin 的 sql/mysql/scm/table.sql 末尾；scminterface 工程见 scminterface-admin/src/main/resources/sql/mysql/scm/table.sql
/
CALL add_table_column('zs_tp_order', 'receive_channel', 'varchar(16)', '接收渠道 TENANT=我方推送 ZS=中设客户推送', 'ZS');
/
CALL add_table_column('zs_tp_order', 'scm_sup_code', 'varchar(64)', '接口 SCMSUPCODE：SCM平台供应商编码（客户端随单传递）', NULL);
/
CALL add_table_column('scm_delivery', 'zs_jsfs', 'varchar(32)', '中设订单结算方式jsfs快照：3高值0低值', NULL);
/
-- ========== UUID 主键列统一为 varchar(36)（列非 varchar，或 varchar 长度小于 36 时 MODIFY；已为 varchar 且长度≥36 则跳过） ==========
CALL upgrade_uuid_column_if_varchar32('scm_order_detail_delivery_rel', 'id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('zs_tp_order_detail_delivery_rel', 'id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_barcode_seed', 'id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_delivery_detail_barcode', 'id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_tenant_status_period', 'period_id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_tenant_status_log', 'log_id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_tenant_modify_log', 'log_id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_tenant_menu', 'id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_tenant_menu_pause', 'pause_id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_tenant_menu_pause_log', 'log_id', '主键UUID7');
/
CALL upgrade_uuid_column_if_varchar32('scm_tenant_menu_pause_log', 'pause_id', '暂停控制ID');
/
-- ========== SCM 权限模型升级：菜单/角色维度、白名单与医院-供应商黑名单 ==========
CALL add_table_column('sys_menu', 'auth_type', 'varchar(20)', '菜单权限类型 platform/hospital/supplier/hospital_supplier', 'platform');
/
CALL add_table_column('sys_menu', 'data_binding_flag', 'char(1)', '废弃字段（历史兼容）', '0');
/
CALL add_table_column('sys_menu', 'default_open_scope', 'varchar(32)', '默认开放范围 none/all_hospital/all_supplier/all', 'none');
/
CALL add_table_column('sys_menu', 'hospital_grant_supplier_flag', 'char(1)', '是否需要由医院授予供应商 0否 1是', '0');
/
CALL add_table_column('sys_menu', 'menu_biz_category', 'varchar(32)', '业务分类 platform_ops/scm_auth/tenant/...', 'other');
/
CALL add_table_column('sys_role', 'role_type', 'varchar(20)', '角色类型 platform/hospital/supplier', 'platform');
/
CALL add_table_column('sys_role', 'hospital_id', 'bigint(20)', '医院角色绑定的医院ID', NULL);
/
-- scm_hospital_menu_auth、scm_supplier_menu_auth、scm_hospital_supplier_permission 全量建表已合并至 spd/spd-admin/src/main/resources/sql/mysql/material/table.sql
/
-- ========== sys_role_menu：增量列（varchar36）==========
-- 完整 PK 由主键 (role_id,menu_id) 迁到 id + uk(role_id,menu_id,hospital_id,supplier_id) 见 migrate_sys_role_menu_legacy_pk.sql（存量库执行一次；全新库仅用 scm/table.sql 已是最终结构）
CALL add_table_column('sys_role_menu', 'id', 'varchar(36)', '主键 UUID7', NULL);
/
CALL add_table_column('sys_role_menu', 'hospital_id', 'varchar(36)', '角色菜单绑定医院（空串不按院收窄）', NULL);
/
CALL add_table_column('sys_role_menu', 'supplier_id', 'varchar(36)', '角色菜单绑定供应商（空串不按商收窄）', NULL);
/
UPDATE sys_role_menu SET hospital_id = '' WHERE hospital_id IS NULL;
/
UPDATE sys_role_menu SET supplier_id = '' WHERE supplier_id IS NULL;
/
UPDATE sys_role_menu SET id = REPLACE(UUID(), '-', '') WHERE id IS NULL OR TRIM(id) = '';
/
UPDATE sys_role_menu rm INNER JOIN sys_role r ON r.role_id = rm.role_id
SET rm.hospital_id = CASE WHEN r.hospital_id IS NOT NULL THEN CAST(r.hospital_id AS CHAR) ELSE rm.hospital_id END,
    rm.supplier_id = CASE WHEN r.supplier_id IS NOT NULL THEN CAST(r.supplier_id AS CHAR) ELSE rm.supplier_id END;
/
ALTER TABLE sys_role_menu MODIFY COLUMN id varchar(36) NOT NULL COMMENT '主键 UUID7 风格';
/
ALTER TABLE sys_role_menu MODIFY COLUMN hospital_id varchar(36) NOT NULL DEFAULT '' COMMENT '绑定医院（空不按医院收窄）';
/
ALTER TABLE sys_role_menu MODIFY COLUMN supplier_id varchar(36) NOT NULL DEFAULT '' COMMENT '绑定供应商（空不按供应商收窄）';
/
/
CALL add_table_column('scm_hospital', 'pinyin_code', 'varchar(64)', '首拼简码（医院名称拼音首字母，小写）', NULL);
/
CALL add_table_column('scm_supplier', 'pinyin_code', 'varchar(64)', '首拼简码（公司名称拼音首字母，小写）', NULL);
/
CALL add_table_column('sys_role', 'pinyin_code', 'varchar(64)', '首拼简码（角色名称拼音首字母，小写）', NULL);
/
-- 菜单初始化/分类/白名单回填语句已迁移至 menu.sql，column.sql 仅保留结构变更。
