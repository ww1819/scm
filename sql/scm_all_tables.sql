-- ----------------------------
-- 供应商管理平台 - 所有表结构
-- ----------------------------

-- ----------------------------
-- 供应商相关表
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

-- ----------------------------
-- 医院相关表
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

-- ----------------------------
-- 物资相关表
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

-- ----------------------------
-- 资质证件表
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

-- ----------------------------
-- 订单相关表
-- ----------------------------
drop table if exists scm_order;
create table scm_order (
  order_id           bigint(20)      not null auto_increment    comment '订单ID',
  order_no           varchar(50)     not null                   comment '订单编号',
  hospital_id        bigint(20)      not null                   comment '医院ID',
  supplier_id        bigint(20)      not null                   comment '供应商ID',
  warehouse_name     varchar(200)    default ''                 comment '要货仓库',
  order_date         date                                       comment '订单日期',
  order_amount       decimal(18,2)   default 0                  comment '订单金额',
  order_status       char(1)         default '0'                comment '订单状态（0待处理 1已确认 2配送中 3已完成 4已取消）',
  apply_dept         varchar(100)    default ''                 comment '申请科室',
  delivery_company   varchar(200)    default ''                 comment '配送公司',
  remark             varchar(500)    default ''                 comment '备注',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  primary key (order_id),
  unique key uk_order_no (order_no),
  key idx_hospital_id (hospital_id),
  key idx_supplier_id (supplier_id),
  key idx_order_date (order_date)
) engine=innodb auto_increment=1 comment = '订单主表';

drop table if exists scm_order_detail;
create table scm_order_detail (
  detail_id          bigint(20)      not null auto_increment    comment '明细ID',
  order_id           bigint(20)      not null                   comment '订单ID',
  material_id        bigint(20)      not null                   comment '物资ID',
  material_code      varchar(50)     default ''                 comment '产品编码',
  material_name      varchar(200)    default ''                 comment '产品名称',
  specification      varchar(200)    default ''                 comment '规格',
  model              varchar(200)    default ''                 comment '型号',
  unit               varchar(20)     default ''                 comment '单位',
  purchase_price     decimal(18,2)   default 0                  comment '采购价格',
  order_quantity     decimal(18,2)   default 0                  comment '订货数量',
  remaining_quantity decimal(18,2)   default 0                  comment '剩余待配送数',
  amount             decimal(18,2)   default 0                  comment '金额',
  manufacturer_id    bigint(20)      default null               comment '厂家ID',
  manufacturer_name  varchar(200)    default ''                 comment '厂家名称',
  register_no        varchar(100)    default ''                 comment '注册证号',
  remark             varchar(500)    default ''                 comment '备注',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  primary key (detail_id),
  key idx_order_id (order_id),
  key idx_material_id (material_id)
) engine=innodb auto_increment=1 comment = '订单明细表';

-- ----------------------------
-- 配送相关表
-- ----------------------------
drop table if exists scm_delivery;
create table scm_delivery (
  delivery_id        bigint(20)      not null auto_increment    comment '配送单ID',
  delivery_no        varchar(50)     not null                   comment '配送单号',
  hospital_id        bigint(20)      not null                   comment '医院ID',
  warehouse_name     varchar(200)    default ''                 comment '仓库',
  order_id           bigint(20)      default null               comment '订单ID',
  order_no           varchar(50)     default ''                 comment '订单号',
  supplier_id        bigint(20)      not null                   comment '供应商ID',
  delivery_amount    decimal(18,2)   default 0                  comment '配送金额',
  delivery_status    char(1)         default '0'                comment '单据状态（0未审核 1已审核 2已配送 3已入库）',
  delivery_person    varchar(50)     default ''                 comment '配送员',
  delivery_address   varchar(500)    default ''                 comment '配送地址',
  expected_delivery_date date                                   comment '预计配送时间',
  invoice_no         varchar(50)     default ''                 comment '发票号',
  invoice_amount    decimal(18,2)   default 0                  comment '发票金额',
  invoice_date       date                                       comment '发票日期',
  order_date         date                                       comment '订单日期',
  remark             varchar(500)    default ''                 comment '备注',
  create_by          varchar(64)     default ''                 comment '制单人',
  create_time        datetime                                   comment '制单日期',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  primary key (delivery_id),
  unique key uk_delivery_no (delivery_no),
  key idx_hospital_id (hospital_id),
  key idx_supplier_id (supplier_id),
  key idx_order_id (order_id)
) engine=innodb auto_increment=1 comment = '配送单主表';

drop table if exists scm_delivery_detail;
create table scm_delivery_detail (
  detail_id          bigint(20)      not null auto_increment    comment '明细ID',
  delivery_id        bigint(20)      not null                   comment '配送单ID',
  order_detail_id    bigint(20)      default null               comment '订单明细ID',
  material_id        bigint(20)      not null                   comment '物资ID',
  material_code      varchar(50)     default ''                 comment '产品编码',
  material_name      varchar(200)    default ''                 comment '产品名称',
  specification      varchar(200)    default ''                 comment '规格',
  model              varchar(200)    default ''                 comment '型号',
  unit               varchar(20)     default ''                 comment '单位',
  remaining_quantity decimal(18,2)   default 0                  comment '剩余配送数量',
  delivery_quantity  decimal(18,2)   default 0                  comment '配送数量',
  price              decimal(18,2)   default 0                  comment '单价',
  amount             decimal(18,2)   default 0                  comment '金额',
  batch_no           varchar(50)     default ''                 comment '批号',
  production_date    date                                       comment '生产日期',
  expire_date        date                                       comment '有效期',
  manufacturer_id    bigint(20)      default null               comment '生产厂家ID',
  manufacturer_name  varchar(200)    default ''                 comment '生产厂家',
  register_no        varchar(100)    default ''                 comment '注册证号',
  register_expire_date date                                     comment '注册证有效期',
  package_spec       varchar(200)    default ''                 comment '包装规格',
  in_time            datetime                                   comment '入库时间',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default ''                 comment '备注',
  primary key (detail_id),
  key idx_delivery_id (delivery_id),
  key idx_material_id (material_id),
  key idx_order_detail_id (order_detail_id)
) engine=innodb auto_increment=1 comment = '配送明细表';

drop table if exists scm_delivery_invoice;
create table scm_delivery_invoice (
  invoice_id         bigint(20)      not null auto_increment    comment '发票ID',
  delivery_id        bigint(20)      not null                   comment '配送单ID',
  invoice_no         varchar(50)     not null                   comment '发票号',
  invoice_date       date                                       comment '发票日期',
  invoice_amount     decimal(18,2)   default 0                  comment '发票金额',
  invoice_type       varchar(50)     default ''                 comment '发票类型',
  tax_amount         decimal(18,2)   default 0                  comment '税额',
  total_amount       decimal(18,2)   default 0                  comment '价税合计',
  status             char(1)         default '0'                comment '状态（0正常 1已作废）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (invoice_id),
  key idx_delivery_id (delivery_id),
  unique key uk_invoice_no (invoice_no)
) engine=innodb auto_increment=1 comment = '配送发票表';

-- ----------------------------
-- 结算相关表
-- ----------------------------
drop table if exists scm_settlement;
create table scm_settlement (
  settlement_id     bigint(20)      not null auto_increment    comment '结算单ID',
  settlement_no      varchar(50)     not null                   comment '结算单号',
  invoice_no         varchar(50)     default ''                 comment '发票号',
  invoice_date       date                                       comment '发票日期',
  customer_id        bigint(20)      not null                   comment '客户ID（医院ID）',
  customer_name      varchar(200)    default ''                 comment '客户名称',
  supplier_id        bigint(20)      not null                   comment '供应商ID',
  supplier_name      varchar(200)    default ''                 comment '供应商名称',
  delivery_company   varchar(200)    default ''                 comment '配送商',
  handler            varchar(50)     default ''                 comment '经办人',
  total_amount       decimal(18,2)   default 0                  comment '总金额',
  customer_status    char(1)         default '0'                comment '客户结算状态（0未结算 1已结算）',
  audit_status       char(1)         default '0'                comment '审核状态（0待审核 1已审核 2已拒绝）',
  audit_by           varchar(64)     default ''                 comment '审核人',
  audit_time         datetime                                   comment '审核时间',
  audit_remark      varchar(500)    default ''                 comment '审核备注',
  acceptance_date    date                                       comment '客户验收日期',
  status             char(1)         default '0'                comment '状态（0正常 1已作废）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (settlement_id),
  unique key uk_settlement_no (settlement_no),
  key idx_customer_id (customer_id),
  key idx_supplier_id (supplier_id),
  key idx_invoice_no (invoice_no)
) engine=innodb auto_increment=1 comment = '结算单主表';

drop table if exists scm_settlement_detail;
create table scm_settlement_detail (
  detail_id          bigint(20)      not null auto_increment    comment '明细ID',
  settlement_id      bigint(20)      not null                   comment '结算单ID',
  delivery_id        bigint(20)      default null               comment '配送单ID',
  delivery_no        varchar(50)     default ''                 comment '配送单号',
  material_id        bigint(20)      default null               comment '物资ID',
  material_code      varchar(50)     default ''                 comment '产品编码',
  material_name      varchar(200)    default ''                 comment '产品名称',
  specification      varchar(200)    default ''                 comment '规格',
  model              varchar(200)    default ''                 comment '型号',
  unit               varchar(20)     default ''                 comment '单位',
  quantity           decimal(18,2)   default 0                  comment '数量',
  price              decimal(18,2)   default 0                  comment '单价',
  amount              decimal(18,2)   default 0                  comment '金额',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (detail_id),
  key idx_settlement_id (settlement_id),
  key idx_delivery_id (delivery_id)
) engine=innodb auto_increment=1 comment = '结算明细表';

-- ----------------------------
-- 数据中心表
-- ----------------------------
drop table if exists scm_purchase_statistics;
create table scm_purchase_statistics (
  statistics_id      bigint(20)      not null auto_increment    comment '统计ID',
  statistics_type    char(1)         not null                   comment '统计类型（1月采购量 2年采购量）',
  statistics_year    int(4)          default null               comment '统计年份',
  statistics_month   int(2)          default null               comment '统计月份',
  hospital_id        bigint(20)      default null               comment '医院ID',
  supplier_id        bigint(20)      default null               comment '供应商ID',
  material_id        bigint(20)      default null               comment '物资ID',
  material_code      varchar(50)     default ''                 comment '产品编码',
  material_name      varchar(200)    default ''                 comment '产品名称',
  purchase_quantity  decimal(18,2)   default 0                  comment '采购数量',
  purchase_amount    decimal(18,2)   default 0                  comment '采购金额',
  order_count        int(11)         default 0                  comment '订单数量',
  create_time        datetime                                   comment '创建时间',
  update_time        datetime                                   comment '更新时间',
  primary key (statistics_id),
  key idx_statistics_type (statistics_type),
  key idx_statistics_year (statistics_year),
  key idx_statistics_month (statistics_month),
  key idx_hospital_id (hospital_id),
  key idx_supplier_id (supplier_id),
  key idx_material_id (material_id)
) engine=innodb auto_increment=1 comment = '采购统计表';

