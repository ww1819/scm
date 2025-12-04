-- ----------------------------
-- 供应商管理平台 - 医院相关表
-- ----------------------------

-- ----------------------------
-- 1、医院信息表
-- ----------------------------
drop table if exists scm_hospital;
create table scm_hospital (
  hospital_id        bigint(20)      not null auto_increment    comment '医院ID',
  hospital_code      varchar(50)     default ''                 comment '医院编码',
  hospital_name     varchar(200)    not null                   comment '医院名称',
  hospital_short_name varchar(100)   default ''                 comment '医院简称',
  hospital_level     varchar(50)     default ''                 comment '医院等级',
  province           varchar(50)     default ''                 comment '省份/直辖市',
  city               varchar(50)     default ''                 comment '城市',
  district           varchar(50)     default ''                 comment '县级/区',
  address            varchar(500)    default ''                 comment '详细地址',
  contact_person     varchar(50)     default ''                 comment '联系人',
  contact_phone      varchar(20)     default ''                 comment '联系电话',
  email              varchar(100)    default ''                 comment '邮箱',
  status             char(1)         default '0'                comment '状态（0正常 1停用）',
  del_flag           char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (hospital_id),
  unique key uk_hospital_code (hospital_code)
) engine=innodb auto_increment=1 comment = '医院信息表';

-- ----------------------------
-- 2、医院用户表
-- ----------------------------
drop table if exists scm_hospital_user;
create table scm_hospital_user (
  hospital_user_id   bigint(20)      not null auto_increment    comment '医院用户ID',
  hospital_id        bigint(20)      not null                   comment '医院ID',
  user_id            bigint(20)      not null                   comment '用户ID',
  is_main            char(1)         default '0'                comment '是否主账号（0否 1是）',
  status             char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  primary key (hospital_user_id),
  key idx_hospital_id (hospital_id),
  key idx_user_id (user_id)
) engine=innodb auto_increment=1 comment = '医院用户表';

-- ----------------------------
-- 3、医院供应商关联表
-- ----------------------------
drop table if exists scm_hospital_supplier;
create table scm_hospital_supplier (
  relation_id        bigint(20)      not null auto_increment    comment '关联ID',
  hospital_id        bigint(20)      not null                   comment '医院ID',
  supplier_id        bigint(20)      not null                   comment '供应商ID',
  bind_status        char(1)         default '0'                comment '绑定状态（0待审核 1已绑定 2已解绑）',
  bind_time          datetime                                   comment '绑定时间',
  bind_by            varchar(64)     default ''                 comment '绑定操作人',
  unbind_time        datetime                                   comment '解绑时间',
  unbind_by          varchar(64)     default ''                 comment '解绑操作人',
  status             char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (relation_id),
  key idx_hospital_id (hospital_id),
  key idx_supplier_id (supplier_id),
  unique key uk_hospital_supplier (hospital_id, supplier_id)
) engine=innodb auto_increment=1 comment = '医院供应商关联表';

