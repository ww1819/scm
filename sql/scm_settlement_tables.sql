-- ----------------------------
-- 供应商管理平台 - 结算相关表
-- ----------------------------

-- ----------------------------
-- 1、结算单主表
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

-- ----------------------------
-- 2、结算明细表
-- ----------------------------
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

