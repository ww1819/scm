-- 配送单接口下载记录（scminterface 写入；scm 反审核时校验）
-- 主键 UUID7（36 位含连字符）；delivery_id 为 varchar 逻辑外键（存配送单 bigint 的十进制字符串）
DROP TABLE IF EXISTS scm_delivery_download_log;
CREATE TABLE scm_delivery_download_log (
  id                varchar(36)   NOT NULL COMMENT '主键 UUID7（36位含连字符）',
  delivery_id       varchar(32)   NOT NULL COMMENT '配送单ID（varchar 逻辑外键，与 scm_delivery.delivery_id 对应）',
  download_time     datetime      NOT NULL COMMENT '下载时间',
  download_channel  varchar(32) DEFAULT NULL COMMENT 'SPD_XML=SPD配送单接口;ZS_XML=第三方配送单接口',
  PRIMARY KEY (id),
  KEY idx_delivery_id (delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送单接口下载记录';

-- 按钮：配送单反审核（医院与供应商均可使用，与详情等一致走院商可见策略，见 menu.sql 中 25009 的 auth 配置）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark, status) VALUES ('25009', '配送单反审核', '2501', '9', '#', '', 'F', '0', '1', 'delivery:delivery:unaudit', '#', 'admin', sysdate(), '', null, '', '0');

INSERT IGNORE INTO scm_hospital_menu_auth (id, hospital_id, menu_id, create_by, create_time)
SELECT REPLACE(UUID(), '-', ''), CAST(hu.hospital_id AS CHAR), '25009', 'patch_scm_delivery_download_log', NOW()
FROM scm_hospital_user hu
WHERE (hu.del_flag = '0' OR hu.del_flag IS NULL)
  AND NOT EXISTS (
    SELECT 1 FROM scm_hospital_menu_auth a
    WHERE a.hospital_id = CAST(hu.hospital_id AS CHAR) AND a.menu_id = '25009'
  );

INSERT IGNORE INTO scm_supplier_menu_auth (id, supplier_id, hospital_id, menu_id, create_by, create_time)
SELECT REPLACE(UUID(), '-', ''), CAST(su.supplier_id AS CHAR), NULL, '25009', 'patch_scm_delivery_download_log', NOW()
FROM scm_supplier_user su
WHERE (su.del_flag = '0' OR su.del_flag IS NULL)
  AND NOT EXISTS (
    SELECT 1 FROM scm_supplier_menu_auth a
    WHERE a.supplier_id = CAST(su.supplier_id AS CHAR) AND IFNULL(a.hospital_id, '') = '' AND a.menu_id = '25009'
  );

-- 为所有有效角色赋予「配送单反审核」按钮权限（与 patch_sys_role_menu_tp_order_confirm_void 用法一致）
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, hospital_id, supplier_id)
SELECT REPLACE(UUID(), '-', ''), r.role_id, '25009', '', ''
FROM sys_role r
WHERE IFNULL(r.del_flag, '0') = '0'
  AND IFNULL(r.status, '0') = '0'
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_menu rm
    WHERE rm.role_id = r.role_id
      AND rm.menu_id = '25009'
      AND IFNULL(rm.hospital_id, '') = ''
      AND IFNULL(rm.supplier_id, '') = ''
  );
