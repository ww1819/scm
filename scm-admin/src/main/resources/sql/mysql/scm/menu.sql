-- ========== SCM 供应商管理平台 菜单权限配置 ==========
-- 按「/」分段执行；使用 INSERT IGNORE 避免重复执行报错
/
INSERT IGNORE INTO sys_menu VALUES('2000', '供应商管理', '0', '5', '#', '', 'M', '0', '1', '', 'fa fa-truck', 'admin', sysdate(), '', null, '供应商管理目录');
/
INSERT IGNORE INTO sys_menu VALUES('2001', '供应商维护', '2000', '1', '/supplier/supplier', '', 'C', '0', '1', 'supplier:supplier:view', 'fa fa-building', 'admin', sysdate(), '', null, '供应商维护菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2002', '企业用户维护', '2000', '2', '/supplier/user', '', 'C', '0', '1', 'supplier:user:view', 'fa fa-users', 'admin', sysdate(), '', null, '企业用户维护菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2100', '医院管理', '0', '6', '#', '', 'M', '0', '1', '', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '医院管理目录');
/
INSERT IGNORE INTO sys_menu VALUES('2101', '医院信息维护', '2100', '1', '/hospital/hospital', '', 'C', '0', '1', 'hospital:hospital:view', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '医院信息维护菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2200', '基础数据', '0', '7', '#', '', 'M', '0', '1', '', 'fa fa-database', 'admin', sysdate(), '', null, '基础数据目录');
/
INSERT IGNORE INTO sys_menu VALUES('2201', '耗材分类', '2200', '1', '/material/category', '', 'C', '0', '1', 'material:category:view', 'fa fa-sitemap', 'admin', sysdate(), '', null, '耗材分类菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2202', '物资字典', '2200', '2', '/material/dict', '', 'C', '0', '1', 'material:dict:view', 'fa fa-book', 'admin', sysdate(), '', null, '物资字典菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2300', '资质证件管理', '0', '8', '#', '', 'M', '0', '1', '', 'fa fa-certificate', 'admin', sysdate(), '', null, '资质证件管理目录');
/
INSERT IGNORE INTO sys_menu VALUES('2301', '供应商资质登记', '2300', '1', '/certificate/supplier', '', 'C', '0', '1', 'certificate:supplier:view', 'fa fa-id-card', 'admin', sysdate(), '', null, '供应商资质登记菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2302', '产品证件登记', '2300', '2', '/certificate/product', '', 'C', '0', '1', 'certificate:product:view', 'fa fa-file-text-o', 'admin', sysdate(), '', null, '产品证件登记菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2303', '供应商资质审核', '2300', '3', '/certificate/supplier/audit', '', 'C', '0', '1', 'certificate:supplier:audit', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '供应商资质审核菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2304', '产品证件审核', '2300', '4', '/certificate/product/audit', '', 'C', '0', '1', 'certificate:product:audit', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '产品证件审核菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2305', '证件类型维护', '2300', '5', '/certificate/type', '', 'C', '0', '1', 'certificate:type:view', 'fa fa-list', 'admin', sysdate(), '', null, '证件类型维护菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2400', '订单管理', '0', '9', '#', '', 'M', '0', '1', '', 'fa fa-shopping-cart', 'admin', sysdate(), '', null, '订单管理目录');
/
INSERT IGNORE INTO sys_menu VALUES('2401', '订单查询', '2400', '1', '/order/order', '', 'C', '0', '1', 'order:order:view', 'fa fa-list-alt', 'admin', sysdate(), '', null, '订单查询菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2402', '订单接收', '2400', '2', '/order/receive', '', 'C', '0', '1', 'order:order:receive', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '订单接收菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2500', '配送管理', '0', '10', '#', '', 'M', '0', '1', '', 'fa fa-truck', 'admin', sysdate(), '', null, '配送管理目录');
/
INSERT IGNORE INTO sys_menu VALUES('2501', '配送单据申请', '2500', '1', '/delivery/delivery', '', 'C', '0', '1', 'delivery:delivery:view', 'fa fa-file-text', 'admin', sysdate(), '', null, '配送单据申请菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2502', '配送信息查询', '2500', '2', '/delivery/delivery/query', '', 'C', '0', '1', 'delivery:delivery:view', 'fa fa-search', 'admin', sysdate(), '', null, '配送信息查询菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2600', '结算管理', '0', '11', '#', '', 'M', '0', '1', '', 'fa fa-money', 'admin', sysdate(), '', null, '结算管理目录');
/
INSERT IGNORE INTO sys_menu VALUES('2601', '发票结算', '2600', '1', '/settlement/settlement', '', 'C', '0', '1', 'settlement:settlement:view', 'fa fa-file-text-o', 'admin', sysdate(), '', null, '发票结算菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2602', '结算查询', '2600', '2', '/settlement/query', '', 'C', '0', '1', 'settlement:settlement:view', '', 'admin', sysdate(), '', null, '结算查询菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2700', '数据中心', '0', '12', '#', '', 'M', '0', '1', '', 'fa fa-bar-chart', 'admin', sysdate(), '', null, '数据中心目录');
/
INSERT IGNORE INTO sys_menu VALUES('2701', '月采购量', '2700', '1', '/datacenter/datacenter/monthly', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-calendar', 'admin', sysdate(), '', null, '月采购量菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2702', '年采购量', '2700', '2', '/datacenter/datacenter/yearly', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-calendar-o', 'admin', sysdate(), '', null, '年采购量菜单');
/
INSERT IGNORE INTO sys_menu VALUES('2703', '数据分析报表', '2700', '3', '/datacenter/datacenter/analysis', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-line-chart', 'admin', sysdate(), '', null, '数据分析报表菜单');
/
