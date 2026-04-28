# SCM 权限模型升级 — 执行顺序与验证清单

## 一、数据库脚本执行顺序（新环境 / 全量）

1. `sql/mysql/scm/table.sql` — 含 `sys_menu`/`sys_role` 扩展字段；三张授权表白名单表亦可由 SPD 侧 `material/table.sql` 建表  
2. `sql/mysql/scm/column.sql` — 存量库增量列、`sys_menu.auth_type` 等归类、`scm_*` 白名单回填等  
3. `sql/mysql/scm/menu.sql` — 菜单数据；其中 **SCM「数据权限」独占 `2850`–`2854` 段**（含按钮 `28511`–`28542`）。**勿与「客户管理」混淆：`2800`–`2802` 为客户管理菜单**，与 SCM 无关。存量库若从未出现过「数据权限」侧栏，可补充执行 `patch_insert_scm_data_scope_menus_285x.sql`。

## 二、上线后必做（平台管理员）

- **非** `user_id = 1` 的平台用户：在 **角色管理** 中为角色勾选 **数据权限**（根 `menu_id = 2850`）及子菜单，否则看不到授权页面。超级管理员（`user_id = 1`）走全量菜单逻辑，无需在角色里额外勾选。  
- 执行 `column.sql` 后若仍有用户侧栏为空：确认该用户已在 `scm_hospital_user` / `scm_supplier_user` 中且 `column.sql` 白名单回填已执行。

## 三、功能验证清单

| 场景 | 步骤 | 期望 |
|------|------|------|
| 新供应商注册 | 走注册流程 | 自动创建 `supplier_admin` 角色、`scm_supplier_menu_auth` 全量供应商类菜单、`sys_role_menu` 同步；用户仅绑定该角色 |
| 新医院 | 后台新增医院 | 自动创建 `hospital_admin` 角色及医院菜单白名单与角色菜单 |
| 菜单可见性 | 供应商/医院用户登录 | 仅见「角色菜单 ∩ 白名单」且 `auth_type` 为 hospital/supplier 时需白名单行存在 |
| 医院菜单授权 | 打开 `/scm/auth/hospitalMenu`，选医院、改勾选、保存 | `scm_hospital_menu_auth` 与医院管理员 `sys_role_menu` 与勾选一致；重置后恢复全量医院类菜单 |
| 供应商菜单授权 | `/scm/auth/supplierMenu` | 同上 |
| 黑名单 | `/scm/auth/hospitalSupplierPerm` 维护禁止提交 | 该对 `(hospital_id, supplier_id)` 下供应商保存订单/配送单时报错；列表/导出按供应商维度隐藏被禁止医院（配送/订单列表） |
| 关联医院 | 供应商用户在供应商资料中勾选医院 | 若禁止关联则保存失败；平台用户不受黑名单限制 |
| 角色列表 | 系统管理-角色 | 展示角色类型、绑定医院、绑定供应商名称 |

## 四、回归注意

- 全局角色查询 `selectGlobalRoleByKey/Name` 已限制为 `role_type` 为空或 `platform`，且 `hospital_id` 为空，避免匹配到医院/供应商专属角色。  
- 老数据依赖 `column.sql` 中白名单回填；若手工删过白名单需用「重置」或重新授权。
