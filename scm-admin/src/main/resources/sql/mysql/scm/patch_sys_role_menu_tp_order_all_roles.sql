-- =============================================================================
-- 为所有有效角色批量赋予「第三方订单查询」菜单及按钮权限（sys_role_menu）
-- 菜单 ID 须已在 sys_menu 中存在：2400 订单管理(M)、2403 第三方订单查询(C)、
--   24031 第三方订单-查询(F)、24032 第三方订单-详情(F)
-- 说明：
--   1) 同时插入 2400，避免仅有子菜单时侧栏/树形不显示父级。
--   2) hospital_id、supplier_id 置空串，与现网 uk(role_id,menu_id,hospital_id,supplier_id) 一致。
--   3) 若需包含「已停用」角色，去掉 AND r.status = '0' 条件即可。
--   4) 防重复：NOT EXISTS 按 role_id + menu_id + 空院/空商 判断，脚本可重复执行；
--      若表上已有 uk_role_menu_scope，亦可保留外层 INSERT IGNORE 作并发双保险（二选一即可）。
-- =============================================================================

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(), '-', ''), r.role_id, m.menu_id, '', ''
FROM sys_role r
CROSS JOIN (
  SELECT 2400 AS menu_id
  UNION ALL SELECT 2403
  UNION ALL SELECT 24031
  UNION ALL SELECT 24032
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
