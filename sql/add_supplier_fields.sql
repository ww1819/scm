-- 添加供应商表新字段：税号和资质有效期
ALTER TABLE scm_supplier 
ADD COLUMN tax_number VARCHAR(50) DEFAULT '' COMMENT '税号' AFTER contact_phone,
ADD COLUMN qualification_expiry_date DATE DEFAULT NULL COMMENT '资质有效期' AFTER tax_number;

