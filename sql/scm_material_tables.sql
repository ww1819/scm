-- ----------------------------
-- 供应商管理平台 - 物资相关表
-- ----------------------------

-- ----------------------------
-- 1、耗材分类表（树形结构）
-- ----------------------------
drop table if exists scm_material_category;
create table scm_material_category (
  category_id         bigint(20)      not null auto_increment    comment '分类ID',
  parent_id           bigint(20)      default 0                  comment '父分类ID',
  ancestors           varchar(500)    default ''                 comment '祖级列表',
  category_code       varchar(50)     default ''                 comment '分类编码',
  category_name       varchar(100)    not null                   comment '分类名称',
  order_num           int(4)          default 0                  comment '显示顺序',
  status              char(1)         default '0'                comment '状态（0正常 1停用）',
  del_flag            char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (category_id),
  key idx_parent_id (parent_id)
) engine=innodb auto_increment=1 comment = '耗材分类表';

-- ----------------------------
-- 2、物资字典表
-- ----------------------------
drop table if exists scm_material_dict;
create table scm_material_dict (
  material_id         bigint(20)      not null auto_increment    comment '物资ID',
  material_code       varchar(50)     not null                   comment '产品编码',
  material_name       varchar(200)    not null                   comment '产品名称',
  specification       varchar(200)    default ''                 comment '规格',
  model               varchar(200)    default ''                 comment '型号',
  unit                varchar(20)     default ''                 comment '单位',
  category_id         bigint(20)      default null               comment '分类ID',
  manufacturer_id     bigint(20)      default null               comment '厂家ID',
  purchase_price      decimal(18,2)   default 0                  comment '采购价格',
  status              char(1)         default '0'                comment '状态（0正常 1停用）',
  del_flag            char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (material_id),
  unique key uk_material_code (material_code),
  key idx_category_id (category_id),
  key idx_manufacturer_id (manufacturer_id)
) engine=innodb auto_increment=1 comment = '物资字典表';

-- ----------------------------
-- 3、厂家信息表
-- ----------------------------
drop table if exists scm_manufacturer;
create table scm_manufacturer (
  manufacturer_id     bigint(20)      not null auto_increment    comment '厂家ID',
  manufacturer_code   varchar(50)     default ''                 comment '厂家编码',
  manufacturer_name   varchar(200)    not null                   comment '厂家名称',
  manufacturer_short_name varchar(100) default ''                 comment '厂家简称',
  contact_person      varchar(50)     default ''                 comment '联系人',
  contact_phone       varchar(20)     default ''                 comment '联系电话',
  address             varchar(500)    default ''                 comment '地址',
  email               varchar(100)    default ''                 comment '邮箱',
  website             varchar(200)    default ''                 comment '网址',
  status              char(1)         default '0'                comment '状态（0正常 1停用）',
  del_flag            char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (manufacturer_id),
  unique key uk_manufacturer_code (manufacturer_code)
) engine=innodb auto_increment=1 comment = '厂家信息表';

