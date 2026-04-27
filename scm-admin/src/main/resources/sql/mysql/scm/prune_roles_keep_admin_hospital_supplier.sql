-- =============================================================================
-- 角色精简：仅保留「超级管理员」「医院」「供应商」三类可用角色（其余逻辑删除）
-- 执行前请备份数据库；建议在业务低峰执行。
--
-- 保留规则（在 del_flag='0' 的角色中）：
--   1) 超级管理员：role_key = 'admin'，取最小 role_id
--   2) 医院：role_name = '医院' 或 role_key = 'hospital_admin'，取最小 role_id
--   3) 供应商：优先 role_key = 'supplier' 且 tenant 维度为空、非某供应商专属行；
--             若无则取 role_name = '供应商' 且 supplier_id 为空、tenant_id 为空，取最小 role_id
--
-- 说明：
--   - 租户/供应商注册自动创建的「医院管理员」「供应商业务员」等带 tenant_id/supplier_id 的角色
--     不在上述「全局保留」范围内，会被一并清理。
--   - 若需长期只保留三角色，请同步调整 TenantAdminCreateServiceImpl、SupplierRegisterServiceImpl 等
--     否则会再次生成租户级角色。
-- =============================================================================

SET @keep_admin := (
    SELECT MIN(role_id) FROM sys_role WHERE del_flag = '0' AND role_key = 'admin'
);
SET @keep_hospital := (
    SELECT MIN(role_id) FROM sys_role
    WHERE del_flag = '0' AND (role_name = '医院' OR role_key = 'hospital_admin')
);
SET @keep_supplier := (
    SELECT MIN(role_id) FROM sys_role
    WHERE del_flag = '0'
      AND role_key = 'supplier'
      AND (supplier_id IS NULL OR supplier_id = 0)
      AND (tenant_id IS NULL OR tenant_id = '')
);
SET @keep_supplier := COALESCE(@keep_supplier, (
    SELECT MIN(role_id) FROM sys_role
    WHERE del_flag = '0'
      AND role_name = '供应商'
      AND (supplier_id IS NULL OR supplier_id = 0)
      AND (tenant_id IS NULL OR tenant_id = '')
));

-- 无超级管理员则中止（避免误删）
SELECT IF(@keep_admin IS NULL, 'ERROR: 未找到 role_key=admin 的超级管理员，已跳过执行', 'OK') AS check_admin;

-- 若上一步为 ERROR，请勿继续执行后续语句（未找到 admin 时下列 UPDATE 不会生效）
UPDATE sys_role r
SET r.del_flag = '2',
    r.update_time = NOW()
WHERE r.del_flag = '0'
  AND @keep_admin IS NOT NULL
  AND r.role_id <> @keep_admin
  AND (COALESCE(@keep_hospital, 0) = 0 OR r.role_id <> @keep_hospital)
  AND (COALESCE(@keep_supplier, 0) = 0 OR r.role_id <> @keep_supplier);

-- 清理关联（物理删除，避免孤儿数据）
DELETE ur FROM sys_user_role ur
INNER JOIN sys_role r ON ur.role_id = r.role_id
WHERE r.del_flag = '2';

DELETE rm FROM sys_role_menu rm
INNER JOIN sys_role r ON rm.role_id = r.role_id
WHERE r.del_flag = '2';

DELETE rd FROM sys_role_dept rd
INNER JOIN sys_role r ON rd.role_id = r.role_id
WHERE r.del_flag = '2';

-- 结果核对
SELECT role_id, role_name, role_key, tenant_id, supplier_id, del_flag
FROM sys_role
WHERE del_flag = '0'
ORDER BY role_sort, role_id;
