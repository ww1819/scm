-- 对账表菜单：从供应商资质审核页解耦，指向独立对账表页面
UPDATE sys_menu
SET url = '/settlement/settlement/reconciliation',
    perms = 'settlement:settlement:view',
    remark = '供应商证件对账表（独立于资质审核）'
WHERE del_flag = '0'
  AND menu_name = '对账表'
  AND (url LIKE '/certificate/supplier/audit%' OR url = '/certificate/supplier/audit');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status)
VALUES ('2603', '对账表', '2600', '3', '/settlement/settlement/reconciliation', '', 'C', '0', '1', 'settlement:settlement:view', 'fa fa-table', 'admin', sysdate(), '', null, '供应商证件对账表（独立于资质审核）', '0');
