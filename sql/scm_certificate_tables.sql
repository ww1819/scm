-- ----------------------------
-- 供应商管理平台 - 资质证件表
-- ----------------------------

-- ----------------------------
-- 1、供应商证件表
-- ----------------------------
drop table if exists scm_supplier_certificate;
create table scm_supplier_certificate (
  certificate_id     bigint(20)      not null auto_increment    comment '证件ID',
  supplier_id        bigint(20)      not null                   comment '供应商ID',
  certificate_type   varchar(50)     not null                   comment '证件类型（营业执照、经营许可证等）',
  certificate_name   varchar(200)    default ''                 comment '证件名称',
  certificate_no     varchar(100)    default ''                 comment '证件编号',
  issue_date         date                                       comment '发证日期',
  expire_date        date                                       comment '有效期至',
  certificate_file   varchar(500)    default ''                 comment '证件文件路径',
  audit_status       char(1)         default '0'                comment '审核状态（0待审核 1已审核 2已拒绝）',
  audit_by           varchar(64)     default ''                 comment '审核人',
  audit_time         datetime                                   comment '审核时间',
  audit_remark      varchar(500)    default ''                 comment '审核备注',
  is_expired         char(1)         default '0'                comment '是否过期（0否 1是）',
  is_warning         char(1)         default '0'                comment '是否预警（0否 1是）',
  status             char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (certificate_id),
  key idx_supplier_id (supplier_id),
  key idx_expire_date (expire_date)
) engine=innodb auto_increment=1 comment = '供应商证件表';

-- ----------------------------
-- 2、产品证件表
-- ----------------------------
drop table if exists scm_product_certificate;
create table scm_product_certificate (
  certificate_id     bigint(20)      not null auto_increment    comment '证件ID',
  material_id        bigint(20)      not null                   comment '物资ID',
  supplier_id        bigint(20)      not null                   comment '供应商ID',
  certificate_type   varchar(50)     default ''                 comment '证件类型',
  certificate_name   varchar(200)    default ''                 comment '证件名称',
  register_no        varchar(100)    default ''                 comment '注册证号',
  register_date      date                                       comment '注册日期',
  expire_date        date                                       comment '有效期至',
  certificate_file   varchar(500)    default ''                 comment '证件文件路径',
  audit_status       char(1)         default '0'                comment '审核状态（0待审核 1已审核 2已拒绝）',
  audit_by           varchar(64)     default ''                 comment '审核人',
  audit_time         datetime                                   comment '审核时间',
  audit_remark      varchar(500)    default ''                 comment '审核备注',
  is_expired         char(1)         default '0'                comment '是否过期（0否 1是）',
  is_warning         char(1)         default '0'                comment '是否预警（0否 1是）',
  status             char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (certificate_id),
  key idx_material_id (material_id),
  key idx_supplier_id (supplier_id),
  key idx_expire_date (expire_date)
) engine=innodb auto_increment=1 comment = '产品证件表';

-- ----------------------------
-- 3、证件配置表
-- ----------------------------
drop table if exists scm_certificate_config;
create table scm_certificate_config (
  config_id          bigint(20)      not null auto_increment    comment '配置ID',
  config_type         varchar(50)     not null                   comment '配置类型（supplier_certificate供应商证件 product_certificate产品证件）',
  certificate_type   varchar(50)     default ''                 comment '证件类型',
  warning_days        int(11)         default 30                 comment '预警天数',
  recent_days         int(11)         default 7                  comment '近期证件天数',
  status              char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (config_id),
  unique key uk_config_type (config_type, certificate_type)
) engine=innodb auto_increment=1 comment = '证件配置表';

