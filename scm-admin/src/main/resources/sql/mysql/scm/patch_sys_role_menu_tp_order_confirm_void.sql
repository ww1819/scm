-- 为所有有效角色赋予「第三方订单-确认」「第三方订单-作废」按钮权限（与 patch_sys_role_menu_tp_order_all_roles 用法一致）
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(), '-', ''), r.role_id, m.menu_id, '', ''
FROM sys_role r
CROSS JOIN (
  SELECT 24033 AS menu_id
  UNION ALL SELECT 24034
) m
WHERE IFNULL(r.del_flag, '0') = '0'
  AND IFNULL(r.status, '0') = '0'
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_menu rm
    WHERE rm.role_id = r.role_id
      AND rm.menu_id = m.menu_id
      AND IFNULL(rm.hospital_id, '') = ''
      AND IFNULL(rm.supplier_id, '') = ''
  );
