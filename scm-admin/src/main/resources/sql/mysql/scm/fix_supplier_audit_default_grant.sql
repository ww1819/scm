-- 一次性修复：供应商注册不应默认出现「供应商资质审核」页面
-- 根因：登记页按钮(23001-23006)误挂在审核页(2301)下；bootstrap 扩祖先时把审核页一并带入供应商角色。
-- 建议执行后重启应用，并让供应商用户重新登录。

-- 1) 登记页按钮改挂到「供应商资质登记」(20016)
UPDATE sys_menu
SET parent_id = '20016', update_by = 'sql_fix_final', update_time = NOW()
WHERE del_flag = '0' AND parent_id = '2301' AND menu_type = 'F';

-- 2) 关闭审核页及 audit 权限相关菜单的默认院/商开放
UPDATE sys_menu
SET default_open_scope = 'none',
    default_open_hospital = '0',
    default_open_supplier = '0',
    update_by = 'sql_fix_final',
    update_time = NOW()
WHERE del_flag = '0'
  AND (
    perms = 'certificate:supplier:audit'
    OR (menu_type IN ('C', 'M') AND url LIKE '/certificate/supplier/audit%')
  );

UPDATE sys_menu
SET auth_type = 'hospital',
    hospital_grant_supplier_flag = '0',
    default_open_scope = 'none',
    default_open_hospital = '0',
    default_open_supplier = '0',
    menu_biz_category = 'certificate',
    update_by = 'sql_fix_final',
    update_time = NOW()
WHERE del_flag = '0' AND menu_id = '2301';

-- 3) 回收全部历史授权
DELETE FROM scm_hospital_menu_auth
WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu
    WHERE del_flag = '0'
      AND (perms = 'certificate:supplier:audit' OR (menu_type IN ('C','M') AND url LIKE '/certificate/supplier/audit%'))
  ) t
);

DELETE FROM scm_supplier_menu_auth
WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu
    WHERE del_flag = '0'
      AND (perms = 'certificate:supplier:audit' OR (menu_type IN ('C','M') AND url LIKE '/certificate/supplier/audit%'))
  ) t
);

DELETE rm FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id
WHERE r.del_flag = '0'
  AND r.role_type IN ('hospital', 'supplier')
  AND rm.menu_id IN (
    SELECT menu_id FROM (
      SELECT menu_id FROM sys_menu
      WHERE del_flag = '0'
        AND (perms = 'certificate:supplier:audit' OR (menu_type IN ('C','M') AND url LIKE '/certificate/supplier/audit%'))
    ) t
  );
