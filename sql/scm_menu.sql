-- ----------------------------
-- 供应商管理平台 - 菜单权限配置
-- ----------------------------

-- 一级菜单 - 供应商管理
insert into sys_menu values('2000', '供应商管理', '0', '5', '#', '', 'M', '0', '1', '', 'fa fa-truck', 'admin', sysdate(), '', null, '供应商管理目录');
-- 二级菜单
insert into sys_menu values('2001', '供应商维护', '2000', '1', '/supplier/supplier', '', 'C', '0', '1', 'supplier:supplier:view', 'fa fa-building', 'admin', sysdate(), '', null, '供应商维护菜单');
insert into sys_menu values('2002', '企业用户维护', '2000', '2', '/supplier/user', '', 'C', '0', '1', 'supplier:user:view', 'fa fa-users', 'admin', sysdate(), '', null, '企业用户维护菜单');

-- 一级菜单 - 医院管理
insert into sys_menu values('2100', '医院管理', '0', '6', '#', '', 'M', '0', '1', '', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '医院管理目录');
-- 二级菜单
insert into sys_menu values('2101', '医院信息维护', '2100', '1', '/hospital/hospital', '', 'C', '0', '1', 'hospital:hospital:view', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '医院信息维护菜单');

-- 一级菜单 - 基础数据
insert into sys_menu values('2200', '基础数据', '0', '7', '#', '', 'M', '0', '1', '', 'fa fa-database', 'admin', sysdate(), '', null, '基础数据目录');
-- 二级菜单
insert into sys_menu values('2201', '耗材分类', '2200', '1', '/material/category', '', 'C', '0', '1', 'material:category:view', 'fa fa-sitemap', 'admin', sysdate(), '', null, '耗材分类菜单');
insert into sys_menu values('2202', '物资字典', '2200', '2', '/material/dict', '', 'C', '0', '1', 'material:dict:view', 'fa fa-book', 'admin', sysdate(), '', null, '物资字典菜单');

-- 一级菜单 - 资质证件管理
insert into sys_menu values('2300', '资质证件管理', '0', '8', '#', '', 'M', '0', '1', '', 'fa fa-certificate', 'admin', sysdate(), '', null, '资质证件管理目录');
-- 二级菜单
insert into sys_menu values('2301', '供应商资质登记', '2300', '1', '/certificate/supplier', '', 'C', '0', '1', 'certificate:supplier:view', 'fa fa-id-card', 'admin', sysdate(), '', null, '供应商资质登记菜单');
insert into sys_menu values('2303', '供应商资质审核', '2300', '3', '/certificate/supplier/audit', '', 'C', '0', '1', 'certificate:supplier:audit', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '供应商资质审核菜单');
insert into sys_menu values('2302', '产品证件登记', '2300', '2', '/certificate/product', '', 'C', '0', '1', 'certificate:product:view', 'fa fa-file-text-o', 'admin', sysdate(), '', null, '产品证件登记菜单');
insert into sys_menu values('2304', '产品证件审核', '2300', '4', '/certificate/product/audit', '', 'C', '0', '1', 'certificate:product:audit', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '产品证件审核菜单');
insert into sys_menu values('2305', '证件类型维护', '2300', '5', '/certificate/type', '', 'C', '0', '1', 'certificate:type:view', 'fa fa-list', 'admin', sysdate(), '', null, '证件类型维护菜单');

-- 一级菜单 - 订单管理
insert into sys_menu values('2400', '订单管理', '0', '9', '#', '', 'M', '0', '1', '', 'fa fa-shopping-cart', 'admin', sysdate(), '', null, '订单管理目录');
-- 二级菜单
insert into sys_menu values('2401', '订单查询', '2400', '1', '/order/order', '', 'C', '0', '1', 'order:order:view', 'fa fa-list-alt', 'admin', sysdate(), '', null, '订单查询菜单');
insert into sys_menu values('2402', '订单接收', '2400', '2', '/order/receive', '', 'C', '0', '1', 'order:order:receive', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '订单接收菜单');

-- 一级菜单 - 配送管理
insert into sys_menu values('2500', '配送管理', '0', '10', '#', '', 'M', '0', '1', '', 'fa fa-truck', 'admin', sysdate(), '', null, '配送管理目录');
-- 二级菜单
insert into sys_menu values('2501', '配送单据申请', '2500', '1', '/delivery/delivery', '', 'C', '0', '1', 'delivery:delivery:view', 'fa fa-file-text', 'admin', sysdate(), '', null, '配送单据申请菜单');
insert into sys_menu values('2502', '配送信息查询', '2500', '2', '/delivery/delivery/query', '', 'C', '0', '1', 'delivery:delivery:view', 'fa fa-search', 'admin', sysdate(), '', null, '配送信息查询菜单');

-- 一级菜单 - 结算管理
insert into sys_menu values('2600', '结算管理', '0', '11', '#', '', 'M', '0', '1', '', 'fa fa-money', 'admin', sysdate(), '', null, '结算管理目录');
-- 二级菜单
insert into sys_menu values('2601', '发票结算', '2600', '1', '/settlement/settlement', '', 'C', '0', '1', 'settlement:settlement:view', 'fa fa-file-text-o', 'admin', sysdate(), '', null, '发票结算菜单');
insert into sys_menu values('2602', '结算查询', '2600', '2', '/settlement/query', '', 'C', '0', '1', 'settlement:settlement:view', 'admin', sysdate(), '', null, '结算查询菜单');

-- 一级菜单 - 数据中心
insert into sys_menu values('2700', '数据中心', '0', '12', '#', '', 'M', '0', '1', '', 'fa fa-bar-chart', 'admin', sysdate(), '', null, '数据中心目录');
-- 二级菜单
insert into sys_menu values('2701', '月采购量', '2700', '1', '/datacenter/datacenter/monthly', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-calendar', 'admin', sysdate(), '', null, '月采购量菜单');
insert into sys_menu values('2702', '年采购量', '2700', '2', '/datacenter/datacenter/yearly', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-calendar-o', 'admin', sysdate(), '', null, '年采购量菜单');
insert into sys_menu values('2703', '数据分析报表', '2700', '3', '/datacenter/datacenter/analysis', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-line-chart', 'admin', sysdate(), '', null, '数据分析报表菜单');

-- ----------------------------
-- 按钮权限配置
-- ----------------------------

-- 供应商维护按钮
insert into sys_menu values('20001', '供应商查询', '2001', '1', '#', '', 'F', '0', '1', 'supplier:supplier:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20002', '供应商新增', '2001', '2', '#', '', 'F', '0', '1', 'supplier:supplier:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20003', '供应商修改', '2001', '3', '#', '', 'F', '0', '1', 'supplier:supplier:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20004', '供应商删除', '2001', '4', '#', '', 'F', '0', '1', 'supplier:supplier:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20005', '供应商导出', '2001', '5', '#', '', 'F', '0', '1', 'supplier:supplier:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20006', '供应商审核', '2001', '6', '#', '', 'F', '0', '1', 'supplier:supplier:audit', '#', 'admin', sysdate(), '', null, '');

-- 企业用户维护按钮
insert into sys_menu values('20011', '企业用户查询', '2002', '1', '#', '', 'F', '0', '1', 'supplier:user:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20012', '企业用户新增', '2002', '2', '#', '', 'F', '0', '1', 'supplier:user:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20013', '企业用户修改', '2002', '3', '#', '', 'F', '0', '1', 'supplier:user:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20014', '企业用户删除', '2002', '4', '#', '', 'F', '0', '1', 'supplier:user:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('20015', '企业用户导出', '2002', '5', '#', '', 'F', '0', '1', 'supplier:user:export', '#', 'admin', sysdate(), '', null, '');

-- 医院信息维护按钮
insert into sys_menu values('21001', '医院查询', '2101', '1', '#', '', 'F', '0', '1', 'hospital:hospital:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('21002', '医院新增', '2101', '2', '#', '', 'F', '0', '1', 'hospital:hospital:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('21003', '医院修改', '2101', '3', '#', '', 'F', '0', '1', 'hospital:hospital:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('21004', '医院删除', '2101', '4', '#', '', 'F', '0', '1', 'hospital:hospital:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('21005', '医院导出', '2101', '5', '#', '', 'F', '0', '1', 'hospital:hospital:export', '#', 'admin', sysdate(), '', null, '');

-- 耗材分类按钮
insert into sys_menu values('22001', '分类查询', '2201', '1', '#', '', 'F', '0', '1', 'material:category:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('22002', '分类新增', '2201', '2', '#', '', 'F', '0', '1', 'material:category:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('22003', '分类修改', '2201', '3', '#', '', 'F', '0', '1', 'material:category:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('22004', '分类删除', '2201', '4', '#', '', 'F', '0', '1', 'material:category:remove', '#', 'admin', sysdate(), '', null, '');

-- 物资字典按钮
insert into sys_menu values('22011', '物资查询', '2202', '1', '#', '', 'F', '0', '1', 'material:dict:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('22012', '物资新增', '2202', '2', '#', '', 'F', '0', '1', 'material:dict:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('22013', '物资修改', '2202', '3', '#', '', 'F', '0', '1', 'material:dict:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('22014', '物资删除', '2202', '4', '#', '', 'F', '0', '1', 'material:dict:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('22015', '物资导出', '2202', '5', '#', '', 'F', '0', '1', 'material:dict:export', '#', 'admin', sysdate(), '', null, '');

-- 供应商证件按钮
insert into sys_menu values('23001', '证件查询', '2301', '1', '#', '', 'F', '0', '1', 'certificate:supplier:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23002', '证件新增', '2301', '2', '#', '', 'F', '0', '1', 'certificate:supplier:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23003', '证件修改', '2301', '3', '#', '', 'F', '0', '1', 'certificate:supplier:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23004', '证件删除', '2301', '4', '#', '', 'F', '0', '1', 'certificate:supplier:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23005', '证件审核', '2301', '5', '#', '', 'F', '0', '1', 'certificate:supplier:audit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23006', '证件导出', '2301', '6', '#', '', 'F', '0', '1', 'certificate:supplier:export', '#', 'admin', sysdate(), '', null, '');

-- 产品证件按钮
insert into sys_menu values('23011', '证件查询', '2302', '1', '#', '', 'F', '0', '1', 'certificate:product:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23012', '证件新增', '2302', '2', '#', '', 'F', '0', '1', 'certificate:product:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23013', '证件修改', '2302', '3', '#', '', 'F', '0', '1', 'certificate:product:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23014', '证件删除', '2302', '4', '#', '', 'F', '0', '1', 'certificate:product:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23015', '证件审核', '2302', '5', '#', '', 'F', '0', '1', 'certificate:product:audit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23016', '证件导出', '2302', '6', '#', '', 'F', '0', '1', 'certificate:product:export', '#', 'admin', sysdate(), '', null, '');

-- 证件类型维护按钮
insert into sys_menu values('23021', '类型查询', '2305', '1', '#', '', 'F', '0', '1', 'certificate:type:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23022', '类型新增', '2305', '2', '#', '', 'F', '0', '1', 'certificate:type:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23023', '类型修改', '2305', '3', '#', '', 'F', '0', '1', 'certificate:type:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23024', '类型删除', '2305', '4', '#', '', 'F', '0', '1', 'certificate:type:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('23025', '类型导出', '2305', '5', '#', '', 'F', '0', '1', 'certificate:type:export', '#', 'admin', sysdate(), '', null, '');

-- 订单管理按钮
insert into sys_menu values('24001', '订单查询', '2401', '1', '#', '', 'F', '0', '1', 'order:order:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('24002', '订单新增', '2401', '2', '#', '', 'F', '0', '1', 'order:order:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('24003', '订单修改', '2401', '3', '#', '', 'F', '0', '1', 'order:order:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('24004', '订单删除', '2401', '4', '#', '', 'F', '0', '1', 'order:order:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('24005', '订单导出', '2401', '5', '#', '', 'F', '0', '1', 'order:order:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('24006', '订单详情', '2401', '6', '#', '', 'F', '0', '1', 'order:order:detail', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('24007', '订单接收', '2402', '1', '#', '', 'F', '0', '1', 'order:order:receive', '#', 'admin', sysdate(), '', null, '');

-- 配送管理按钮
insert into sys_menu values('25001', '配送单查询', '2501', '1', '#', '', 'F', '0', '1', 'delivery:delivery:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25002', '配送单新增', '2501', '2', '#', '', 'F', '0', '1', 'delivery:delivery:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25003', '配送单修改', '2501', '3', '#', '', 'F', '0', '1', 'delivery:delivery:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25004', '配送单删除', '2501', '4', '#', '', 'F', '0', '1', 'delivery:delivery:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25005', '配送单导出', '2501', '5', '#', '', 'F', '0', '1', 'delivery:delivery:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25006', '配送单审核', '2501', '6', '#', '', 'F', '0', '1', 'delivery:delivery:audit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25007', '配送单打印', '2501', '7', '#', '', 'F', '0', '1', 'delivery:delivery:print', '#', 'admin', sysdate(), '', null, '');
-- 配送信息查询按钮
insert into sys_menu values('25011', '明细表查询', '2502', '1', '#', '', 'F', '0', '1', 'delivery:delivery:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25012', '明细表导出', '2502', '2', '#', '', 'F', '0', '1', 'delivery:delivery:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25013', '汇总表查询', '2502', '3', '#', '', 'F', '0', '1', 'delivery:delivery:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('25014', '汇总表导出', '2502', '4', '#', '', 'F', '0', '1', 'delivery:delivery:export', '#', 'admin', sysdate(), '', null, '');

-- 结算管理按钮
insert into sys_menu values('26001', '结算单查询', '2601', '1', '#', '', 'F', '0', '1', 'settlement:settlement:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('26002', '结算单新增', '2601', '2', '#', '', 'F', '0', '1', 'settlement:settlement:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('26003', '结算单修改', '2601', '3', '#', '', 'F', '0', '1', 'settlement:settlement:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('26004', '结算单删除', '2601', '4', '#', '', 'F', '0', '1', 'settlement:settlement:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('26005', '结算单导出', '2601', '5', '#', '', 'F', '0', '1', 'settlement:settlement:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('26006', '结算单审核', '2601', '6', '#', '', 'F', '0', '1', 'settlement:settlement:audit', '#', 'admin', sysdate(), '', null, '');

-- 数据中心按钮
insert into sys_menu values('27001', '统计查询', '2701', '1', '#', '', 'F', '0', '1', 'datacenter:datacenter:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('27002', '统计导出', '2701', '2', '#', '', 'F', '0', '1', 'datacenter:datacenter:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('27003', '数据生成', '2701', '3', '#', '', 'F', '0', '1', 'datacenter:datacenter:edit', '#', 'admin', sysdate(), '', null, '');

