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
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2004', '新增供应商关联', '2000', '4', '/supplier/associate', '', 'C', '0', '1', 'supplier:associate:view', 'fa fa-link', 'admin', sysdate(), '', null, '注册用户选择供应商提交关联申请，供应商管理员审核通过后添加供应商业务员角色', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('20041', '提交关联', '2004', '1', '#', '', 'F', '0', '1', 'supplier:associate:add', '#', 'admin', sysdate(), '', null, '', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2100', '医院管理', '0', '6', '#', '', 'M', '0', '1', '', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '医院管理目录', '0');
/
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES('2101', '医院信息维护', '2100', '1', '/hospital/hospital', '', 'C', '0', '1', 'hospital:hospital:view', 'fa fa-hospital-o', 'admin', sysdate(), '', null, '医院信息维护菜单', '0');
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
