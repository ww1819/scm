-- 修改产品证件表，允许supplier_id为NULL
ALTER TABLE scm_product_certificate MODIFY COLUMN supplier_id bigint(20) DEFAULT NULL COMMENT '供应商ID';

