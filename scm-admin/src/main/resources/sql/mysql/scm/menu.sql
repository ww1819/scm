-- ========== SCM 供应商管理平台 菜单权限配置 ==========
-- 按「/」分段执行；使用 INSERT IGNORE 避免重复执行报错
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2000', '供应商管理', '0', '5', '#', '', 'M', '0', '1', '', 'fa fa-truck', 'admin', sysdate(), '', null, '供应商管理目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2001', '供应商维护', '2000', '1', '/supplier/supplier', '', 'C', '0', '1', 'supplier:supplier:view', 'fa fa-building', 'admin', sysdate(), '', null, '供应商维护菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2002', '企业用户维护', '2000', '2', '/supplier/user', '', 'C', '0', '1', 'supplier:user:view', 'fa fa-users', 'admin', sysdate(), '', null, '企业用户维护菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2003', '业务员审核', '2000', '3', '/supplier/apply', '', 'C', '0', '1', 'supplier:apply:view', 'fa fa-user-plus', 'admin', sysdate(), '', null, '供应商业务员关联申请审核', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20031', '申请列表', '2003', '1', '#', '', 'F', '0', '1', 'supplier:apply:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20032', '审核', '2003', '2', '#', '', 'F', '0', '1', 'supplier:apply:audit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2004', '新增医院关联', '2000', '4', '/supplier/associate', '', 'C', '0', '1', 'supplier:associate:view', 'fa fa-link', 'admin', sysdate(), '', null, '供应商向医院提交关联申请，医院审核通过后建立医院供应商绑定关系', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20041', '提交关联', '2004', '1', '#', '', 'F', '0', '1', 'supplier:associate:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20042', '撤回申请', '2004', '2', '#', '', 'F', '0', '1', 'supplier:associate:withdraw', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2100', '医院管理', '0', '6', '#', '', 'M', '0', '1', '', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '医院管理目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2101', '医院信息维护', '2100', '1', '/hospital/hospital', '', 'C', '0', '1', 'hospital:hospital:view', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '医院信息维护菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2102', '医院关联申请审核', '2100', '2', '/hospital/associateAudit', '', 'C', '0', '1', 'hospital:associateAudit:view', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '医院审核供应商发起的医院关联申请', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('21021', '审核列表', '2102', '1', '#', '', 'F', '0', '1', 'hospital:associateAudit:view', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('21022', '申请审核', '2102', '2', '#', '', 'F', '0', '1', 'hospital:associateAudit:audit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2200', '基础数据', '0', '7', '#', '', 'M', '0', '1', '', 'fa fa-database', 'admin', sysdate(), '', null, '基础数据目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2201', '耗材分类', '2200', '1', '/material/category', '', 'C', '0', '1', 'material:category:view', 'fa fa-sitemap', 'admin', sysdate(), '', null, '耗材分类菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2202', '物资字典', '2200', '2', '/material/dict', '', 'C', '0', '1', 'material:dict:view', 'fa fa-book', 'admin', sysdate(), '', null, '物资字典菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2300', '资质证件管理', '0', '8', '#', '', 'M', '0', '1', '', 'fa fa-certificate', 'admin', sysdate(), '', null, '资质证件管理目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2301', '供应商资质登记', '2300', '1', '/certificate/supplier', '', 'C', '0', '1', 'certificate:supplier:view', 'fa fa-id-card', 'admin', sysdate(), '', null, '供应商资质登记菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2302', '产品证件登记', '2300', '2', '/certificate/product', '', 'C', '0', '1', 'certificate:product:view', 'fa fa-file-text-o', 'admin', sysdate(), '', null, '产品证件登记菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2303', '供应商资质审核', '2300', '3', '/certificate/supplier/audit', '', 'C', '0', '1', 'certificate:supplier:audit', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '供应商资质审核菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2304', '产品证件审核', '2300', '4', '/certificate/product/audit', '', 'C', '0', '1', 'certificate:product:audit', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '产品证件审核菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2305', '证件类型维护', '2300', '5', '/certificate/type', '', 'C', '0', '1', 'certificate:type:view', 'fa fa-list', 'admin', sysdate(), '', null, '证件类型维护菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2400', '订单管理', '0', '9', '#', '', 'M', '0', '1', '', 'fa fa-shopping-cart', 'admin', sysdate(), '', null, '订单管理目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2401', '订单查询', '2400', '1', '/order/order', '', 'C', '0', '1', 'order:order:view', 'fa fa-list-alt', 'admin', sysdate(), '', null, '订单查询菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2402', '订单接收', '2400', '2', '/order/receive', '', 'C', '0', '1', 'order:order:receive', 'fa fa-check-square-o', 'admin', sysdate(), '', null, '订单接收菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2500', '配送管理', '0', '10', '#', '', 'M', '0', '1', '', 'fa fa-truck', 'admin', sysdate(), '', null, '配送管理目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2501', '配送单据申请', '2500', '1', '/delivery/delivery', '', 'C', '0', '1', 'delivery:delivery:view', 'fa fa-file-text', 'admin', sysdate(), '', null, '配送单据申请菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2502', '配送信息查询', '2500', '2', '/delivery/delivery/query', '', 'C', '0', '1', 'delivery:delivery:view', 'fa fa-search', 'admin', sysdate(), '', null, '配送信息查询菜单', '0');
/
-- 配送单据申请按钮（父菜单 2501）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25001', '配送单查询', '2501', '1', '#', '', 'F', '0', '1', 'delivery:delivery:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25002', '配送单新增', '2501', '2', '#', '', 'F', '0', '1', 'delivery:delivery:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25003', '配送单修改', '2501', '3', '#', '', 'F', '0', '1', 'delivery:delivery:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25004', '配送单删除', '2501', '4', '#', '', 'F', '0', '1', 'delivery:delivery:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25005', '配送单导出', '2501', '5', '#', '', 'F', '0', '1', 'delivery:delivery:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25006', '配送单审核', '2501', '6', '#', '', 'F', '0', '1', 'delivery:delivery:audit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25007', '配送单打印', '2501', '7', '#', '', 'F', '0', '1', 'delivery:delivery:print', '#', 'admin', sysdate(), '', null, '', '0');
/
-- 配送信息查询按钮（父菜单 2502）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25011', '明细表查询', '2502', '1', '#', '', 'F', '0', '1', 'delivery:delivery:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25012', '明细表导出', '2502', '2', '#', '', 'F', '0', '1', 'delivery:delivery:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25013', '汇总表查询', '2502', '3', '#', '', 'F', '0', '1', 'delivery:delivery:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25014', '汇总表导出', '2502', '4', '#', '', 'F', '0', '1', 'delivery:delivery:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2600', '结算管理', '0', '11', '#', '', 'M', '0', '1', '', 'fa fa-money', 'admin', sysdate(), '', null, '结算管理目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2601', '发票结算', '2600', '1', '/settlement/settlement', '', 'C', '0', '1', 'settlement:settlement:view', 'fa fa-file-text-o', 'admin', sysdate(), '', null, '发票结算菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2602', '结算查询', '2600', '2', '/settlement/query', '', 'C', '0', '1', 'settlement:settlement:view', '', 'admin', sysdate(), '', null, '结算查询菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2700', '数据中心', '0', '12', '#', '', 'M', '0', '1', '', 'fa fa-bar-chart', 'admin', sysdate(), '', null, '数据中心目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2701', '月采购量', '2700', '1', '/datacenter/datacenter/monthly', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-calendar', 'admin', sysdate(), '', null, '月采购量菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2702', '年采购量', '2700', '2', '/datacenter/datacenter/yearly', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-calendar-o', 'admin', sysdate(), '', null, '年采购量菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2703', '数据分析报表', '2700', '3', '/datacenter/datacenter/analysis', '', 'C', '0', '1', 'datacenter:datacenter:view', 'fa fa-line-chart', 'admin', sysdate(), '', null, '数据分析报表菜单', '0');
/
-- 客户管理（租户）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2800', '客户管理', '0', '13', '#', '', 'M', '0', '1', '', 'fa fa-id-badge', 'admin', sysdate(), '', null, '客户管理目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2801', '客户维护', '2800', '1', '/tenant/tenant', '', 'C', '0', '1', 'tenant:tenant:view', 'fa fa-users', 'admin', sysdate(), '', null, '客户维护菜单', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2802', '客户菜单功能管理', '2800', '2', '/tenant/menuPause', '', 'C', '0', '1', 'tenant:menuPause:view', 'fa fa-pause-circle-o', 'admin', sysdate(), '', null, '客户菜单暂停使用管理', '0');
/
-- 客户维护按钮权限（父菜单2801）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2811', '客户查询', '2801', '1', '#', '', 'F', '0', '1', 'tenant:tenant:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2812', '客户新增', '2801', '2', '#', '', 'F', '0', '1', 'tenant:tenant:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2813', '客户修改', '2801', '3', '#', '', 'F', '0', '1', 'tenant:tenant:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2814', '客户删除', '2801', '4', '#', '', 'F', '0', '1', 'tenant:tenant:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2815', '客户导出', '2801', '5', '#', '', 'F', '0', '1', 'tenant:tenant:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2816', '客户授权', '2801', '6', '#', '', 'F', '0', '1', 'tenant:tenant:auth', '#', 'admin', sysdate(), '', null, '客户授权（分配功能菜单）', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2818', '客户菜单权限授权', '2801', '8', '#', '', 'F', '0', '1', 'tenant:tenant:auth', '#', 'admin', sysdate(), '', null, '为客户分配可用的功能菜单权限', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2817', '客户重置', '2801', '7', '#', '', 'F', '0', '1', 'tenant:tenant:reset', '#', 'admin', sysdate(), '', null, '', '0');
/
-- 客户菜单功能管理按钮（父菜单2802）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2821', '菜单暂停列表', '2802', '1', '#', '', 'F', '0', '1', 'tenant:menuPause:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2822', '菜单暂停/恢复', '2802', '2', '#', '', 'F', '0', '1', 'tenant:menuPause:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
-- 权限补遗（2026-04-27 代码扫描补齐）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('25008', '配送单详情', '2501', '8', '#', '', 'F', '0', '1', 'delivery:delivery:detail', '#', 'admin', sysdate(), '', null, '代码中存在 detail 权限，菜单缺失补齐', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('26007', '结算单详情', '2601', '7', '#', '', 'F', '0', '1', 'settlement:settlement:detail', '#', 'admin', sysdate(), '', null, '代码中存在 detail 权限，菜单缺失补齐', '0');
/
-- 接口与对接（当前生产菜单表已有，脚本补齐）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2910', '接口与对接', '0', '20', '#', '', 'M', '0', '1', '', 'fa fa-plug', 'admin', sysdate(), '', null, 'SCMInterface 第三方对接目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2911', '第三方推送订单', '2910', '1', '/interface/zsTpOrder', '', 'C', '0', '1', 'interface:zsTp:view', 'fa fa-cloud-download', 'admin', sysdate(), '', null, 'ZS 推送订单（占位路由，前端可按需实现）', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2912', '推送订单查询', '2911', '1', '#', '', 'F', '0', '1', 'interface:zsTp:query', '#', 'admin', sysdate(), '', null, '', '0');
/
-- 增量补齐（来自 scm_menu.sql 缺失项）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20011', '企业用户查询', '2002', '1', '#', '', 'F', '0', '1', 'supplier:user:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20012', '企业用户新增', '2002', '2', '#', '', 'F', '0', '1', 'supplier:user:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20013', '企业用户修改', '2002', '3', '#', '', 'F', '0', '1', 'supplier:user:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20014', '企业用户删除', '2002', '4', '#', '', 'F', '0', '1', 'supplier:user:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20015', '企业用户导出', '2002', '5', '#', '', 'F', '0', '1', 'supplier:user:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('21001', '医院查询', '2101', '1', '#', '', 'F', '0', '1', 'hospital:hospital:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('21002', '医院新增', '2101', '2', '#', '', 'F', '0', '1', 'hospital:hospital:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('21003', '医院修改', '2101', '3', '#', '', 'F', '0', '1', 'hospital:hospital:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('21004', '医院删除', '2101', '4', '#', '', 'F', '0', '1', 'hospital:hospital:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('21005', '医院导出', '2101', '5', '#', '', 'F', '0', '1', 'hospital:hospital:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22001', '分类查询', '2201', '1', '#', '', 'F', '0', '1', 'material:category:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22002', '分类新增', '2201', '2', '#', '', 'F', '0', '1', 'material:category:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22003', '分类修改', '2201', '3', '#', '', 'F', '0', '1', 'material:category:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22004', '分类删除', '2201', '4', '#', '', 'F', '0', '1', 'material:category:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22011', '物资查询', '2202', '1', '#', '', 'F', '0', '1', 'material:dict:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22012', '物资新增', '2202', '2', '#', '', 'F', '0', '1', 'material:dict:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22013', '物资修改', '2202', '3', '#', '', 'F', '0', '1', 'material:dict:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22014', '物资删除', '2202', '4', '#', '', 'F', '0', '1', 'material:dict:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('22015', '物资导出', '2202', '5', '#', '', 'F', '0', '1', 'material:dict:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23001', '证件查询', '2301', '1', '#', '', 'F', '0', '1', 'certificate:supplier:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23002', '证件新增', '2301', '2', '#', '', 'F', '0', '1', 'certificate:supplier:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23003', '证件修改', '2301', '3', '#', '', 'F', '0', '1', 'certificate:supplier:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23004', '证件删除', '2301', '4', '#', '', 'F', '0', '1', 'certificate:supplier:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23005', '证件审核', '2301', '5', '#', '', 'F', '0', '1', 'certificate:supplier:audit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23006', '证件导出', '2301', '6', '#', '', 'F', '0', '1', 'certificate:supplier:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23011', '证件查询', '2302', '1', '#', '', 'F', '0', '1', 'certificate:product:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23012', '证件新增', '2302', '2', '#', '', 'F', '0', '1', 'certificate:product:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23013', '证件修改', '2302', '3', '#', '', 'F', '0', '1', 'certificate:product:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23014', '证件删除', '2302', '4', '#', '', 'F', '0', '1', 'certificate:product:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23015', '证件审核', '2302', '5', '#', '', 'F', '0', '1', 'certificate:product:audit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23016', '证件导出', '2302', '6', '#', '', 'F', '0', '1', 'certificate:product:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23021', '类型查询', '2305', '1', '#', '', 'F', '0', '1', 'certificate:type:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23022', '类型新增', '2305', '2', '#', '', 'F', '0', '1', 'certificate:type:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23023', '类型修改', '2305', '3', '#', '', 'F', '0', '1', 'certificate:type:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23024', '类型删除', '2305', '4', '#', '', 'F', '0', '1', 'certificate:type:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('23025', '类型导出', '2305', '5', '#', '', 'F', '0', '1', 'certificate:type:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('24001', '订单查询', '2401', '1', '#', '', 'F', '0', '1', 'order:order:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('24002', '订单新增', '2401', '2', '#', '', 'F', '0', '1', 'order:order:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('24003', '订单修改', '2401', '3', '#', '', 'F', '0', '1', 'order:order:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('24004', '订单删除', '2401', '4', '#', '', 'F', '0', '1', 'order:order:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('24005', '订单导出', '2401', '5', '#', '', 'F', '0', '1', 'order:order:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('24006', '订单详情', '2401', '6', '#', '', 'F', '0', '1', 'order:order:detail', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('24007', '订单接收', '2402', '1', '#', '', 'F', '0', '1', 'order:order:receive', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('27001', '统计查询', '2701', '1', '#', '', 'F', '0', '1', 'datacenter:datacenter:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('27002', '统计导出', '2701', '2', '#', '', 'F', '0', '1', 'datacenter:datacenter:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('27003', '数据生成', '2701', '3', '#', '', 'F', '0', '1', 'datacenter:datacenter:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('27004', '统计导出(兼容)', '2701', '4', '#', '', 'F', '0', '1', 'datacenter:statistics:export', '#', 'admin', sysdate(), '', null, '兼容旧页面权限点', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20001', '供应商查询', '2001', '1', '#', '', 'F', '0', '1', 'supplier:supplier:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20002', '供应商新增', '2001', '2', '#', '', 'F', '0', '1', 'supplier:supplier:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20003', '供应商修改', '2001', '3', '#', '', 'F', '0', '1', 'supplier:supplier:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20004', '供应商删除', '2001', '4', '#', '', 'F', '0', '1', 'supplier:supplier:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20005', '供应商导出', '2001', '5', '#', '', 'F', '0', '1', 'supplier:supplier:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20006', '供应商审核', '2001', '6', '#', '', 'F', '0', '1', 'supplier:supplier:audit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('26001', '结算单查询', '2601', '1', '#', '', 'F', '0', '1', 'settlement:settlement:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('26002', '结算单新增', '2601', '2', '#', '', 'F', '0', '1', 'settlement:settlement:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('26003', '结算单修改', '2601', '3', '#', '', 'F', '0', '1', 'settlement:settlement:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('26004', '结算单删除', '2601', '4', '#', '', 'F', '0', '1', 'settlement:settlement:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('26005', '结算单导出', '2601', '5', '#', '', 'F', '0', '1', 'settlement:settlement:export', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('26006', '结算单审核', '2601', '6', '#', '', 'F', '0', '1', 'settlement:settlement:audit', '#', 'admin', sysdate(), '', null, '', '0');
/
-- SCM「数据权限」菜单单独占用 285x：勿复用 2800–2802（该段已由上方「客户管理」占用，INSERT IGNORE 会导致本段整批静默跳过）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2850', '数据权限', '0', '13', '#', '', 'M', '0', '1', '', 'fa fa-shield', 'admin', sysdate(), '', null, 'SCM医院/供应商菜单与数据权限', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2851', '医院菜单授权', '2850', '1', '/scm/auth/hospitalMenu', '', 'C', '0', '1', 'scmAuth:hospitalMenu:view', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28511', '医院菜单查询', '2851', '1', '#', '', 'F', '0', '1', 'scmAuth:hospitalMenu:query', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28512', '医院菜单保存', '2851', '2', '#', '', 'F', '0', '1', 'scmAuth:hospitalMenu:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28513', '医院菜单重置', '2851', '3', '#', '', 'F', '0', '1', 'scmAuth:hospitalMenu:reset', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2852', '供应商菜单授权', '2850', '2', '/scm/auth/supplierMenu', '', 'C', '0', '1', 'scmAuth:supplierMenu:view', 'fa fa-truck', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28521', '供应商菜单查询', '2852', '1', '#', '', 'F', '0', '1', 'scmAuth:supplierMenu:query', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28522', '供应商菜单保存', '2852', '2', '#', '', 'F', '0', '1', 'scmAuth:supplierMenu:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28523', '供应商菜单重置', '2852', '3', '#', '', 'F', '0', '1', 'scmAuth:supplierMenu:reset', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2853', '医院供应商数据权限', '2850', '3', '/scm/auth/hospitalSupplierPerm', '', 'C', '0', '1', 'scmAuth:hospitalSupplier:view', 'fa fa-ban', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28531', '黑名单列表', '2853', '1', '#', '', 'F', '0', '1', 'scmAuth:hospitalSupplier:list', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28532', '黑名单维护', '2853', '2', '#', '', 'F', '0', '1', 'scmAuth:hospitalSupplier:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28533', '黑名单删除', '2853', '3', '#', '', 'F', '0', '1', 'scmAuth:hospitalSupplier:remove', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2854', '医院供应商菜单授权', '2850', '4', '/scm/auth/hospitalSupplierMenu', '', 'C', '0', '1', 'scmAuth:hospitalSupplierMenu:view', 'fa fa-sitemap', 'admin', sysdate(), '', null, '医院向名下供应商授予联合菜单权限', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28541', '医院供应商菜单查询', '2854', '1', '#', '', 'F', '0', '1', 'scmAuth:hospitalSupplierMenu:query', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('28542', '医院供应商菜单保存', '2854', '2', '#', '', 'F', '0', '1', 'scmAuth:hospitalSupplierMenu:edit', '#', 'admin', sysdate(), '', null, '', '0');
/
-- ========== SCM 菜单维度：权限类型 / 院授予供应商 / 默认开放范围 / 业务分类 ==========
CALL add_table_column('sys_menu', 'menu_biz_category', 'varchar(32)', '业务分类', 'other');
/
UPDATE sys_menu SET data_binding_flag = '0' WHERE del_flag = '0' OR del_flag IS NULL;
/
-- 平台：系统、租户、接口、SCM 授权配置（不进入院/商白名单默认同步）
UPDATE sys_menu SET auth_type = 'platform', hospital_grant_supplier_flag = '0', default_open_scope = 'none', menu_biz_category = 'scm_auth'
WHERE del_flag = '0' AND (perms LIKE 'scmAuth:%' OR perms LIKE 'system:%');
/
UPDATE sys_menu SET auth_type = 'hospital', hospital_grant_supplier_flag = '0', default_open_scope = 'all_hospital', menu_biz_category = 'scm_auth'
WHERE del_flag = '0' AND perms LIKE 'scmAuth:hospitalSupplierMenu%';
/
UPDATE sys_menu SET auth_type = 'platform', hospital_grant_supplier_flag = '0', default_open_scope = 'none', menu_biz_category = 'tenant'
WHERE del_flag = '0' AND perms LIKE 'tenant:%';
/
UPDATE sys_menu SET auth_type = 'platform', hospital_grant_supplier_flag = '0', default_open_scope = 'none', menu_biz_category = 'integration'
WHERE del_flag = '0' AND perms LIKE 'interface:%';
/
-- 主数据：仅平台维护
UPDATE sys_menu SET auth_type = 'platform', hospital_grant_supplier_flag = '0', default_open_scope = 'none', menu_biz_category = 'master_data'
WHERE del_flag = '0' AND perms LIKE 'material:%';
/
-- 供应商域
UPDATE sys_menu SET auth_type = 'supplier', hospital_grant_supplier_flag = '0', default_open_scope = 'all_supplier', menu_biz_category = 'supplier_master'
WHERE del_flag = '0' AND (perms LIKE 'supplier:%' OR url LIKE '/supplier%');
/
UPDATE sys_menu SET auth_type = 'supplier', hospital_grant_supplier_flag = '0', default_open_scope = 'all_supplier', menu_biz_category = 'supplier_master'
WHERE del_flag = '0' AND menu_id IN ('2000','2300');
/
-- 医院域
UPDATE sys_menu SET auth_type = 'hospital', hospital_grant_supplier_flag = '0', default_open_scope = 'all_hospital', menu_biz_category = 'hospital_master'
WHERE del_flag = '0' AND (perms LIKE 'hospital:%' OR url LIKE '/hospital%');
/
UPDATE sys_menu SET auth_type = 'hospital', hospital_grant_supplier_flag = '0', default_open_scope = 'all_hospital', menu_biz_category = 'hospital_master'
WHERE del_flag = '0' AND menu_id = '2100';
/
-- 资质：登记侧供应商；审核侧院-商联合且需按院授予
UPDATE sys_menu SET auth_type = 'supplier', hospital_grant_supplier_flag = '0', default_open_scope = 'all_supplier', menu_biz_category = 'certificate'
WHERE del_flag = '0' AND perms LIKE 'certificate:%' AND perms NOT LIKE '%:audit%';
/
UPDATE sys_menu SET auth_type = 'hospital_supplier', hospital_grant_supplier_flag = '1', default_open_scope = 'all_hospital', menu_biz_category = 'certificate'
WHERE del_flag = '0' AND perms LIKE 'certificate:%' AND perms LIKE '%:audit%';
/
-- 采供协同：联合菜单，医院先授权菜单范围，再对供应商按院授予
UPDATE sys_menu SET auth_type = 'hospital_supplier', hospital_grant_supplier_flag = '1', default_open_scope = 'all_hospital', menu_biz_category = 'supply_chain'
WHERE del_flag = '0' AND (perms LIKE 'order:%' OR perms LIKE 'delivery:%');
/
UPDATE sys_menu SET auth_type = 'hospital_supplier', hospital_grant_supplier_flag = '1', default_open_scope = 'all_hospital', menu_biz_category = 'settlement'
WHERE del_flag = '0' AND perms LIKE 'settlement:%';
/
-- 数据中心：院、商均可见，默认不按院授予（与 sys_menu 中 hospital_supplier 非 grant 分支一致）
UPDATE sys_menu SET auth_type = 'hospital_supplier', hospital_grant_supplier_flag = '0', default_open_scope = 'all', menu_biz_category = 'datacenter'
WHERE del_flag = '0' AND perms LIKE 'datacenter:%';
/
UPDATE sys_menu SET auth_type = 'hospital_supplier', hospital_grant_supplier_flag = '0', default_open_scope = 'all', menu_biz_category = 'datacenter'
WHERE del_flag = '0' AND menu_id IN ('2700','2701','2702','2703');
/
-- 无 perms 的目录：按业务模块补维度（若 menu_id 与历史脚本冲突，可按 menu_name 再调）
UPDATE sys_menu SET auth_type = 'hospital_supplier', hospital_grant_supplier_flag = '1', default_open_scope = 'all_hospital', menu_biz_category = 'supply_chain'
WHERE del_flag = '0' AND menu_id IN ('2400','2500');
/
UPDATE sys_menu SET auth_type = 'hospital_supplier', hospital_grant_supplier_flag = '1', default_open_scope = 'all_hospital', menu_biz_category = 'settlement'
WHERE del_flag = '0' AND menu_id = '2600';
/
UPDATE sys_menu SET auth_type = 'platform', hospital_grant_supplier_flag = '0', default_open_scope = 'none', menu_biz_category = 'platform_ops'
WHERE del_flag = '0' AND menu_id IN ('2200','2910');
/
UPDATE sys_menu SET auth_type = 'platform', hospital_grant_supplier_flag = '0', default_open_scope = 'none', menu_biz_category = 'tenant'
WHERE del_flag = '0' AND menu_name = '客户管理' AND parent_id = 0;
/
UPDATE sys_menu SET auth_type = 'platform', hospital_grant_supplier_flag = '0', default_open_scope = 'none', menu_biz_category = 'scm_auth'
WHERE del_flag = '0' AND menu_name = '数据权限' AND parent_id = 0;
/
UPDATE sys_menu SET auth_type = 'platform', hospital_grant_supplier_flag = '0', default_open_scope = 'none', menu_biz_category = 'other'
WHERE del_flag = '0' AND (auth_type IS NULL OR auth_type = '');
/
-- 历史环境：为已有医院/供应商用户补齐白名单（尊重 default_open_scope）
INSERT IGNORE INTO scm_supplier_menu_auth (id, supplier_id, hospital_id, menu_id, create_by, create_time)
SELECT REPLACE(UUID(), '-', ''), CAST(su.supplier_id AS CHAR), NULL, CAST(m.menu_id AS CHAR), 'migration', NOW()
FROM scm_supplier_user su
JOIN sys_menu m ON m.auth_type = 'supplier' AND (m.del_flag = '0' OR m.del_flag IS NULL)
  AND m.default_open_scope IN ('all','all_supplier')
WHERE (su.del_flag = '0' OR su.del_flag IS NULL);
/
INSERT IGNORE INTO scm_hospital_menu_auth (id, hospital_id, menu_id, create_by, create_time)
SELECT REPLACE(UUID(), '-', ''), CAST(hu.hospital_id AS CHAR), CAST(m.menu_id AS CHAR), 'migration', NOW()
FROM scm_hospital_user hu
JOIN sys_menu m ON (m.auth_type = 'hospital' OR m.auth_type = 'hospital_supplier') AND (m.del_flag = '0' OR m.del_flag IS NULL)
  AND m.default_open_scope IN ('all','all_hospital')
WHERE (hu.del_flag = '0' OR hu.del_flag IS NULL);
/
