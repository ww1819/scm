-- 配送明细：医保编码（已有库执行一次）
ALTER TABLE `scm_delivery_detail`
  ADD COLUMN `medical_insurance_code` varchar(100) DEFAULT '' COMMENT '医保编码' AFTER `register_no`;
