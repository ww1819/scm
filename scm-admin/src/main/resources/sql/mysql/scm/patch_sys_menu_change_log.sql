-- 菜单变更审计表（存量环境在 table.sql 之后执行一次即可）
CREATE TABLE IF NOT EXISTS `sys_menu_change_log` (
  `log_id` varchar(36) NOT NULL COMMENT '主键 UUID7 风格（应用侧 IdUtils.dashedUuid7，36位带-）',
  `menu_id` varchar(36) NOT NULL COMMENT '菜单ID（sys_menu.menu_id 的字符形式，逻辑关联不设 DB 外键）',
  `change_type` char(1) NOT NULL COMMENT 'I新增 U修改 D删除 S排序',
  `oper_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `oper_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `menu_snapshot` longtext COMMENT 'JSON：before/after 菜单字段快照',
  PRIMARY KEY (`log_id`),
  KEY `idx_menu_change_menu_time` (`menu_id`, `oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单变更记录';
