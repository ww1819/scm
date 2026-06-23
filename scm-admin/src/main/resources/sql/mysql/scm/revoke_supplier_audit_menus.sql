-- 收回所有供应商账号的「供应商资质审核」「产品证件审核」菜单权限
-- 作用范围：供应商角色 sys_role_menu、供应商菜单白名单 scm_supplier_menu_auth
-- 执行后请供应商用户重新登录

-- ========== 执行前查看（可选） ==========
-- SELECT m.menu_id, m.menu_name, m.menu_type, m.url, m.perms
-- FROM sys_menu m
-- WHERE m.del_flag = '0'
--   AND (
--     m.perms IN ('certificate:supplier:audit', 'certificate:product:audit')
--     OR (m.menu_type IN ('C', 'M') AND m.url LIKE '/certificate/supplier/audit%')
--     OR (m.menu_type IN ('C', 'M') AND m.url LIKE '/certificate/product/audit%')
--     OR m.menu_name IN ('供应商资质审核', '产品证件审核', '产品证件审核1')
--   )
-- ORDER BY m.menu_id;

-- ========== 1. 供应商角色菜单：删除审核相关 ==========
DELETE rm
FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id AND r.del_flag = '0'
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id AND m.del_flag = '0'
WHERE r.role_type = 'supplier'
  AND (
    m.perms IN ('certificate:supplier:audit', 'certificate:product:audit')
    OR (m.menu_type IN ('C', 'M') AND m.url LIKE '/certificate/supplier/audit%')
    OR (m.menu_type IN ('C', 'M') AND m.url LIKE '/certificate/product/audit%')
    OR m.menu_name IN ('供应商资质审核', '产品证件审核', '产品证件审核1')
  );

-- ========== 2. 供应商菜单白名单：删除审核相关 ==========
DELETE a
FROM scm_supplier_menu_auth a
INNER JOIN sys_menu m ON m.menu_id = a.menu_id AND m.del_flag = '0'
WHERE m.perms IN ('certificate:supplier:audit', 'certificate:product:audit')
   OR (m.menu_type IN ('C', 'M') AND m.url LIKE '/certificate/supplier/audit%')
   OR (m.menu_type IN ('C', 'M') AND m.url LIKE '/certificate/product/audit%')
   OR m.menu_name IN ('供应商资质审核', '产品证件审核', '产品证件审核1');

-- ========== 执行后验证（可选） ==========
-- SELECT r.role_name, m.menu_id, m.menu_name, m.perms
-- FROM sys_role_menu rm
-- JOIN sys_role r ON r.role_id = rm.role_id AND r.del_flag = '0'
-- JOIN sys_menu m ON m.menu_id = rm.menu_id
-- WHERE r.role_type = 'supplier'
--   AND (m.perms IN ('certificate:supplier:audit','certificate:product:audit')
--        OR m.url LIKE '%/audit%')
-- ORDER BY r.role_id, m.menu_id;
