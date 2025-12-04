-- ----------------------------
-- 供应商管理平台 - 订单相关表
-- ----------------------------

-- ----------------------------
-- 1、订单主表
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

-- ----------------------------
-- 2、订单明细表
-- ----------------------------
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

