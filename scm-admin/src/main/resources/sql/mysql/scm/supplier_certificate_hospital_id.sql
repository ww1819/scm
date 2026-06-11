-- 供应商证件按医院维度维护：新增 hospital_id 字段
-- 若已执行 column.sql 可跳过；否则在 scm_test 库执行本脚本

ALTER TABLE `scm_supplier_certificate`
  ADD COLUMN `hospital_id` bigint(20) DEFAULT NULL COMMENT '医院ID（按医院维护资质）' AFTER `supplier_id`;

CREATE INDEX `idx_supplier_cert_hospital_id` ON `scm_supplier_certificate` (`hospital_id`);
CREATE INDEX `idx_supplier_cert_supplier_hospital` ON `scm_supplier_certificate` (`supplier_id`, `hospital_id`);
