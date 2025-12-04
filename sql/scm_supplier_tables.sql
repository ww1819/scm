-- ----------------------------
-- 供应商管理平台 - 供应商相关表
-- ----------------------------

-- ----------------------------
-- 1、供应商信息表
-- ----------------------------
drop table if exists scm_supplier;
create table scm_supplier (
  supplier_id         bigint(20)      not null auto_increment    comment '供应商ID',
  supplier_code      varchar(50)     default ''                 comment '供应商编码',
  company_name       varchar(200)    not null                   comment '公司名称',
  company_short_name varchar(100)    default ''                 comment '公司简称',
  legal_person       varchar(50)     default ''                 comment '法人',
  registered_capital  decimal(18,2)   default 0                   comment '注册资金',
  province           varchar(50)     default ''                 comment '省份/直辖市',
  city               varchar(50)     default ''                 comment '城市',
  district           varchar(50)     default ''                 comment '县级/区',
  address            varchar(500)    default ''                 comment '详细联系地址',
  business_scope     varchar(1000)   default ''                 comment '经营范围',
  email              varchar(100)    default ''                 comment '邮箱',
  website            varchar(200)    default ''                 comment '网址',
  contact_person     varchar(50)     default ''                 comment '联系人',
  contact_phone      varchar(20)     default ''                 comment '联系电话',
  status             char(1)         default '0'                comment '状态（0待审核 1正常 2停用）',
  audit_status       char(1)         default '0'                comment '审核状态（0待审核 1已审核 2已拒绝）',
  audit_by           varchar(64)     default ''                 comment '审核人',
  audit_time         datetime                                   comment '审核时间',
  audit_remark      varchar(500)    default ''                 comment '审核备注',
  del_flag           char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (supplier_id),
  unique key uk_supplier_code (supplier_code)
) engine=innodb auto_increment=1 comment = '供应商信息表';

-- ----------------------------
-- 2、供应商用户表
-- ----------------------------
drop table if exists scm_supplier_user;
create table scm_supplier_user (
  supplier_user_id   bigint(20)      not null auto_increment    comment '供应商用户ID',
  supplier_id        bigint(20)      not null                   comment '供应商ID',
  user_id            bigint(20)      not null                   comment '用户ID',
  is_main            char(1)         default '0'                comment '是否主账号（0否 1是）',
  status             char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  primary key (supplier_user_id),
  key idx_supplier_id (supplier_id),
  key idx_user_id (user_id)
) engine=innodb auto_increment=1 comment = '供应商用户表';

-- ----------------------------
-- 3、供应商编码对照表
-- ----------------------------
drop table if exists scm_supplier_code_mapping;
create table scm_supplier_code_mapping (
  mapping_id         bigint(20)      not null auto_increment    comment '对照ID',
  supplier_id        bigint(20)      not null                   comment '供应商ID',
  hospital_id       bigint(20)      not null                   comment '医院ID',
  supplier_code     varchar(50)     default ''                 comment '供应商编码',
  hospital_code     varchar(50)     default ''                 comment '医院编码',
  status             char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (mapping_id),
  key idx_supplier_id (supplier_id),
  key idx_hospital_id (hospital_id)
) engine=innodb auto_increment=1 comment = '供应商编码对照表';

