-- ----------------------------
-- 供应商管理平台 - 数据中心表
-- ----------------------------

-- ----------------------------
-- 1、采购统计表
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

