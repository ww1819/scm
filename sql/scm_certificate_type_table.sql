-- ----------------------------
-- 供应商管理平台 - 证件类型表
-- ----------------------------

-- 证件类型表
drop table if exists scm_certificate_type;
create table scm_certificate_type (
  type_id            bigint(20)      not null auto_increment    comment '类型ID',
  type_code          varchar(50)     default ''                 comment '类型编码',
  type_name          varchar(100)    not null                   comment '类型名称',
  type_category      varchar(50)     default ''                 comment '类型分类（supplier供应商证件 product产品证件）',
  description        varchar(500)    default ''                 comment '类型描述',
  status             char(1)         default '0'                comment '状态（0正常 1停用）',
  order_num          int(4)          default 0                  comment '显示顺序',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (type_id),
  unique key uk_type_code (type_code),
  key idx_type_category (type_category)
) engine=innodb auto_increment=1 comment = '证件类型表';

-- 插入初始数据
insert into scm_certificate_type(type_code, type_name, type_category, description, order_num, status, create_by, create_time) values
('SUPPLIER_001', '营业执照', 'supplier', '供应商营业执照', 1, '0', 'admin', sysdate()),
('SUPPLIER_002', '经营许可证', 'supplier', '供应商经营许可证', 2, '0', 'admin', sysdate()),
('SUPPLIER_003', '三证', 'supplier', '营业执照、组织机构代码证、税务登记证', 3, '0', 'admin', sysdate()),
('PRODUCT_001', '医疗器械注册证', 'product', '产品医疗器械注册证', 1, '0', 'admin', sysdate()),
('PRODUCT_002', '生产许可证', 'product', '产品生产许可证', 2, '0', 'admin', sysdate());

