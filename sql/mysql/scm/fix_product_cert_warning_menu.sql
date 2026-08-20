-- 去掉重复的「产品证件预警」菜单，只保留 menu_id=2308
-- visible: 0显示 1隐藏；status: 0正常 1停用

-- 1) 确保标准菜单存在且指向预警页
INSERT IGNORE INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
  perms, icon, create_by, create_time, update_by, update_time, remark, status
) VALUES (
  '2308', '产品证件预警', '2300', '8', '/certificate/product/warning', '', 'C', '0', '1',
  'certificate:product:list', 'fa fa-exclamation-triangle', 'admin', sysdate(), '', null,
  '产品证件有效期预警（45天内）', '0'
);

UPDATE sys_menu
SET menu_name = '产品证件预警',
    url = '/certificate/product/warning',
    perms = 'certificate:product:list',
    parent_id = '2300',
    visible = '0',
    status = '0',
    update_time = sysdate()
WHERE menu_id = '2308';

-- 2) 隐藏其它同名/同 URL 的重复项（如历史菜单 20017）
UPDATE sys_menu
SET visible = '1',
    status = '1',
    update_time = sysdate(),
    remark = CONCAT(IFNULL(remark, ''), ' [dup-hidden]')
WHERE menu_id <> '2308'
  AND (
    menu_name = '产品证件预警'
    OR url = '/certificate/product/warning'
  );
