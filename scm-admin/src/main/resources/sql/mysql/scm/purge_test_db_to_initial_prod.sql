-- =============================================================================
-- 测试库 → 正式初始库：数据清理脚本（MySQL 8.0+，依赖 WITH RECURSIVE 裁剪部门）
--
-- 【执行前必读】
--   1. 全库备份；在业务低峰执行；先在克隆库验证。
--   2. 常量：保留医院用户 hospital_id = 45；保留供应商 supplier_id = 41；可按环境修改。
--   3. 院供关系：不保留任何绑定；上线后需在系统内重新建立医院-供应商关系。
--   4. Redis 会话/Token、第三方密钥、OSS 对象、application 配置：不在本脚本内，需运维单独处理；
--      清库后应清空 Redis 并令全员重新登录。
--   5. 若库未执行 column.sql 增量（无 tenant_id 等列），请先对齐结构或删除本脚本中对应 UPDATE 段。
--
-- 【总体需求（定稿记录）】
--   1) 保留用户：仅保留 role_key='admin' 的超级管理员、hospital_id=45 的医院用户、supplier_id=41 的供应商用户。
--   2) 同步裁剪仅与已删组织相关的部门节点（保留用户部门及祖先链以外部门标记删除）。
--   3) 保留医院信息（scm_hospital 全量保留）。
--   4) 供应商仅保留 supplier_id=41 主表行（公司、联系人、状态等），其余供应商删除。
--   5) 供应商证照策略：仅清空证照类子表与资质附件/快照，不删除 supplier_id=41 主表。
--   6) 院供关系从零开始：删除全部医院-供应商绑定及衍生（含普儒与全部医院）。
--   7) 业务数据清理：第一方订单、配送、结算及明细；条码、追溯、第三方订单(zs_tp_*)、申请单、统计等全清。
--   8) 通知与日志清理：系统通知、操作日志、登录日志、在线会话日志、菜单变更日志、任务日志全清。
--   9) 产品域清理：产品主表全部删除；用户产品档案、产品证照、产品证照扩展/快照全部删除。
--  10) 删除所有非普儒供应商数据及其用户、院供衍生映射/权限范围/申请等关联数据。
--  11) 租户域策略：所有租户管理相关表数据全部删除（当前系统未启用租户管理）。
--  12) 角色策略：删除租户级/越界医院或供应商专属角色；保留 admin 与 45/41 范围内合法角色关联。
--  13) sys_user_role 策略：保留用户仅保留与「医院 / 供应商 / admin」一致的角色关联，其它删除。
--  14) tenant_id 处理：对保留行 tenant_id（及相关 spd_tenant_id 快照）按脚本规则置空/清理，避免残留租户语义。
--  15) 非 SQL 范围：Redis Token/在线会话、第三方密钥、回调 URL、测试 OSS 路径由运维清理，不进初始库镜像；
--      清库后强制全量用户重新登录。
--
-- =============================================================================

SET NAMES utf8mb4;

/* ========= 可调整常量 ========= */
SET @KEEP_HOSPITAL_ID := 45;
SET @KEEP_SUPPLIER_ID := 41;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------------
-- 1) 保留用户清单（执行前快照，避免后续角色清理影响判定）
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_keep_user;
CREATE TEMPORARY TABLE tmp_keep_user (user_id BIGINT NOT NULL PRIMARY KEY);

INSERT INTO tmp_keep_user (user_id)
SELECT DISTINCT u.user_id
FROM sys_user u
JOIN sys_user_role ur ON ur.user_id = u.user_id
JOIN sys_role r ON r.role_id = ur.role_id AND r.del_flag = '0' AND r.role_key = 'admin'
UNION
SELECT user_id FROM scm_hospital_user WHERE hospital_id = @KEEP_HOSPITAL_ID
UNION
SELECT user_id FROM scm_supplier_user WHERE supplier_id = @KEEP_SUPPLIER_ID;

-- 无保留用户时中止（防误删）
SELECT COUNT(*) AS keep_user_count FROM tmp_keep_user;

-- ---------------------------------------------------------------------------
-- 2) 业务与第三方：订单 / 配送 / 结算 / 关联 / 条码 / 统计
-- ---------------------------------------------------------------------------
DELETE FROM zs_tp_order_detail_delivery_rel;
DELETE FROM zs_tp_order_detail;
DELETE FROM zs_tp_order;

DELETE FROM scm_order_detail_delivery_rel;
DELETE FROM scm_order_detail;
DELETE FROM scm_order;

DELETE FROM scm_delivery_detail_barcode;
DELETE FROM scm_delivery_detail;
DELETE FROM scm_delivery_invoice;
DELETE FROM scm_delivery;

DELETE FROM scm_settlement_detail;
DELETE FROM scm_settlement;

DELETE FROM scm_purchase_statistics;

DELETE FROM scm_barcode_seed;

DELETE FROM scm_spd_tenant_bind;
DELETE FROM scm_spd_supplier_bind;
DELETE FROM scm_supplier_export_log;

TRUNCATE TABLE scm_supplier_user_apply;

-- ---------------------------------------------------------------------------
-- 3) 院供关系及衍生（含全部医院×全部供应商，不保留绑定）
-- ---------------------------------------------------------------------------
DELETE FROM scm_hospital_supplier_permission;
DELETE FROM scm_supplier_cert_apply_bundle;
DELETE FROM scm_hospital_supplier_apply_log;
DELETE FROM scm_hospital_supplier_apply;
DELETE FROM scm_hospital_supplier_change_log;
DELETE FROM scm_supplier_code_mapping;
DELETE FROM scm_hospital_supplier;

-- 供应商菜单白名单：仅保留本平台保留供应商
DELETE FROM scm_supplier_menu_auth
WHERE supplier_id <> CAST(@KEEP_SUPPLIER_ID AS CHAR);

-- ---------------------------------------------------------------------------
-- 4) 产品主数据、证照（含供应商证照子表；supplier 41 主表行保留）
-- ---------------------------------------------------------------------------
DELETE FROM scm_product_cert_license_snap;
DELETE FROM scm_product_certificate_aux;
DELETE FROM scm_product_certificate;

DELETE FROM scm_material_dict;
DELETE FROM scm_material_category;
DELETE FROM scm_manufacturer;

DELETE FROM scm_supplier_cert_change_log;
DELETE FROM scm_supplier_certificate;

-- ---------------------------------------------------------------------------
-- 5) 租户域（当前未启用租户管理亦清空）
-- ---------------------------------------------------------------------------
DELETE FROM scm_tenant_menu_pause_log;
DELETE FROM scm_tenant_menu_pause;
DELETE FROM scm_tenant_menu;
DELETE FROM scm_tenant_modify_log;
DELETE FROM scm_tenant_status_log;
DELETE FROM scm_tenant_status_period;
DELETE FROM scm_tenant;

-- ---------------------------------------------------------------------------
-- 6) 非保留供应商及其与用户的关联
-- ---------------------------------------------------------------------------
DELETE FROM scm_supplier_user WHERE supplier_id <> @KEEP_SUPPLIER_ID;
DELETE FROM scm_supplier WHERE supplier_id <> @KEEP_SUPPLIER_ID;

-- ---------------------------------------------------------------------------
-- 7) 角色：租户维度、越界院/商专属角色 → del_flag='2'，并清理关联
-- ---------------------------------------------------------------------------
UPDATE sys_role
SET del_flag = '2', update_time = NOW()
WHERE del_flag = '0'
  AND role_key <> 'admin'
  AND (
        (tenant_id IS NOT NULL AND TRIM(tenant_id) <> '')
     OR (hospital_id IS NOT NULL AND hospital_id NOT IN (0, @KEEP_HOSPITAL_ID))
     OR (supplier_id IS NOT NULL AND supplier_id NOT IN (0, @KEEP_SUPPLIER_ID))
      );

DELETE rd FROM sys_role_dept rd
JOIN sys_role r ON r.role_id = rd.role_id
WHERE r.del_flag = '2';

DELETE rm FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
WHERE r.del_flag = '2';

DELETE ur FROM sys_user_role ur
JOIN sys_role r ON r.role_id = ur.role_id
WHERE r.del_flag = '2';

-- ---------------------------------------------------------------------------
-- 8) 删除非保留用户及其关联
-- ---------------------------------------------------------------------------
DELETE up FROM sys_user_post up
LEFT JOIN tmp_keep_user k ON k.user_id = up.user_id
WHERE k.user_id IS NULL;

DELETE ur FROM sys_user_role ur
LEFT JOIN tmp_keep_user k ON k.user_id = ur.user_id
WHERE k.user_id IS NULL;

DELETE FROM sys_user
WHERE user_id NOT IN (SELECT user_id FROM tmp_keep_user);

DELETE FROM scm_hospital_user
WHERE hospital_id <> @KEEP_HOSPITAL_ID
   OR user_id NOT IN (SELECT user_id FROM tmp_keep_user);

DELETE FROM scm_supplier_user
WHERE supplier_id <> @KEEP_SUPPLIER_ID
   OR user_id NOT IN (SELECT user_id FROM tmp_keep_user);

-- ---------------------------------------------------------------------------
-- 9) 部门裁剪：保留「保留用户 dept」及其祖先链，其余标记删除
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_keep_dept;
CREATE TEMPORARY TABLE tmp_keep_dept (dept_id BIGINT NOT NULL PRIMARY KEY);

INSERT INTO tmp_keep_dept (dept_id)
WITH RECURSIVE dk AS (
    SELECT d.dept_id, d.parent_id
    FROM sys_dept d
    JOIN sys_user u ON u.dept_id = d.dept_id
    JOIN tmp_keep_user k ON k.user_id = u.user_id
    WHERE u.dept_id IS NOT NULL
    UNION ALL
    SELECT p.dept_id, p.parent_id
    FROM sys_dept p
    JOIN dk ON p.dept_id = dk.parent_id
)
SELECT DISTINCT dept_id FROM dk;

-- MySQL 限制：同一条语句不能多次打开同一 TEMPORARY 表（JOIN 与 EXISTS 子查询各算一次 → 1137 Can't reopen table）。
SET @dept_keep_cnt := (SELECT COUNT(*) FROM tmp_keep_dept);

UPDATE sys_dept d
LEFT JOIN tmp_keep_dept k ON k.dept_id = d.dept_id
SET d.del_flag = '2', d.update_time = NOW()
WHERE d.del_flag = '0'
  AND k.dept_id IS NULL
  AND @dept_keep_cnt > 0;

-- ---------------------------------------------------------------------------
-- 10) 通知、日志、在线会话（库内）
-- ---------------------------------------------------------------------------
DELETE FROM scm_notice_receiver;
DELETE FROM sys_notice;

DELETE FROM sys_menu_change_log;
DELETE FROM sys_oper_log;
DELETE FROM sys_logininfor;
DELETE FROM sys_user_online;
DELETE FROM sys_job_log;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- 11) 保留行上的租户/SPD 快照字段置空（列须已存在；不存在则整段注释掉）
-- ---------------------------------------------------------------------------
UPDATE scm_hospital SET tenant_id = NULL WHERE 1 = 1;
UPDATE scm_supplier SET tenant_id = NULL WHERE supplier_id = @KEEP_SUPPLIER_ID;
UPDATE sys_dept SET tenant_id = NULL WHERE 1 = 1;
UPDATE sys_user u
INNER JOIN tmp_keep_user k ON u.user_id = k.user_id
SET u.tenant_id = NULL;

-- ---------------------------------------------------------------------------
-- 12) 结果核对（可选）
-- ---------------------------------------------------------------------------
SELECT 'keep_users' AS item, COUNT(*) AS cnt FROM tmp_keep_user
UNION ALL SELECT 'scm_hospital', COUNT(*) FROM scm_hospital WHERE del_flag = '0'
UNION ALL SELECT 'scm_supplier', COUNT(*) FROM scm_supplier WHERE del_flag = '0'
UNION ALL SELECT 'scm_hospital_supplier', COUNT(*) FROM scm_hospital_supplier
UNION ALL SELECT 'scm_material_dict', COUNT(*) FROM scm_material_dict WHERE del_flag = '0'
UNION ALL SELECT 'scm_order', COUNT(*) FROM scm_order
UNION ALL SELECT 'sys_user', COUNT(*) FROM sys_user WHERE del_flag = '0';

SELECT u.user_id, u.login_name, u.user_name, u.user_type
FROM sys_user u
ORDER BY u.user_id;

-- 无角色用户（若存在需在库内补挂医院/供应商/admin 角色后上线）
SELECT u.user_id, u.login_name, u.user_name
FROM sys_user u
LEFT JOIN sys_user_role ur ON ur.user_id = u.user_id
WHERE u.del_flag = '0'
GROUP BY u.user_id, u.login_name, u.user_name
HAVING COUNT(ur.role_id) = 0;
