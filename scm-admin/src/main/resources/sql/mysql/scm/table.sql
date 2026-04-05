-- ========== SCM 供应商管理平台 建表脚本 ==========
-- 执行顺序：1.table.sql 2.column.sql 3.view.sql 4.trigger.sql 5.procedure.sql 6.function.sql 7.menu.sql 8.data_integrity.sql
-- 按「/」分段，每段一条语句执行
/
CREATE TABLE IF NOT EXISTS `scm_supplier` (
  `supplier_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_code` varchar(50) DEFAULT '' COMMENT '供应商编码',
  `company_name` varchar(200) NOT NULL COMMENT '公司名称',
  `company_short_name` varchar(100) DEFAULT '' COMMENT '公司简称',
  `legal_person` varchar(50) DEFAULT '' COMMENT '法人',
  `registered_capital` decimal(18,2) DEFAULT 0 COMMENT '注册资金',
  `province` varchar(50) DEFAULT '' COMMENT '省份/直辖市',
  `city` varchar(50) DEFAULT '' COMMENT '城市',
  `district` varchar(50) DEFAULT '' COMMENT '县级/区',
  `address` varchar(500) DEFAULT '' COMMENT '详细联系地址',
  `business_scope` varchar(1000) DEFAULT '' COMMENT '经营范围',
  `email` varchar(100) DEFAULT '' COMMENT '邮箱',
  `website` varchar(200) DEFAULT '' COMMENT '网址',
  `contact_person` varchar(50) DEFAULT '' COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `tax_number` varchar(50) DEFAULT '' COMMENT '税号',
  `qualification_expiry_date` date DEFAULT NULL COMMENT '资质有效期',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待审核 1正常 2停用）',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态（0待审核 1已审核 2已拒绝）',
  `audit_by` varchar(64) DEFAULT '' COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT '' COMMENT '审核备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`supplier_id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商信息表';
/
CREATE TABLE IF NOT EXISTS `scm_supplier_user` (
  `supplier_user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '供应商用户ID',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `is_main` char(1) DEFAULT '0' COMMENT '是否主账号（0否 1是）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`supplier_user_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商用户表';
/
CREATE TABLE IF NOT EXISTS `scm_supplier_user_apply` (
  `apply_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待审核 1通过 2拒绝）',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `audit_by` varchar(64) DEFAULT '' COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT '' COMMENT '审核备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`apply_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商业务员关联申请';
/
CREATE TABLE IF NOT EXISTS `scm_supplier_code_mapping` (
  `mapping_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '对照ID',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `supplier_code` varchar(50) DEFAULT '' COMMENT '供应商编码',
  `hospital_code` varchar(50) DEFAULT '' COMMENT '医院编码',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`mapping_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_hospital_id` (`hospital_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商编码对照表';
/
CREATE TABLE IF NOT EXISTS `scm_hospital` (
  `hospital_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '医院ID',
  `hospital_code` varchar(50) DEFAULT '' COMMENT '医院编码',
  `hospital_name` varchar(200) NOT NULL COMMENT '医院名称',
  `hospital_short_name` varchar(100) DEFAULT '' COMMENT '医院简称',
  `hospital_level` varchar(50) DEFAULT '' COMMENT '医院等级',
  `province` varchar(50) DEFAULT '' COMMENT '省份/直辖市',
  `city` varchar(50) DEFAULT '' COMMENT '城市',
  `district` varchar(50) DEFAULT '' COMMENT '县级/区',
  `address` varchar(500) DEFAULT '' COMMENT '详细地址',
  `contact_person` varchar(50) DEFAULT '' COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `email` varchar(100) DEFAULT '' COMMENT '邮箱',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`hospital_id`),
  UNIQUE KEY `uk_hospital_code` (`hospital_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院信息表';
/
CREATE TABLE IF NOT EXISTS `scm_hospital_user` (
  `hospital_user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '医院用户ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `is_main` char(1) DEFAULT '0' COMMENT '是否主账号（0否 1是）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`hospital_user_id`),
  KEY `idx_hospital_id` (`hospital_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院用户表';
/
CREATE TABLE IF NOT EXISTS `scm_hospital_supplier` (
  `relation_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `hospital_id` bigint(20) NOT NULL COMMENT '医院ID',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `bind_status` char(1) DEFAULT '0' COMMENT '绑定状态（0待审核 1已绑定 2已解绑）',
  `bind_time` datetime DEFAULT NULL COMMENT '绑定时间',
  `bind_by` varchar(64) DEFAULT '' COMMENT '绑定操作人',
  `unbind_time` datetime DEFAULT NULL COMMENT '解绑时间',
  `unbind_by` varchar(64) DEFAULT '' COMMENT '解绑操作人',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`relation_id`),
  KEY `idx_hospital_id` (`hospital_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  UNIQUE KEY `uk_hospital_supplier` (`hospital_id`,`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院供应商关联表';
/
CREATE TABLE IF NOT EXISTS `scm_material_category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父分类ID',
  `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
  `category_code` varchar(50) DEFAULT '' COMMENT '分类编码',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材分类表';
/
CREATE TABLE IF NOT EXISTS `scm_material_dict` (
  `material_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '物资ID',
  `material_code` varchar(50) NOT NULL COMMENT '产品编码',
  `material_name` varchar(200) NOT NULL COMMENT '产品名称',
  `specification` varchar(200) DEFAULT '' COMMENT '规格',
  `model` varchar(200) DEFAULT '' COMMENT '型号',
  `unit` varchar(20) DEFAULT '' COMMENT '单位',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `manufacturer_id` bigint(20) DEFAULT NULL COMMENT '厂家ID',
  `purchase_price` decimal(18,2) DEFAULT 0 COMMENT '采购价格',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`material_id`),
  UNIQUE KEY `uk_material_code` (`material_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_manufacturer_id` (`manufacturer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资字典表';
/
CREATE TABLE IF NOT EXISTS `scm_manufacturer` (
  `manufacturer_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '厂家ID',
  `manufacturer_code` varchar(50) DEFAULT '' COMMENT '厂家编码',
  `manufacturer_name` varchar(200) NOT NULL COMMENT '厂家名称',
  `manufacturer_short_name` varchar(100) DEFAULT '' COMMENT '厂家简称',
  `contact_person` varchar(50) DEFAULT '' COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `address` varchar(500) DEFAULT '' COMMENT '地址',
  `email` varchar(100) DEFAULT '' COMMENT '邮箱',
  `website` varchar(200) DEFAULT '' COMMENT '网址',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`manufacturer_id`),
  UNIQUE KEY `uk_manufacturer_code` (`manufacturer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厂家信息表';
/
CREATE TABLE IF NOT EXISTS `scm_supplier_certificate` (
  `certificate_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '证件ID',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `certificate_type` varchar(50) NOT NULL COMMENT '证件类型',
  `certificate_name` varchar(200) DEFAULT '' COMMENT '证件名称',
  `certificate_no` varchar(100) DEFAULT '' COMMENT '证件编号',
  `issue_date` date DEFAULT NULL COMMENT '发证日期',
  `expire_date` date DEFAULT NULL COMMENT '有效期至',
  `certificate_file` varchar(500) DEFAULT '' COMMENT '证件文件路径',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态（0待审核 1已审核 2已拒绝）',
  `audit_by` varchar(64) DEFAULT '' COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT '' COMMENT '审核备注',
  `is_expired` char(1) DEFAULT '0' COMMENT '是否过期（0否 1是）',
  `is_warning` char(1) DEFAULT '0' COMMENT '是否预警（0否 1是）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`certificate_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商证件表';
/
CREATE TABLE IF NOT EXISTS `scm_product_certificate` (
  `certificate_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '证件ID',
  `material_id` bigint(20) NOT NULL COMMENT '物资ID',
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `certificate_type` varchar(50) DEFAULT '' COMMENT '证件类型',
  `certificate_name` varchar(200) DEFAULT '' COMMENT '证件名称',
  `register_name` varchar(200) DEFAULT '' COMMENT '注册证名称',
  `register_no` varchar(100) DEFAULT '' COMMENT '注册证号',
  `udi_code` varchar(100) DEFAULT '' COMMENT '条码号(UDI)',
  `pinyin_code` varchar(50) DEFAULT '' COMMENT '拼音简码',
  `register_date` date DEFAULT NULL COMMENT '注册日期',
  `register_issue_date` date DEFAULT NULL COMMENT '注册证发证日期',
  `expire_date` date DEFAULT NULL COMMENT '有效期至',
  `register_valid_date` date DEFAULT NULL COMMENT '注册有效期',
  `bid_price` decimal(18,2) DEFAULT 0 COMMENT '中标价格',
  `sale_price` decimal(18,2) DEFAULT 0 COMMENT '销售价格',
  `hospital_code` varchar(50) DEFAULT '' COMMENT '医院编码',
  `sale_customer` varchar(200) DEFAULT '' COMMENT '销售客户',
  `product_category` varchar(20) DEFAULT '' COMMENT '产品类别（高值、低值）',
  `certificate_file` varchar(500) DEFAULT '' COMMENT '证件文件路径',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态（0待审核 1已审核 2已拒绝）',
  `audit_by` varchar(64) DEFAULT '' COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT '' COMMENT '审核备注',
  `is_expired` char(1) DEFAULT '0' COMMENT '是否过期（0否 1是）',
  `is_warning` char(1) DEFAULT '0' COMMENT '是否预警（0否 1是）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`certificate_id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品证件表';
/
CREATE TABLE IF NOT EXISTS `scm_certificate_config` (
  `config_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_type` varchar(50) NOT NULL COMMENT '配置类型',
  `certificate_type` varchar(50) DEFAULT '' COMMENT '证件类型',
  `warning_days` int(11) DEFAULT 30 COMMENT '预警天数',
  `recent_days` int(11) DEFAULT 7 COMMENT '近期证件天数',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_type` (`config_type`,`certificate_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证件配置表';
/
CREATE TABLE IF NOT EXISTS `scm_certificate_type` (
  `type_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `type_code` varchar(50) DEFAULT '' COMMENT '类型编码',
  `type_name` varchar(100) NOT NULL COMMENT '类型名称',
  `type_category` varchar(50) DEFAULT '' COMMENT '类型分类（supplier供应商证件 product产品证件）',
  `description` varchar(500) DEFAULT '' COMMENT '类型描述',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`type_id`),
  UNIQUE KEY `uk_type_code` (`type_code`),
  KEY `idx_type_category` (`type_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证件类型表';
/
CREATE TABLE IF NOT EXISTS `scm_order` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `hospital_id` bigint(20) DEFAULT NULL COMMENT '医院ID',
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '订单供应商ID',
  `order_supplier_name` varchar(256) DEFAULT '' COMMENT '订单供应商名称',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '订单仓库ID',
  `warehouse_name` varchar(200) DEFAULT '' COMMENT '订单仓库名称',
  `order_dept_id` bigint(20) DEFAULT NULL COMMENT '订单科室ID',
  `order_dept_name` varchar(200) DEFAULT '' COMMENT '订单科室名称',
  `tenant_id` varchar(64) DEFAULT NULL COMMENT '租户ID',
  `order_date` date DEFAULT NULL COMMENT '订单日期',
  `order_amount` decimal(18,2) DEFAULT 0 COMMENT '订单金额',
  `order_status` char(1) DEFAULT '0' COMMENT '订单状态（0待处理 1已确认 2配送中 3已完成 4已取消）',
  `apply_dept` varchar(100) DEFAULT '' COMMENT '申请科室',
  `delivery_company` varchar(200) DEFAULT '' COMMENT '配送公司',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_hospital_id` (`hospital_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_order_date` (`order_date`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';
/
CREATE TABLE IF NOT EXISTS `scm_order_detail` (
  `detail_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `material_id` bigint(20) NOT NULL COMMENT '物资ID',
  `material_code` varchar(50) DEFAULT '' COMMENT '产品编码',
  `material_name` varchar(200) DEFAULT '' COMMENT '产品名称',
  `specification` varchar(200) DEFAULT '' COMMENT '规格',
  `model` varchar(200) DEFAULT '' COMMENT '型号',
  `unit` varchar(20) DEFAULT '' COMMENT '单位',
  `purchase_price` decimal(18,2) DEFAULT 0 COMMENT '采购价格',
  `order_quantity` decimal(18,2) DEFAULT 0 COMMENT '订货数量',
  `remaining_quantity` decimal(18,2) DEFAULT 0 COMMENT '剩余待配送数',
  `amount` decimal(18,2) DEFAULT 0 COMMENT '金额',
  `pack_coefficient` decimal(18,6) DEFAULT NULL COMMENT '打包系数',
  `manufacturer_id` bigint(20) DEFAULT NULL COMMENT '厂家ID',
  `manufacturer_name` varchar(200) DEFAULT '' COMMENT '厂家名称',
  `register_no` varchar(100) DEFAULT '' COMMENT '注册证号',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`detail_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
/
CREATE TABLE IF NOT EXISTS `scm_delivery` (
  `delivery_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配送单ID',
  `delivery_no` varchar(50) NOT NULL COMMENT '配送单号',
  `hospital_id` bigint(20) DEFAULT NULL COMMENT '医院ID',
  `warehouse_name` varchar(200) DEFAULT '' COMMENT '仓库',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单ID',
  `order_no` varchar(50) DEFAULT '' COMMENT '订单号',
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `delivery_amount` decimal(18,2) DEFAULT 0 COMMENT '配送金额',
  `delivery_status` char(1) DEFAULT '0' COMMENT '单据状态（0未审核 1已审核 2已配送 3已入库）',
  `delivery_person` varchar(50) DEFAULT '' COMMENT '配送员',
  `delivery_address` varchar(500) DEFAULT '' COMMENT '配送地址',
  `expected_delivery_date` date DEFAULT NULL COMMENT '预计配送时间',
  `invoice_no` varchar(50) DEFAULT '' COMMENT '发票号',
  `invoice_amount` decimal(18,2) DEFAULT 0 COMMENT '发票金额',
  `invoice_date` date DEFAULT NULL COMMENT '发票日期',
  `order_date` date DEFAULT NULL COMMENT '订单日期',
  `zs_order_id` varchar(36) DEFAULT NULL COMMENT '中设订单主键 zs_tp_order.id',
  `src_order_supplier_id` varchar(128) DEFAULT '' COMMENT '订单供应商ID(字符串快照)',
  `src_order_supplier_name` varchar(256) DEFAULT '' COMMENT '订单供应商名称',
  `src_order_warehouse_id` varchar(128) DEFAULT '' COMMENT '订单仓库ID(字符串快照)',
  `src_order_warehouse_name` varchar(256) DEFAULT '' COMMENT '订单仓库名称',
  `src_order_dept_id` varchar(128) DEFAULT '' COMMENT '订单科室ID(字符串快照)',
  `src_order_dept_name` varchar(256) DEFAULT '' COMMENT '订单科室名称',
  `zs_customer_id` varchar(128) DEFAULT '' COMMENT '中设客户ID(zs_tp_order.customer)',
  `zs_jsfs` varchar(32) DEFAULT NULL COMMENT '中设订单结算方式jsfs快照：3高值0低值',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态（0待审核 1已审核 2已拒绝）',
  `audit_by` varchar(64) DEFAULT '' COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT '' COMMENT '审核备注',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '制单人',
  `create_time` datetime DEFAULT NULL COMMENT '制单日期',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`delivery_id`),
  UNIQUE KEY `uk_delivery_no` (`delivery_no`),
  KEY `idx_hospital_id` (`hospital_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_zs_order_id` (`zs_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送单主表';
/
CREATE TABLE IF NOT EXISTS `scm_delivery_detail` (
  `detail_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `delivery_id` bigint(20) NOT NULL COMMENT '配送单ID',
  `order_detail_id` bigint(20) DEFAULT NULL COMMENT '订单明细ID',
  `zs_order_detail_id` varchar(36) DEFAULT NULL COMMENT '中设明细主键 zs_tp_order_detail.id',
  `material_id` bigint(20) NOT NULL COMMENT '物资ID',
  `material_code` varchar(50) DEFAULT '' COMMENT '产品编码',
  `material_name` varchar(200) DEFAULT '' COMMENT '产品名称',
  `specification` varchar(200) DEFAULT '' COMMENT '规格',
  `model` varchar(200) DEFAULT '' COMMENT '型号',
  `unit` varchar(20) DEFAULT '' COMMENT '单位',
  `remaining_quantity` decimal(18,2) DEFAULT 0 COMMENT '剩余配送数量',
  `delivery_quantity` decimal(18,2) DEFAULT 0 COMMENT '配送数量',
  `price` decimal(18,2) DEFAULT 0 COMMENT '单价',
  `amount` decimal(18,2) DEFAULT 0 COMMENT '金额',
  `pack_coefficient` decimal(18,6) DEFAULT NULL COMMENT '打包系数',
  `batch_no` varchar(50) DEFAULT '' COMMENT '批号',
  `main_barcode` varchar(128) DEFAULT '' COMMENT '主条码',
  `aux_barcode` varchar(128) DEFAULT '' COMMENT '辅条码',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `expire_date` date DEFAULT NULL COMMENT '有效期',
  `manufacturer_id` bigint(20) DEFAULT NULL COMMENT '生产厂家ID',
  `manufacturer_name` varchar(200) DEFAULT '' COMMENT '生产厂家',
  `register_no` varchar(100) DEFAULT '' COMMENT '注册证号',
  `register_expire_date` date DEFAULT NULL COMMENT '注册证有效期',
  `package_spec` varchar(200) DEFAULT '' COMMENT '包装规格',
  `in_time` datetime DEFAULT NULL COMMENT '入库时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_delivery_id` (`delivery_id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_order_detail_id` (`order_detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送明细表';
/
CREATE TABLE IF NOT EXISTS `scm_delivery_invoice` (
  `invoice_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '发票ID',
  `delivery_id` bigint(20) NOT NULL COMMENT '配送单ID',
  `invoice_no` varchar(50) NOT NULL COMMENT '发票号',
  `invoice_date` date DEFAULT NULL COMMENT '发票日期',
  `invoice_amount` decimal(18,2) DEFAULT 0 COMMENT '发票金额',
  `invoice_type` varchar(50) DEFAULT '' COMMENT '发票类型',
  `tax_amount` decimal(18,2) DEFAULT 0 COMMENT '税额',
  `total_amount` decimal(18,2) DEFAULT 0 COMMENT '价税合计',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1已作废）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`invoice_id`),
  KEY `idx_delivery_id` (`delivery_id`),
  UNIQUE KEY `uk_invoice_no` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送发票表';
/
CREATE TABLE IF NOT EXISTS `scm_settlement` (
  `settlement_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '结算单ID',
  `settlement_no` varchar(50) NOT NULL COMMENT '结算单号',
  `invoice_no` varchar(50) DEFAULT '' COMMENT '发票号',
  `invoice_date` date DEFAULT NULL COMMENT '发票日期',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID（医院ID）',
  `customer_name` varchar(200) DEFAULT '' COMMENT '客户名称',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(200) DEFAULT '' COMMENT '供应商名称',
  `delivery_company` varchar(200) DEFAULT '' COMMENT '配送商',
  `handler` varchar(50) DEFAULT '' COMMENT '经办人',
  `total_amount` decimal(18,2) DEFAULT 0 COMMENT '总金额',
  `customer_status` char(1) DEFAULT '0' COMMENT '客户结算状态（0未结算 1已结算）',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态（0待审核 1已审核 2已拒绝）',
  `audit_by` varchar(64) DEFAULT '' COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT '' COMMENT '审核备注',
  `acceptance_date` date DEFAULT NULL COMMENT '客户验收日期',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1已作废）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`settlement_id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_invoice_no` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算单主表';
/
CREATE TABLE IF NOT EXISTS `scm_settlement_detail` (
  `detail_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `settlement_id` bigint(20) NOT NULL COMMENT '结算单ID',
  `delivery_id` bigint(20) DEFAULT NULL COMMENT '配送单ID',
  `delivery_no` varchar(50) DEFAULT '' COMMENT '配送单号',
  `material_id` bigint(20) DEFAULT NULL COMMENT '物资ID',
  `material_code` varchar(50) DEFAULT '' COMMENT '产品编码',
  `material_name` varchar(200) DEFAULT '' COMMENT '产品名称',
  `specification` varchar(200) DEFAULT '' COMMENT '规格',
  `model` varchar(200) DEFAULT '' COMMENT '型号',
  `unit` varchar(20) DEFAULT '' COMMENT '单位',
  `quantity` decimal(18,2) DEFAULT 0 COMMENT '数量',
  `price` decimal(18,2) DEFAULT 0 COMMENT '单价',
  `amount` decimal(18,2) DEFAULT 0 COMMENT '金额',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_settlement_id` (`settlement_id`),
  KEY `idx_delivery_id` (`delivery_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算明细表';
/
CREATE TABLE IF NOT EXISTS `scm_purchase_statistics` (
  `statistics_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `statistics_type` char(1) NOT NULL COMMENT '统计类型（1月采购量 2年采购量）',
  `statistics_year` int(4) DEFAULT NULL COMMENT '统计年份',
  `statistics_month` int(2) DEFAULT NULL COMMENT '统计月份',
  `hospital_id` bigint(20) DEFAULT NULL COMMENT '医院ID',
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `material_id` bigint(20) DEFAULT NULL COMMENT '物资ID',
  `material_code` varchar(50) DEFAULT '' COMMENT '产品编码',
  `material_name` varchar(200) DEFAULT '' COMMENT '产品名称',
  `purchase_quantity` decimal(18,2) DEFAULT 0 COMMENT '采购数量',
  `purchase_amount` decimal(18,2) DEFAULT 0 COMMENT '采购金额',
  `order_count` int(11) DEFAULT 0 COMMENT '订单数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`statistics_id`),
  KEY `idx_statistics_type` (`statistics_type`),
  KEY `idx_statistics_year` (`statistics_year`),
  KEY `idx_statistics_month` (`statistics_month`),
  KEY `idx_hospital_id` (`hospital_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购统计表';
/
CREATE TABLE IF NOT EXISTS `scm_tenant` (
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID（UUID7）',
  `tenant_name` varchar(100) NOT NULL COMMENT '租户名称',
  `tenant_code` varchar(64) DEFAULT '' COMMENT '租户编码',
  `pinyin_code` varchar(64) DEFAULT '' COMMENT '拼音简码',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `planned_stop_time` datetime DEFAULT NULL COMMENT '计划停用时间',
  `contact_person` varchar(50) DEFAULT '' COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_by` varchar(64) DEFAULT NULL COMMENT '删除人',
  `del_time` datetime DEFAULT NULL COMMENT '删除时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`tenant_id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`),
  KEY `idx_pinyin_code` (`pinyin_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SCM租户表（客户）';
/
-- 客户实际启用/停用时间段
CREATE TABLE IF NOT EXISTS `scm_tenant_status_period` (
  `period_id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
  `status` char(1) NOT NULL COMMENT '状态（0启用 1停用）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`period_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户实际启用停用时间段';
/
-- 客户启用/停用记录
CREATE TABLE IF NOT EXISTS `scm_tenant_status_log` (
  `log_id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
  `action` char(1) NOT NULL COMMENT '动作（0启用 1停用）',
  `oper_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`log_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户启用停用记录';
/
-- 客户信息修改记录
CREATE TABLE IF NOT EXISTS `scm_tenant_modify_log` (
  `log_id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
  `field_name` varchar(64) DEFAULT '' COMMENT '字段名',
  `old_value` varchar(500) DEFAULT NULL COMMENT '原值',
  `new_value` varchar(500) DEFAULT NULL COMMENT '新值',
  `oper_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息修改记录';
/
-- 租户功能菜单授权（主键 UUID7）
CREATE TABLE IF NOT EXISTS `scm_tenant_menu` (
  `id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_menu` (`tenant_id`,`menu_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户功能菜单授权';
/
-- 客户菜单功能暂停控制
CREATE TABLE IF NOT EXISTS `scm_tenant_menu_pause` (
  `pause_id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `pause_status` char(1) NOT NULL DEFAULT '1' COMMENT '暂停状态（0正常 1暂停）',
  `pause_time` datetime DEFAULT NULL COMMENT '暂停时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`pause_id`),
  UNIQUE KEY `uk_tenant_menu` (`tenant_id`,`menu_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户菜单功能暂停';
/
-- 客户菜单暂停使用记录
CREATE TABLE IF NOT EXISTS `scm_tenant_menu_pause_log` (
  `log_id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `pause_id` varchar(36) NOT NULL COMMENT '暂停控制ID',
  `tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `action` char(1) NOT NULL COMMENT '动作（0恢复 1暂停）',
  `oper_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`log_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_pause_id` (`pause_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户菜单暂停使用记录';
/
-- ========== 代码生成表（RuoYi） ==========
/
CREATE TABLE IF NOT EXISTS `gen_table` (
  `table_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) DEFAULT 'crud' COMMENT '使用的模板（crud单表 tree树表 sub主子表）',
  `package_name` varchar(100) DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) DEFAULT NULL COMMENT '生成功能作者',
  `form_col_num` int(1) DEFAULT 1 COMMENT '表单布局（单列 双列 三列）',
  `gen_type` char(1) DEFAULT '0' COMMENT '生成代码方式（0zip 1自定义路径）',
  `gen_path` varchar(200) DEFAULT '/' COMMENT '生成路径',
  `options` varchar(1000) DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表';
/
CREATE TABLE IF NOT EXISTS `gen_table_column` (
  `column_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint(20) DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) DEFAULT 'EQ' COMMENT '查询方式',
  `html_type` varchar(200) DEFAULT NULL COMMENT '显示类型',
  `dict_type` varchar(200) DEFAULT '' COMMENT '字典类型',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表字段';
/
-- ========== Quartz 定时任务表 ==========
/
CREATE TABLE IF NOT EXISTS `qrtz_job_details` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) NOT NULL COMMENT '任务组名',
  `description` varchar(250) DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob DEFAULT NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务详细信息表';
/
CREATE TABLE IF NOT EXISTS `qrtz_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) NOT NULL COMMENT '触发器名称',
  `trigger_group` varchar(200) NOT NULL COMMENT '触发器组名',
  `job_name` varchar(200) NOT NULL COMMENT 'qrtz_job_details表job_name外键',
  `job_group` varchar(200) NOT NULL COMMENT 'qrtz_job_details表job_group外键',
  `description` varchar(250) DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint(13) DEFAULT NULL COMMENT '下一次触发时间（毫秒）',
  `prev_fire_time` bigint(13) DEFAULT NULL COMMENT '上一次触发时间',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) NOT NULL COMMENT '触发器类型',
  `start_time` bigint(13) NOT NULL COMMENT '开始时间',
  `end_time` bigint(13) DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint(2) DEFAULT NULL COMMENT '补偿执行策略',
  `job_data` blob DEFAULT NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `idx_qrtz_t_j` (`sched_name`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='触发器详细信息表';
/
CREATE TABLE IF NOT EXISTS `qrtz_simple_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) NOT NULL COMMENT '触发器名称',
  `trigger_group` varchar(200) NOT NULL COMMENT '触发器组名',
  `repeat_count` bigint(7) NOT NULL COMMENT '重复次数',
  `repeat_interval` bigint(12) NOT NULL COMMENT '重复间隔时间',
  `times_triggered` bigint(10) NOT NULL COMMENT '已触发次数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简单触发器表';
/
CREATE TABLE IF NOT EXISTS `qrtz_cron_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) NOT NULL COMMENT '触发器名称',
  `trigger_group` varchar(200) NOT NULL COMMENT '触发器组名',
  `cron_expression` varchar(200) NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Cron触发器表';
/
CREATE TABLE IF NOT EXISTS `qrtz_blob_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) NOT NULL COMMENT '触发器名称',
  `trigger_group` varchar(200) NOT NULL COMMENT '触发器组名',
  `blob_data` blob DEFAULT NULL COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Blob类型触发器表';
/
CREATE TABLE IF NOT EXISTS `qrtz_calendars` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`,`calendar_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日历信息表';
/
CREATE TABLE IF NOT EXISTS `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) NOT NULL COMMENT '触发器组名',
  PRIMARY KEY (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='暂停的触发器组表';
/
CREATE TABLE IF NOT EXISTS `qrtz_fired_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) NOT NULL COMMENT '触发器名称',
  `trigger_group` varchar(200) NOT NULL COMMENT '触发器组名',
  `instance_name` varchar(200) NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint(13) NOT NULL COMMENT '触发时间',
  `sched_time` bigint(13) NOT NULL COMMENT '定时器制定时间',
  `priority` int(11) NOT NULL COMMENT '优先级',
  `state` varchar(16) NOT NULL COMMENT '状态',
  `job_name` varchar(200) DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`,`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='已触发的触发器表';
/
CREATE TABLE IF NOT EXISTS `qrtz_scheduler_state` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint(13) NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint(13) NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`,`instance_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度器状态表';
/
CREATE TABLE IF NOT EXISTS `qrtz_locks` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`,`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储的悲观锁信息表';
/
CREATE TABLE IF NOT EXISTS `qrtz_simprop_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) NOT NULL COMMENT '触发器名称',
  `trigger_group` varchar(200) NOT NULL COMMENT '触发器组名',
  `str_prop_1` varchar(512) DEFAULT NULL COMMENT 'String参数1',
  `str_prop_2` varchar(512) DEFAULT NULL COMMENT 'String参数2',
  `str_prop_3` varchar(512) DEFAULT NULL COMMENT 'String参数3',
  `int_prop_1` int(11) DEFAULT NULL COMMENT 'int参数1',
  `int_prop_2` int(11) DEFAULT NULL COMMENT 'int参数2',
  `long_prop_1` bigint(20) DEFAULT NULL COMMENT 'long参数1',
  `long_prop_2` bigint(20) DEFAULT NULL COMMENT 'long参数2',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal参数1',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal参数2',
  `bool_prop_1` varchar(1) DEFAULT NULL COMMENT 'Boolean参数1',
  `bool_prop_2` varchar(1) DEFAULT NULL COMMENT 'Boolean参数2',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步机制行锁表';
/
-- ========== 系统表（RuoYi） ==========
/
CREATE TABLE IF NOT EXISTS `sys_config` (
  `config_id` int(5) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';
/
CREATE TABLE IF NOT EXISTS `sys_dept` (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) DEFAULT '' COMMENT '部门名称',
  `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `status` char(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';
/
CREATE TABLE IF NOT EXISTS `sys_dict_type` (
  `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';
/
CREATE TABLE IF NOT EXISTS `sys_dict_data` (
  `dict_code` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int(4) DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';
/
CREATE TABLE IF NOT EXISTS `sys_job` (
  `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) DEFAULT '3' COMMENT '计划执行错误策略（1立即 2执行一次 3放弃）',
  `concurrent` char(1) DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';
/
CREATE TABLE IF NOT EXISTS `sys_job_log` (
  `job_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) DEFAULT NULL COMMENT '日志信息',
  `status` char(1) DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度日志表';
/
CREATE TABLE IF NOT EXISTS `sys_logininfor` (
  `info_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `login_name` varchar(50) DEFAULT '' COMMENT '登录账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
  `status` char(1) DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`),
  KEY `idx_logininfor_s` (`status`),
  KEY `idx_logininfor_lt` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统访问记录';
/
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
  `url` varchar(200) DEFAULT '#' COMMENT '请求地址',
  `target` varchar(20) DEFAULT '' COMMENT '打开方式',
  `menu_type` char(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `is_refresh` char(1) DEFAULT '1' COMMENT '是否刷新（0刷新 1不刷新）',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';
/
CREATE TABLE IF NOT EXISTS `sys_notice` (
  `notice_id` int(4) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) NOT NULL COMMENT '公告标题',
  `notice_type` char(1) NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob DEFAULT NULL COMMENT '公告内容',
  `status` char(1) DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';
/
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
  `oper_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int(2) DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `operator_type` int(1) DEFAULT 0 COMMENT '操作类别（0其它 1后台 2手机端）',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status` int(1) DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint(20) DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`),
  KEY `idx_oper_log_bt` (`business_type`),
  KEY `idx_oper_log_s` (`status`),
  KEY `idx_oper_log_ot` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';
/
CREATE TABLE IF NOT EXISTS `sys_post` (
  `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) NOT NULL COMMENT '岗位名称',
  `post_sort` int(4) NOT NULL COMMENT '显示顺序',
  `status` char(1) NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位信息表';
/
CREATE TABLE IF NOT EXISTS `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(4) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) DEFAULT '1' COMMENT '数据范围（1全部 2自定义 3本部门 4本部门及以下）',
  `status` char(1) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';
/
CREATE TABLE IF NOT EXISTS `sys_role_dept` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和部门关联表';
/
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';
/
CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `login_name` varchar(30) NOT NULL COMMENT '登录账号',
  `user_name` varchar(30) DEFAULT '' COMMENT '用户昵称',
  `user_type` varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户 01注册用户）',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) DEFAULT '' COMMENT '头像路径',
  `password` varchar(50) DEFAULT '' COMMENT '密码',
  `salt` varchar(20) DEFAULT '' COMMENT '盐加密',
  `status` char(1) DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
/
CREATE TABLE IF NOT EXISTS `sys_user_online` (
  `sessionId` varchar(50) NOT NULL COMMENT '用户会话id',
  `login_name` varchar(50) DEFAULT '' COMMENT '登录账号',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `ipaddr` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
  `status` varchar(10) DEFAULT '' COMMENT '在线状态on_line/off_line',
  `start_timestamp` datetime DEFAULT NULL COMMENT 'session创建时间',
  `last_access_time` datetime DEFAULT NULL COMMENT 'session最后访问时间',
  `expire_time` int(5) DEFAULT 0 COMMENT '超时时间（分钟）',
  PRIMARY KEY (`sessionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线用户记录';
/
CREATE TABLE IF NOT EXISTS `sys_user_post` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与岗位关联表';
/
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';
/

CREATE TABLE IF NOT EXISTS zs_tp_order (
  id              VARCHAR(36)  NOT NULL COMMENT '本系统主键 UUID7',
  third_party_pk  VARCHAR(128) NOT NULL COMMENT '第三方系统主键（如推送 JSON 中行键 1、2）',
  customer        VARCHAR(64)  NOT NULL COMMENT '第三方服务标识，对应 CUSTOMER',
  sheet_je        DECIMAL(24, 8) NULL COMMENT 'SHEET_JE',
  dh              VARCHAR(128) NULL COMMENT '单号 DH',
  supno           VARCHAR(64)  NULL,
  sup             VARCHAR(256) NULL,
  supno2          VARCHAR(64)  NULL,
  sup2            VARCHAR(256) NULL,
  ckno            VARCHAR(64)  NULL,
  ck              VARCHAR(128) NULL,
  pc              VARCHAR(128) NULL,
  oper            VARCHAR(64)  NULL,
  bz              VARCHAR(512) NULL,
  jsfs            VARCHAR(32)  NULL,
  receive_channel VARCHAR(16)  NOT NULL DEFAULT 'ZS' COMMENT '接收渠道：TENANT=我方推送 ZS=中设客户推送',
  ksbh            VARCHAR(64)  NULL,
  ksmc            VARCHAR(128) NULL,
  zjly            VARCHAR(128) NULL,
  create_by       VARCHAR(64)  NULL,
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by       VARCHAR(64)  NULL,
  update_time     DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
  del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  del_by          VARCHAR(64)  NULL,
  del_time        DATETIME     NULL,
  PRIMARY KEY (id),
  KEY idx_zs_tp_order_customer_dh (customer, dh),
  KEY idx_zs_tp_order_del (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ZS第三方推送订单主表';
/
CREATE TABLE IF NOT EXISTS zs_tp_order_detail (
  id              VARCHAR(36)  NOT NULL COMMENT '本系统主键 UUID7',
  order_id        VARCHAR(36)  NOT NULL COMMENT '主表 zs_tp_order.id',
  third_party_pk  VARCHAR(128) NOT NULL COMMENT '第三方明细行主键（如 1、2、3）',
  dh              VARCHAR(128) NULL,
  code            VARCHAR(128) NULL,
  name            VARCHAR(512) NULL,
  gg              VARCHAR(256) NULL,
  dw              VARCHAR(32)  NULL,
  bzl             VARCHAR(256) NULL,
  sccj            VARCHAR(256) NULL,
  zcz             VARCHAR(128) NULL,
  phflag          VARCHAR(64)  NULL,
  sl              DECIMAL(24, 8) NULL,
  dj              DECIMAL(24, 8) NULL,
  je              DECIMAL(24, 8) NULL,
  jm              VARCHAR(128) NULL,
  cgj             VARCHAR(32)  NULL,
  bz              VARCHAR(512) NULL,
  bz1             VARCHAR(512) NULL,
  bz2             VARCHAR(512) NULL,
  dsb             DECIMAL(24, 8) NULL,
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
  del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  del_by          VARCHAR(64)  NULL,
  del_time        DATETIME     NULL,
  PRIMARY KEY (id),
  KEY idx_zs_tp_detail_order (order_id),
  KEY idx_zs_tp_detail_del (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ZS第三方推送订单明细';
/
CREATE TABLE IF NOT EXISTS `scm_order_detail_delivery_rel` (
  `id`                  VARCHAR(36)  NOT NULL COMMENT '主键UUID7',
  `order_detail_id`     VARCHAR(64)  NOT NULL COMMENT '订单明细ID',
  `order_id`            VARCHAR(64)  NOT NULL COMMENT '订单ID',
  `order_no`            VARCHAR(128) DEFAULT '' COMMENT '订单号',
  `delivery_id`         VARCHAR(64)  NOT NULL COMMENT '配送单ID',
  `delivery_no`         VARCHAR(128) DEFAULT '' COMMENT '配送单号',
  `delivery_detail_id`  VARCHAR(64)  NOT NULL COMMENT '配送单明细ID',
  `create_time`         VARCHAR(32)  DEFAULT NULL COMMENT '添加时间',
  `create_by`           VARCHAR(64)  DEFAULT NULL COMMENT '添加人ID',
  `tenant_id`           VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_soddr_order_detail` (`order_detail_id`),
  KEY `idx_soddr_order` (`order_id`),
  KEY `idx_soddr_delivery` (`delivery_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='我方订单明细与配送单明细关联';
/
CREATE TABLE IF NOT EXISTS `zs_tp_order_detail_delivery_rel` (
  `id`                  VARCHAR(36)  NOT NULL COMMENT '主键UUID7',
  `order_detail_id`     VARCHAR(64)  NOT NULL COMMENT '中设订单明细ID',
  `order_id`            VARCHAR(64)  NOT NULL COMMENT '中设订单ID',
  `order_no`            VARCHAR(128) DEFAULT '' COMMENT '订单号(DH)',
  `delivery_id`         VARCHAR(64)  NOT NULL COMMENT '配送单ID',
  `delivery_no`         VARCHAR(128) DEFAULT '' COMMENT '配送单号',
  `delivery_detail_id`  VARCHAR(64)  NOT NULL COMMENT '配送单明细ID',
  `create_time`         VARCHAR(32)  DEFAULT NULL COMMENT '添加时间',
  `create_by`           VARCHAR(64)  DEFAULT NULL COMMENT '添加人ID',
  `tenant_id`           VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_zsoddr_order_detail` (`order_detail_id`),
  KEY `idx_zsoddr_order` (`order_id`),
  KEY `idx_zsoddr_delivery` (`delivery_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中设订单明细与配送单明细关联';
/
CREATE TABLE IF NOT EXISTS `scm_barcode_seed` (
  `id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `counter_type` char(1) NOT NULL COMMENT 'T=按租户维度 Z=按中设客户维度',
  `tenant_id` varchar(64) NOT NULL DEFAULT '' COMMENT '租户ID',
  `zs_customer_id` varchar(128) NOT NULL DEFAULT '' COMMENT '中设客户ID(customer)',
  `warehouse_id` varchar(128) NOT NULL DEFAULT '' COMMENT '仓库ID；中设订单种子暂固定空串仅按高低值区分，保留列便于将来按仓扩展',
  `high_low_flag` char(1) NOT NULL DEFAULT 'L' COMMENT '高低值：H高值 L低值（中设JSFS：3高0低）',
  `seed_value` bigint(20) NOT NULL DEFAULT 0 COMMENT '已分配的最大种子序号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scm_barcode_seed` (`counter_type`,`tenant_id`,`zs_customer_id`,`warehouse_id`,`high_low_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中设条码种子序列表';
/
CREATE TABLE IF NOT EXISTS `scm_delivery_detail_barcode` (
  `id` varchar(36) NOT NULL COMMENT '主键UUID7',
  `delivery_id` bigint(20) NOT NULL COMMENT '配送单ID',
  `delivery_no` varchar(50) NOT NULL DEFAULT '' COMMENT '配送单号',
  `delivery_detail_id` bigint(20) NOT NULL COMMENT '配送单明细ID',
  `seed_num` bigint(20) NOT NULL COMMENT '种子序号',
  `barcode_no` varchar(128) NOT NULL DEFAULT '' COMMENT '条码号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ddbc_delivery` (`delivery_id`),
  KEY `idx_ddbc_detail` (`delivery_detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送单明细条码从表';
/