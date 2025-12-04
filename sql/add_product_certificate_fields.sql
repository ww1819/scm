-- 为产品证件表添加新字段
ALTER TABLE scm_product_certificate
ADD COLUMN udi_code VARCHAR(100) DEFAULT '' COMMENT '条码号(UDI)' AFTER register_no,
ADD COLUMN pinyin_code VARCHAR(50) DEFAULT '' COMMENT '拼音简码' AFTER udi_code,
ADD COLUMN register_name VARCHAR(200) DEFAULT '' COMMENT '注册证名称' AFTER certificate_name,
ADD COLUMN register_valid_date DATE DEFAULT NULL COMMENT '注册有效期' AFTER expire_date,
ADD COLUMN register_issue_date DATE DEFAULT NULL COMMENT '注册证发证日期' AFTER register_date,
ADD COLUMN bid_price DECIMAL(18,2) DEFAULT 0 COMMENT '中标价格' AFTER register_valid_date,
ADD COLUMN sale_price DECIMAL(18,2) DEFAULT 0 COMMENT '销售价格' AFTER bid_price,
ADD COLUMN hospital_code VARCHAR(50) DEFAULT '' COMMENT '医院编码' AFTER sale_price,
ADD COLUMN sale_customer VARCHAR(200) DEFAULT '' COMMENT '销售客户' AFTER hospital_code,
ADD COLUMN product_category VARCHAR(20) DEFAULT '' COMMENT '产品类别（高值、低值）' AFTER sale_customer;

