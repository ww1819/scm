-- ----------------------------
-- 供应商管理平台 - 配送相关表
-- ----------------------------

-- ----------------------------
-- 1、配送单主表
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

-- ----------------------------
-- 2、配送明细表
-- ----------------------------
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

-- ----------------------------
-- 3、配送发票表
-- ----------------------------
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

