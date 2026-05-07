-- ========== SCM 模块 数据完整性 ==========
-- 建议在 table.sql、column.sql、menu.sql 之后执行；按「/」分段执行
-- 数据完整性检查，为有默认值的字段赋值
/
-- 开启登录页注册入口（没有该配置时才插入，默认 true 以显示注册链接）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'true', 'Y', 'admin', sysdate(), '是否开启注册功能（true开启后登录页显示立即注册、供应商/业务员注册）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'sys.account.registerUser');
/
-- 角色默认菜单继承策略：same_as_admin=继承管理员菜单（默认）；none=不自动分配菜单
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '医院职工默认菜单策略', 'scm.auth.bootstrap.hospital_staff.menu_mode', 'same_as_admin', 'N', 'admin', sysdate(), '医院创建/重置/补齐时，医院职工角色菜单策略（same_as_admin/none）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'scm.auth.bootstrap.hospital_staff.menu_mode');
/
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '供应商业务员默认菜单策略', 'scm.auth.bootstrap.supplier_sales.menu_mode', 'same_as_admin', 'N', 'admin', sysdate(), '供应商注册/重置/补齐时，供应商业务员角色菜单策略（same_as_admin/none）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'scm.auth.bootstrap.supplier_sales.menu_mode');
/
-- 供应商表 status 默认值
UPDATE scm_supplier SET status = '0' WHERE status IS NULL;
/
-- 修复菜单异常名称（避免角色授权树出现 undefined）
UPDATE sys_menu
SET menu_name = CONCAT('未命名菜单#', menu_id)
WHERE del_flag = '0'
  AND (menu_name IS NULL OR TRIM(menu_name) = '' OR LOWER(TRIM(menu_name)) IN ('undefined', 'null'));
/
UPDATE scm_supplier SET audit_status = '0' WHERE audit_status IS NULL;
/
UPDATE scm_supplier SET del_flag = '0' WHERE del_flag IS NULL;
/
-- 医院表
UPDATE scm_hospital SET status = '0' WHERE status IS NULL;
/
UPDATE scm_hospital SET del_flag = '0' WHERE del_flag IS NULL;
/
-- 物资/分类/厂家 状态与删除标志
UPDATE scm_material_category SET status = '0' WHERE status IS NULL;
/
UPDATE scm_material_category SET del_flag = '0' WHERE del_flag IS NULL;
/
UPDATE scm_material_dict SET status = '0' WHERE status IS NULL;
/
UPDATE scm_material_dict SET del_flag = '0' WHERE del_flag IS NULL;
/
UPDATE scm_manufacturer SET status = '0' WHERE status IS NULL;
/
UPDATE scm_manufacturer SET del_flag = '0' WHERE del_flag IS NULL;
/
-- 证件表
UPDATE scm_supplier_certificate SET audit_status = '0' WHERE audit_status IS NULL;
/
UPDATE scm_supplier_certificate SET is_expired = '0' WHERE is_expired IS NULL;
/
UPDATE scm_supplier_certificate SET is_warning = '0' WHERE is_warning IS NULL;
/
UPDATE scm_supplier_certificate SET status = '0' WHERE status IS NULL;
/
UPDATE scm_product_certificate SET audit_status = '0' WHERE audit_status IS NULL;
/
UPDATE scm_product_certificate SET is_expired = '0' WHERE is_expired IS NULL;
/
UPDATE scm_product_certificate SET is_warning = '0' WHERE is_warning IS NULL;
/
UPDATE scm_product_certificate SET status = '0' WHERE status IS NULL;
/
-- 订单/配送/结算 状态
UPDATE scm_order SET order_status = '0' WHERE order_status IS NULL;
/
UPDATE scm_delivery SET delivery_status = '0' WHERE delivery_status IS NULL;
/
-- 配送单关联订单来源（与 zs_order_id / order_id 对齐；第三方优先）
UPDATE scm_delivery SET ref_order_source = 'ZS'
WHERE (ref_order_source IS NULL OR ref_order_source = '')
  AND zs_order_id IS NOT NULL AND TRIM(zs_order_id) <> '';
/
UPDATE scm_delivery SET ref_order_source = 'SCM'
WHERE (ref_order_source IS NULL OR ref_order_source = '')
  AND order_id IS NOT NULL
  AND (zs_order_id IS NULL OR TRIM(zs_order_id) = '');
/
UPDATE scm_settlement SET audit_status = '0' WHERE audit_status IS NULL;
/
UPDATE scm_settlement SET customer_status = '0' WHERE customer_status IS NULL;
/
UPDATE scm_settlement SET status = '0' WHERE status IS NULL;
/
-- 证件类型表初始数据（已存在则跳过）
INSERT IGNORE INTO scm_certificate_type(type_code, type_name, type_category, description, order_num, status, create_by, create_time) VALUES
('SUPPLIER_001', '营业执照', 'supplier', '供应商营业执照', 1, '0', 'admin', sysdate());
/
INSERT IGNORE INTO scm_certificate_type(type_code, type_name, type_category, description, order_num, status, create_by, create_time) VALUES
('SUPPLIER_002', '经营许可证', 'supplier', '供应商经营许可证', 2, '0', 'admin', sysdate());
/
INSERT IGNORE INTO scm_certificate_type(type_code, type_name, type_category, description, order_num, status, create_by, create_time) VALUES
('SUPPLIER_003', '三证', 'supplier', '营业执照、组织机构代码证、税务登记证', 3, '0', 'admin', sysdate());
/
INSERT IGNORE INTO scm_certificate_type(type_code, type_name, type_category, description, order_num, status, create_by, create_time) VALUES
('PRODUCT_001', '医疗器械注册证', 'product', '产品医疗器械注册证', 1, '0', 'admin', sysdate());
/
INSERT IGNORE INTO scm_certificate_type(type_code, type_name, type_category, description, order_num, status, create_by, create_time) VALUES
('PRODUCT_002', '生产许可证', 'product', '产品生产许可证', 2, '0', 'admin', sysdate());
/
UPDATE scm_certificate_type SET status = '0' WHERE status IS NULL;
/
-- 证件配置：与 scm_certificate_type 联动，产品/供应商扩展证照按配置补行（无配置时按类型分类回退）
INSERT IGNORE INTO scm_certificate_config(config_type, certificate_type, warning_days, recent_days, status, create_by, create_time, del_flag) VALUES
('product_certificate', 'PRODUCT_001', 30, 7, '0', 'admin', sysdate(), '0');
/
INSERT IGNORE INTO scm_certificate_config(config_type, certificate_type, warning_days, recent_days, status, create_by, create_time, del_flag) VALUES
('product_certificate', 'PRODUCT_002', 30, 7, '0', 'admin', sysdate(), '0');
/
INSERT IGNORE INTO scm_certificate_config(config_type, certificate_type, warning_days, recent_days, status, create_by, create_time, del_flag) VALUES
('supplier_certificate', 'SUPPLIER_001', 30, 7, '0', 'admin', sysdate(), '0');
/
INSERT IGNORE INTO scm_certificate_config(config_type, certificate_type, warning_days, recent_days, status, create_by, create_time, del_flag) VALUES
('supplier_certificate', 'SUPPLIER_002', 30, 7, '0', 'admin', sysdate(), '0');
/
INSERT IGNORE INTO scm_certificate_config(config_type, certificate_type, warning_days, recent_days, status, create_by, create_time, del_flag) VALUES
('supplier_certificate', 'SUPPLIER_003', 30, 7, '0', 'admin', sysdate(), '0');
/


-- 注册用户角色（包含新增医院关联功能，没有才插入）
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark)
SELECT 110, '注册用户', 'register_user', 110, '1', '0', '0', 'admin', sysdate(), '平台注册用户，可申请关联供应商成为业务员'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id = 110);
/
INSERT INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(),'-',''), 110, 2004, '', '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 110 AND menu_id = 2004 AND hospital_id = '' AND supplier_id = '');
/
INSERT INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(),'-',''), 110, 20041, '', '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 110 AND menu_id = 20041 AND hospital_id = '' AND supplier_id = '');
/