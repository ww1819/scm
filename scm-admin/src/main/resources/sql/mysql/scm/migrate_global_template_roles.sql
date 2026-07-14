-- =============================================================================
-- SCM 全局模板角色迁移（按机构角色 → 4 个全局 SCM 模板角色）
-- 执行前请备份 sys_role / sys_role_menu / sys_user_role
-- 建议：先部署含本改动的应用版本，再在低峰期执行本脚本
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1) 创建 4 个全局模板角色（hospital_id、supplier_id 均为空）
-- -----------------------------------------------------------------------------
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '供应商管理员', 'supplier_admin', 5, '1', '0', '0', 'migration', NOW(), 'SCM全局模板角色', 'supplier', '1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role
    WHERE del_flag = '0' AND role_key = 'supplier_admin'
      AND (hospital_id IS NULL OR hospital_id = 0)
      AND (supplier_id IS NULL OR supplier_id = 0)
);

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '供应商业务员', 'supplier_sales', 15, '1', '0', '0', 'migration', NOW(), 'SCM全局模板角色', 'supplier', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role
    WHERE del_flag = '0' AND role_key = 'supplier_sales'
      AND (hospital_id IS NULL OR hospital_id = 0)
      AND (supplier_id IS NULL OR supplier_id = 0)
);

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '医院管理员', 'hospital_admin', 10, '1', '0', '0', 'migration', NOW(), 'SCM全局模板角色', 'hospital', '1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role
    WHERE del_flag = '0' AND role_key = 'hospital_admin'
      AND (hospital_id IS NULL OR hospital_id = 0)
      AND (supplier_id IS NULL OR supplier_id = 0)
);

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '医院职工', 'hospital_staff', 20, '1', '0', '0', 'migration', NOW(), 'SCM全局模板角色', 'hospital', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role
    WHERE del_flag = '0' AND role_key = 'hospital_staff'
      AND (hospital_id IS NULL OR hospital_id = 0)
      AND (supplier_id IS NULL OR supplier_id = 0)
);

-- -----------------------------------------------------------------------------
-- 2) 解析全局角色 ID
-- -----------------------------------------------------------------------------
SET @g_supplier_admin := (
    SELECT role_id FROM sys_role
    WHERE del_flag = '0' AND role_key = 'supplier_admin'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
    ORDER BY role_id LIMIT 1
);
SET @g_supplier_sales := (
    SELECT role_id FROM sys_role
    WHERE del_flag = '0' AND role_key = 'supplier_sales'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
    ORDER BY role_id LIMIT 1
);
SET @g_hospital_admin := (
    SELECT role_id FROM sys_role
    WHERE del_flag = '0' AND role_key = 'hospital_admin'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
    ORDER BY role_id LIMIT 1
);
SET @g_hospital_staff := (
    SELECT role_id FROM sys_role
    WHERE del_flag = '0' AND role_key = 'hospital_staff'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
    ORDER BY role_id LIMIT 1
);

-- -----------------------------------------------------------------------------
-- 3) 从最新机构角色复制默认菜单到全局模板（hospital_id、supplier_id 均为空）
-- -----------------------------------------------------------------------------
SET @src_supplier_admin := (
    SELECT r.role_id FROM sys_role r
    WHERE r.del_flag = '0' AND r.role_key = 'supplier_admin' AND r.supplier_id IS NOT NULL
    ORDER BY r.supplier_id DESC LIMIT 1
);
SET @src_supplier_sales := (
    SELECT r.role_id FROM sys_role r
    WHERE r.del_flag = '0' AND r.role_key = 'supplier_sales' AND r.supplier_id IS NOT NULL
    ORDER BY r.supplier_id DESC LIMIT 1
);
SET @src_hospital_admin := (
    SELECT r.role_id FROM sys_role r
    WHERE r.del_flag = '0' AND r.role_key = 'hospital_admin' AND r.hospital_id IS NOT NULL
    ORDER BY r.hospital_id DESC LIMIT 1
);
SET @src_hospital_staff := (
    SELECT r.role_id FROM sys_role r
    WHERE r.del_flag = '0' AND r.role_key = 'hospital_staff' AND r.hospital_id IS NOT NULL
    ORDER BY r.hospital_id DESC LIMIT 1
);

DELETE FROM sys_role_menu
WHERE role_id IN (@g_supplier_admin, @g_supplier_sales, @g_hospital_admin, @g_hospital_staff)
  AND COALESCE(NULLIF(TRIM(hospital_id), ''), '') = ''
  AND COALESCE(NULLIF(TRIM(supplier_id), ''), '') = '';

INSERT INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(), '-', ''), @g_supplier_admin, rm.menu_id, '', ''
FROM sys_role_menu rm
WHERE @src_supplier_admin IS NOT NULL AND rm.role_id = @src_supplier_admin
  AND COALESCE(NULLIF(TRIM(rm.hospital_id), ''), '') = ''
  AND COALESCE(NULLIF(TRIM(rm.supplier_id), ''), '') = ''
ON DUPLICATE KEY UPDATE hospital_id = VALUES(hospital_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(), '-', ''), @g_supplier_sales, rm.menu_id, '', ''
FROM sys_role_menu rm
WHERE @src_supplier_sales IS NOT NULL AND rm.role_id = @src_supplier_sales
  AND COALESCE(NULLIF(TRIM(rm.hospital_id), ''), '') = ''
  AND COALESCE(NULLIF(TRIM(rm.supplier_id), ''), '') = ''
ON DUPLICATE KEY UPDATE hospital_id = VALUES(hospital_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(), '-', ''), @g_hospital_admin, rm.menu_id, '', ''
FROM sys_role_menu rm
WHERE @src_hospital_admin IS NOT NULL AND rm.role_id = @src_hospital_admin
  AND COALESCE(NULLIF(TRIM(rm.hospital_id), ''), '') = ''
  AND COALESCE(NULLIF(TRIM(rm.supplier_id), ''), '') = ''
ON DUPLICATE KEY UPDATE hospital_id = VALUES(hospital_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(), '-', ''), @g_hospital_staff, rm.menu_id, '', ''
FROM sys_role_menu rm
WHERE @src_hospital_staff IS NOT NULL AND rm.role_id = @src_hospital_staff
  AND COALESCE(NULLIF(TRIM(rm.hospital_id), ''), '') = ''
  AND COALESCE(NULLIF(TRIM(rm.supplier_id), ''), '') = ''
ON DUPLICATE KEY UPDATE hospital_id = VALUES(hospital_id);

-- -----------------------------------------------------------------------------
-- 4) 用户改绑全局模板角色
-- -----------------------------------------------------------------------------
-- 4a 供应商主账号 → 全局 supplier_admin
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT su.user_id, @g_supplier_admin
FROM scm_supplier_user su
WHERE su.del_flag = '0' AND su.is_main = '1' AND su.user_id IS NOT NULL;

-- 4b 供应商业务员 → 全局 supplier_sales
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT su.user_id, @g_supplier_sales
FROM scm_supplier_user su
WHERE su.del_flag = '0' AND IFNULL(su.is_main, '0') <> '1' AND su.user_id IS NOT NULL;

-- 4c 原机构医院角色用户 → 对应全局医院角色
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT DISTINCT ur.user_id,
    CASE r.role_key
        WHEN 'hospital_admin' THEN @g_hospital_admin
        WHEN 'hospital_staff' THEN @g_hospital_staff
        ELSE NULL
    END
FROM sys_user_role ur
INNER JOIN sys_role r ON r.role_id = ur.role_id AND r.del_flag = '0'
WHERE r.hospital_id IS NOT NULL
  AND r.role_key IN ('hospital_admin', 'hospital_staff');

-- 4d 仍绑定在机构供应商角色上的用户（兜底）
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT DISTINCT ur.user_id,
    CASE r.role_key
        WHEN 'supplier_admin' THEN @g_supplier_admin
        WHEN 'supplier_admin1' THEN @g_supplier_admin
        WHEN 'supplier_sales' THEN @g_supplier_sales
        ELSE NULL
    END
FROM sys_user_role ur
INNER JOIN sys_role r ON r.role_id = ur.role_id AND r.del_flag = '0'
WHERE r.supplier_id IS NOT NULL
  AND r.role_key IN ('supplier_admin', 'supplier_admin1', 'supplier_sales');

-- -----------------------------------------------------------------------------
-- 5) 解除与机构内置角色的关联
-- -----------------------------------------------------------------------------
DELETE ur FROM sys_user_role ur
INNER JOIN sys_role r ON r.role_id = ur.role_id
WHERE r.del_flag = '0'
  AND (r.supplier_id IS NOT NULL OR r.hospital_id IS NOT NULL)
  AND r.role_key IN ('supplier_admin', 'supplier_admin1', 'supplier_sales', 'hospital_admin', 'hospital_staff');

-- -----------------------------------------------------------------------------
-- 6) 软删除机构内置角色（保留自定义机构角色可手工处理）
-- -----------------------------------------------------------------------------
UPDATE sys_role
SET del_flag = '2',
    update_by = 'migration',
    update_time = NOW(),
    remark = CONCAT(IFNULL(remark, ''), ' [已合并至全局模板角色]')
WHERE del_flag = '0'
  AND (supplier_id IS NOT NULL OR hospital_id IS NOT NULL)
  AND role_key IN ('supplier_admin', 'supplier_admin1', 'supplier_sales', 'hospital_admin', 'hospital_staff');

-- -----------------------------------------------------------------------------
-- 7) 清理已删除机构角色上的菜单关联（可选，减小表体积）
-- -----------------------------------------------------------------------------
DELETE rm FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id
WHERE r.del_flag = '2'
  AND r.role_key IN ('supplier_admin', 'supplier_admin1', 'supplier_sales', 'hospital_admin', 'hospital_staff');

-- -----------------------------------------------------------------------------
-- 8) 验证（执行后人工查看）
-- -----------------------------------------------------------------------------
-- SELECT role_key, COUNT(*) FROM sys_role WHERE del_flag='0' AND role_key IN ('supplier_admin','supplier_sales','hospital_admin','hospital_staff') GROUP BY role_key;
-- SELECT r.role_key, COUNT(DISTINCT ur.user_id) FROM sys_user_role ur JOIN sys_role r ON r.role_id=ur.role_id WHERE r.del_flag='0' GROUP BY r.role_key;
