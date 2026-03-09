-- ========== SCM 模块 数据完整性 ==========
-- 建议在 table.sql、column.sql、menu.sql 之后执行；按「/」分段执行
-- 数据完整性检查，为有默认值的字段赋值
/
-- 供应商表 status 默认值
UPDATE scm_supplier SET status = '0' WHERE status IS NULL;
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
