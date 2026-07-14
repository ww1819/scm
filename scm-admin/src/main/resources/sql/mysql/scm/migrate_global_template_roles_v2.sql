-- =============================================================================
-- SCM 全局模板角色迁移 v2（6 角色 + 资质/订单分流）
--
-- 规则摘要：
--   1. 供应商：仅资质登记/维护，不含 certificate:*:audit（23005/23015/2001605）
--   2. 医院管理员：含供应商资质审核(2303)、产品证件审核(2304)
--   3. 普通供应商：订单查询(2401)，不含第三方订单(2403)
--   4. 第三方供应商（仅绑定伊犁州新华医院 hospital_id=46）：仅第三方订单(2403)
--
-- 执行前请备份：sys_role / sys_role_menu / sys_user_role / sys_menu
-- 建议：先部署含本改动的应用版本，再在低峰期执行
-- =============================================================================

SET @xh_hospital_id := 46;

-- -----------------------------------------------------------------------------
-- 0) 修正菜单元数据（历史库可能把 2301 误写成审核页）
-- -----------------------------------------------------------------------------
UPDATE sys_menu
SET menu_name = '供应商资质登记',
    parent_id = '2300',
    url = '/certificate/supplier',
    perms = 'certificate:supplier:view',
    auth_type = 'supplier',
    default_open_supplier = '1',
    default_open_hospital = '0',
    update_by = 'migration_v2',
    update_time = NOW()
WHERE del_flag = '0' AND menu_id = '2301';

UPDATE sys_menu
SET menu_name = '供应商资质审核',
    parent_id = '2300',
    url = '/certificate/supplier/audit',
    perms = 'certificate:supplier:audit',
    auth_type = 'hospital',
    default_open_supplier = '0',
    default_open_hospital = '1',
    update_by = 'migration_v2',
    update_time = NOW()
WHERE del_flag = '0' AND menu_id = '2303';

UPDATE sys_menu
SET menu_name = '产品证件审核',
    parent_id = '2300',
    url = '/certificate/product/audit',
    perms = 'certificate:product:audit',
    auth_type = 'hospital',
    default_open_supplier = '0',
    default_open_hospital = '1',
    update_by = 'migration_v2',
    update_time = NOW()
WHERE del_flag = '0' AND menu_id = '2304';

-- 登记侧审核按钮不对供应商默认开放
UPDATE sys_menu
SET default_open_supplier = '0',
    auth_type = IF(auth_type = 'hospital', auth_type, 'supplier'),
    update_by = 'migration_v2',
    update_time = NOW()
WHERE del_flag = '0'
  AND menu_id IN ('23005', '23015', '2001605');

-- -----------------------------------------------------------------------------
-- 1) 创建 6 个全局模板角色
-- -----------------------------------------------------------------------------
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '供应商管理员', 'supplier_admin', 5, '1', '0', '0', 'migration_v2', NOW(), 'SCM全局模板角色', 'supplier', '1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE del_flag = '0' AND role_key = 'supplier_admin'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
);

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '供应商业务员', 'supplier_sales', 15, '1', '0', '0', 'migration_v2', NOW(), 'SCM全局模板角色', 'supplier', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE del_flag = '0' AND role_key = 'supplier_sales'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
);

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '第三方供应商管理员', 'tp_supplier_admin', 6, '1', '0', '0', 'migration_v2', NOW(), 'SCM全局模板角色', 'supplier', '1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE del_flag = '0' AND role_key = 'tp_supplier_admin'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
);

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '第三方供应商业务员', 'tp_supplier_sales', 16, '1', '0', '0', 'migration_v2', NOW(), 'SCM全局模板角色', 'supplier', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE del_flag = '0' AND role_key = 'tp_supplier_sales'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
);

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '医院管理员', 'hospital_admin', 10, '1', '0', '0', 'migration_v2', NOW(), 'SCM全局模板角色', 'hospital', '1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE del_flag = '0' AND role_key = 'hospital_admin'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
);

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark, role_type, org_admin)
SELECT '医院职工', 'hospital_staff', 20, '1', '0', '0', 'migration_v2', NOW(), 'SCM全局模板角色', 'hospital', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE del_flag = '0' AND role_key = 'hospital_staff'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0)
);

SET @g_supplier_admin := (
    SELECT role_id FROM sys_role WHERE del_flag = '0' AND role_key = 'supplier_admin'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0) ORDER BY role_id LIMIT 1
);
SET @g_supplier_sales := (
    SELECT role_id FROM sys_role WHERE del_flag = '0' AND role_key = 'supplier_sales'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0) ORDER BY role_id LIMIT 1
);
SET @g_tp_supplier_admin := (
    SELECT role_id FROM sys_role WHERE del_flag = '0' AND role_key = 'tp_supplier_admin'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0) ORDER BY role_id LIMIT 1
);
SET @g_tp_supplier_sales := (
    SELECT role_id FROM sys_role WHERE del_flag = '0' AND role_key = 'tp_supplier_sales'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0) ORDER BY role_id LIMIT 1
);
SET @g_hospital_admin := (
    SELECT role_id FROM sys_role WHERE del_flag = '0' AND role_key = 'hospital_admin'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0) ORDER BY role_id LIMIT 1
);
SET @g_hospital_staff := (
    SELECT role_id FROM sys_role WHERE del_flag = '0' AND role_key = 'hospital_staff'
      AND (hospital_id IS NULL OR hospital_id = 0) AND (supplier_id IS NULL OR supplier_id = 0) ORDER BY role_id LIMIT 1
);

-- -----------------------------------------------------------------------------
-- 2) 第三方供应商：仅绑定新华医院、未绑定其他医院
-- -----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_tp_supplier;
CREATE TEMPORARY TABLE tmp_tp_supplier (
    supplier_id BIGINT PRIMARY KEY
);

INSERT INTO tmp_tp_supplier (supplier_id)
SELECT DISTINCT hs.supplier_id
FROM scm_hospital_supplier hs
WHERE (hs.del_flag IS NULL OR hs.del_flag = '0')
  AND hs.hospital_id = @xh_hospital_id
  AND NOT EXISTS (
      SELECT 1 FROM scm_hospital_supplier hs2
      WHERE (hs2.del_flag IS NULL OR hs2.del_flag = '0')
        AND hs2.supplier_id = hs.supplier_id
        AND hs2.hospital_id <> @xh_hospital_id
  );

-- -----------------------------------------------------------------------------
-- 3) 构建各角色菜单种子（临时表）
-- -----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_supplier_base_seed;
CREATE TEMPORARY TABLE tmp_supplier_base_seed (menu_id BIGINT PRIMARY KEY);

INSERT INTO tmp_supplier_base_seed (menu_id)
SELECT m.menu_id
FROM sys_menu m
WHERE m.del_flag = '0'
  AND m.default_open_supplier = '1'
  AND m.auth_type IN ('supplier', 'hospital_supplier')
  AND m.menu_id NOT IN ('23005', '23015', '2001605')
  AND IFNULL(m.perms, '') NOT LIKE '%:audit%';

DROP TEMPORARY TABLE IF EXISTS tmp_tp_order_subtree;
CREATE TEMPORARY TABLE tmp_tp_order_subtree (menu_id BIGINT PRIMARY KEY);

INSERT INTO tmp_tp_order_subtree (menu_id)
WITH RECURSIVE tp AS (
    SELECT menu_id FROM sys_menu WHERE del_flag = '0' AND menu_id = 2403
    UNION ALL
    SELECT m.menu_id FROM sys_menu m
    INNER JOIN tp ON m.parent_id = tp.menu_id
    WHERE m.del_flag = '0'
)
SELECT menu_id FROM tp;

DROP TEMPORARY TABLE IF EXISTS tmp_reg_order_subtree;
CREATE TEMPORARY TABLE tmp_reg_order_subtree (menu_id BIGINT PRIMARY KEY);

INSERT INTO tmp_reg_order_subtree (menu_id)
WITH RECURSIVE reg AS (
    SELECT menu_id FROM sys_menu WHERE del_flag = '0' AND menu_id = 2401
    UNION ALL
    SELECT m.menu_id FROM sys_menu m
    INNER JOIN reg ON m.parent_id = reg.menu_id
    WHERE m.del_flag = '0'
)
SELECT menu_id FROM reg;

-- 普通供应商管理员种子
DROP TEMPORARY TABLE IF EXISTS tmp_reg_admin_seed;
CREATE TEMPORARY TABLE tmp_reg_admin_seed (menu_id BIGINT PRIMARY KEY);

INSERT INTO tmp_reg_admin_seed (menu_id)
SELECT menu_id FROM tmp_supplier_base_seed
WHERE menu_id NOT IN (SELECT menu_id FROM tmp_tp_order_subtree);

INSERT IGNORE INTO tmp_reg_admin_seed (menu_id)
SELECT menu_id FROM tmp_reg_order_subtree;

INSERT IGNORE INTO tmp_reg_admin_seed (menu_id) VALUES (2400);

-- 第三方供应商管理员种子
DROP TEMPORARY TABLE IF EXISTS tmp_tp_admin_seed;
CREATE TEMPORARY TABLE tmp_tp_admin_seed (menu_id BIGINT PRIMARY KEY);

INSERT INTO tmp_tp_admin_seed (menu_id)
SELECT menu_id FROM tmp_supplier_base_seed
WHERE menu_id NOT IN (SELECT menu_id FROM tmp_reg_order_subtree)
  AND menu_id <> 2401;

INSERT IGNORE INTO tmp_tp_admin_seed (menu_id)
SELECT menu_id FROM tmp_tp_order_subtree;

INSERT IGNORE INTO tmp_tp_admin_seed (menu_id) VALUES (2400);

-- 医院角色种子
DROP TEMPORARY TABLE IF EXISTS tmp_hospital_seed;
CREATE TEMPORARY TABLE tmp_hospital_seed (menu_id BIGINT PRIMARY KEY);

INSERT INTO tmp_hospital_seed (menu_id)
SELECT m.menu_id
FROM sys_menu m
WHERE m.del_flag = '0'
  AND m.default_open_hospital = '1'
  AND m.auth_type IN ('hospital', 'hospital_supplier')
  AND m.auth_type <> 'platform';

-- -----------------------------------------------------------------------------
-- 4) 种子展开祖先链并写入 sys_role_menu
-- -----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_role_menu_closure;
CREATE TEMPORARY TABLE tmp_role_menu_closure (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 普通供应商管理员
INSERT INTO tmp_role_menu_closure (role_id, menu_id)
WITH RECURSIVE anc AS (
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    WHERE m.del_flag = '0' AND m.menu_id IN (SELECT menu_id FROM tmp_reg_admin_seed)
    UNION ALL
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    INNER JOIN anc a ON m.menu_id = a.parent_id
    WHERE m.del_flag = '0'
)
SELECT DISTINCT @g_supplier_admin, menu_id FROM anc;

-- 普通供应商业务员（排除 supplier_admin_only）
INSERT INTO tmp_role_menu_closure (role_id, menu_id)
WITH RECURSIVE anc AS (
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    WHERE m.del_flag = '0' AND m.menu_id IN (SELECT menu_id FROM tmp_reg_admin_seed)
      AND IFNULL(m.supplier_admin_only, '0') <> '1'
    UNION ALL
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    INNER JOIN anc a ON m.menu_id = a.parent_id
    WHERE m.del_flag = '0'
)
SELECT DISTINCT @g_supplier_sales, menu_id FROM anc;

-- 第三方供应商管理员
INSERT INTO tmp_role_menu_closure (role_id, menu_id)
WITH RECURSIVE anc AS (
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    WHERE m.del_flag = '0' AND m.menu_id IN (SELECT menu_id FROM tmp_tp_admin_seed)
    UNION ALL
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    INNER JOIN anc a ON m.menu_id = a.parent_id
    WHERE m.del_flag = '0'
)
SELECT DISTINCT @g_tp_supplier_admin, menu_id FROM anc;

-- 第三方供应商业务员
INSERT INTO tmp_role_menu_closure (role_id, menu_id)
WITH RECURSIVE anc AS (
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    WHERE m.del_flag = '0' AND m.menu_id IN (SELECT menu_id FROM tmp_tp_admin_seed)
      AND IFNULL(m.supplier_admin_only, '0') <> '1'
    UNION ALL
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    INNER JOIN anc a ON m.menu_id = a.parent_id
    WHERE m.del_flag = '0'
)
SELECT DISTINCT @g_tp_supplier_sales, menu_id FROM anc;

-- 医院管理员
INSERT INTO tmp_role_menu_closure (role_id, menu_id)
WITH RECURSIVE anc AS (
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    WHERE m.del_flag = '0' AND m.menu_id IN (SELECT menu_id FROM tmp_hospital_seed)
    UNION ALL
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    INNER JOIN anc a ON m.menu_id = a.parent_id
    WHERE m.del_flag = '0'
)
SELECT DISTINCT @g_hospital_admin, menu_id FROM anc;

-- 医院职工
INSERT INTO tmp_role_menu_closure (role_id, menu_id)
WITH RECURSIVE anc AS (
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    WHERE m.del_flag = '0' AND m.menu_id IN (SELECT menu_id FROM tmp_hospital_seed)
      AND IFNULL(m.hospital_admin_only, '0') <> '1'
    UNION ALL
    SELECT m.menu_id, m.parent_id FROM sys_menu m
    INNER JOIN anc a ON m.menu_id = a.parent_id
    WHERE m.del_flag = '0'
)
SELECT DISTINCT @g_hospital_staff, menu_id FROM anc;

DELETE FROM sys_role_menu
WHERE role_id IN (@g_supplier_admin, @g_supplier_sales, @g_tp_supplier_admin, @g_tp_supplier_sales,
                  @g_hospital_admin, @g_hospital_staff)
  AND COALESCE(NULLIF(TRIM(hospital_id), ''), '') = ''
  AND COALESCE(NULLIF(TRIM(supplier_id), ''), '') = '';

INSERT INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(), '-', ''), c.role_id, c.menu_id, '', ''
FROM tmp_role_menu_closure c
ON DUPLICATE KEY UPDATE hospital_id = VALUES(hospital_id);

-- -----------------------------------------------------------------------------
-- 5) 用户改绑全局模板角色（按普通/第三方分流）
-- -----------------------------------------------------------------------------
DELETE ur FROM sys_user_role ur
INNER JOIN sys_role r ON r.role_id = ur.role_id
WHERE r.del_flag = '0'
  AND r.role_key IN ('supplier_admin', 'supplier_sales', 'tp_supplier_admin', 'tp_supplier_sales',
                     'hospital_admin', 'hospital_staff')
  AND (r.hospital_id IS NULL OR r.hospital_id = 0)
  AND (r.supplier_id IS NULL OR r.supplier_id = 0);

-- 5a 普通供应商主账号
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT su.user_id, @g_supplier_admin
FROM scm_supplier_user su
WHERE (su.del_flag IS NULL OR su.del_flag = '0')
  AND su.is_main = '1'
  AND su.user_id IS NOT NULL
  AND su.supplier_id NOT IN (SELECT supplier_id FROM tmp_tp_supplier);

-- 5b 第三方供应商主账号
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT su.user_id, @g_tp_supplier_admin
FROM scm_supplier_user su
INNER JOIN tmp_tp_supplier t ON t.supplier_id = su.supplier_id
WHERE (su.del_flag IS NULL OR su.del_flag = '0')
  AND su.is_main = '1'
  AND su.user_id IS NOT NULL;

-- 5c 普通供应商业务员
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT su.user_id, @g_supplier_sales
FROM scm_supplier_user su
WHERE (su.del_flag IS NULL OR su.del_flag = '0')
  AND IFNULL(su.is_main, '0') <> '1'
  AND su.user_id IS NOT NULL
  AND su.supplier_id NOT IN (SELECT supplier_id FROM tmp_tp_supplier);

-- 5d 第三方供应商业务员
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT su.user_id, @g_tp_supplier_sales
FROM scm_supplier_user su
INNER JOIN tmp_tp_supplier t ON t.supplier_id = su.supplier_id
WHERE (su.del_flag IS NULL OR su.del_flag = '0')
  AND IFNULL(su.is_main, '0') <> '1'
  AND su.user_id IS NOT NULL;

-- 5e 医院用户（从机构角色兜底）
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

-- 5f 仍绑定机构供应商角色的用户（兜底，按供应商类型分流）
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT DISTINCT ur.user_id,
    CASE
        WHEN r.role_key IN ('supplier_admin', 'supplier_admin1') AND t.supplier_id IS NOT NULL THEN @g_tp_supplier_admin
        WHEN r.role_key IN ('supplier_admin', 'supplier_admin1') THEN @g_supplier_admin
        WHEN r.role_key = 'supplier_sales' AND t.supplier_id IS NOT NULL THEN @g_tp_supplier_sales
        WHEN r.role_key = 'supplier_sales' THEN @g_supplier_sales
        ELSE NULL
    END
FROM sys_user_role ur
INNER JOIN sys_role r ON r.role_id = ur.role_id AND r.del_flag = '0'
LEFT JOIN tmp_tp_supplier t ON t.supplier_id = r.supplier_id
WHERE r.supplier_id IS NOT NULL
  AND r.role_key IN ('supplier_admin', 'supplier_admin1', 'supplier_sales');

-- -----------------------------------------------------------------------------
-- 6) 解除与机构内置角色的关联
-- -----------------------------------------------------------------------------
DELETE ur FROM sys_user_role ur
INNER JOIN sys_role r ON r.role_id = ur.role_id
WHERE r.del_flag = '0'
  AND (r.supplier_id IS NOT NULL OR r.hospital_id IS NOT NULL)
  AND r.role_key IN ('supplier_admin', 'supplier_admin1', 'supplier_sales',
                     'hospital_admin', 'hospital_staff');

-- -----------------------------------------------------------------------------
-- 7) 软删除机构内置角色
-- -----------------------------------------------------------------------------
UPDATE sys_role
SET del_flag = '2',
    update_by = 'migration_v2',
    update_time = NOW(),
    remark = CONCAT(IFNULL(remark, ''), ' [已合并至全局模板角色v2]')
WHERE del_flag = '0'
  AND (supplier_id IS NOT NULL OR hospital_id IS NOT NULL)
  AND role_key IN ('supplier_admin', 'supplier_admin1', 'supplier_sales',
                   'hospital_admin', 'hospital_staff');

DELETE rm FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id
WHERE r.del_flag = '2'
  AND r.role_key IN ('supplier_admin', 'supplier_admin1', 'supplier_sales',
                     'hospital_admin', 'hospital_staff');

DROP TEMPORARY TABLE IF EXISTS tmp_tp_supplier;
DROP TEMPORARY TABLE IF EXISTS tmp_supplier_base_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_tp_order_subtree;
DROP TEMPORARY TABLE IF EXISTS tmp_reg_order_subtree;
DROP TEMPORARY TABLE IF EXISTS tmp_reg_admin_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_tp_admin_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_hospital_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_role_menu_closure;

-- -----------------------------------------------------------------------------
-- 8) 验证（执行后人工查看）
-- -----------------------------------------------------------------------------
-- SELECT role_key, COUNT(*) FROM sys_role WHERE del_flag='0'
--   AND role_key IN ('supplier_admin','supplier_sales','tp_supplier_admin','tp_supplier_sales','hospital_admin','hospital_staff')
-- GROUP BY role_key;
-- SELECT COUNT(*) AS tp_supplier_cnt FROM scm_hospital_supplier hs
--   WHERE hs.hospital_id=46 AND NOT EXISTS (SELECT 1 FROM scm_hospital_supplier x WHERE x.supplier_id=hs.supplier_id AND x.hospital_id<>46);
-- SELECT r.role_key, COUNT(DISTINCT ur.user_id) FROM sys_user_role ur JOIN sys_role r ON r.role_id=ur.role_id WHERE r.del_flag='0' GROUP BY r.role_key;
