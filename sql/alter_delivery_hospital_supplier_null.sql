-- 修改配送单表，允许hospital_id和supplier_id为NULL
ALTER TABLE scm_delivery MODIFY COLUMN hospital_id bigint(20) DEFAULT NULL COMMENT '医院ID';
ALTER TABLE scm_delivery MODIFY COLUMN supplier_id bigint(20) DEFAULT NULL COMMENT '供应商ID';

