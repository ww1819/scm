# SCM 数据库脚本（启动自动执行）

> **唯一权威目录**：本目录由 `SqlInitRunner` 在服务启动时加载。  
> 不要再往 `scm/sql/`、`docs/sql/` 堆「必须执行」的增量脚本；需求稿可放 `docs/sql/`，定稿后**并入本目录**。

## 执行顺序

| 时机 | 脚本（逗号分隔配置于 `scm.sql.init.*`） |
|------|----------------------------------------|
| **空库全量** | `table.sql` → `procedure.sql` → `column.sql` → `view.sql` → `trigger.sql` → `function.sql` → `menu.sql` → `data_integrity.sql` |
| **版本升级**（`upgrade-only=true` 且 `scm.version` 变更） | `procedure.sql` → `column.sql` → `menu.sql` |

触发条件：`scm.sql.init.enabled=true`，且 `scm.version` ≠ 库中 `sys_config.scm.sql.init.applied_version`。

## 各文件职责（勿混放）

| 文件 | 职责 | 写法要点 |
|------|------|----------|
| `table.sql` | **新建表**全量定义 | `CREATE TABLE IF NOT EXISTS`；新表字段在此齐全 |
| `procedure.sql` | 幂等工具存储过程 | 须在 `column.sql` **之前**；含 `add_table_column` / `add_table_index` 等 |
| `column.sql` | **存量升级**：加列、加索引、改类型/注释、数据回填、升级期建新表 | 每语句后单独一行 `/`；加列用 `CALL add_table_column(...)`；新表可再写一份 `CREATE TABLE IF NOT EXISTS`（与 table 双轨） |
| `menu.sql` | 菜单 / 权限点 | `INSERT IGNORE` |
| `view.sql` / `trigger.sql` / `function.sql` | 视图、触发器、函数 | 按需维护 |
| `data_integrity.sql` | 种子数据 / 完整性修补 | 幂等 INSERT |

同目录下 `fix_*`、`migrate_*`、`order_receive_columns.sql`、`insert_*` 等为**手工/历史**脚本，**不会**被启动器执行；有用内容应已并入上表正式文件。

## 分隔符

- 语句之间用**单独一行**的 `/` 分段（不是 `;`）。
- `;` 可出现在语句内部（如过程体）。

## 合并新需求时的检查清单

1. [ ] 新表 → 写入 `table.sql`，并在 `column.sql` 追加 `CREATE TABLE IF NOT EXISTS`（老库升级用）
2. [ ] 加列 → `table.sql` 建表定义补全 + `column.sql` 的 `CALL add_table_column`
3. [ ] 加索引 → `CALL add_table_index`
4. [ ] 改类型/注释 → `column.sql` 的 `ALTER … MODIFY … COMMENT`
5. [ ] 菜单 → `menu.sql` 的 `INSERT IGNORE`
6. [ ] 主键约定 → 新表 UUID7 / 外键字符串（见约定包 §2.1）
7. [ ] **提升 `scm.version`**（如 `4.8.2` → `4.8.3`），否则升级链不跑
8. [ ] `docs/sql/需求编号-*.sql` 改为指向本目录的说明，避免双源

## 当前版本

与 `application.yml` 中 `scm.version` 保持一致（当前 **4.8.3**：合单配送 + 配送明细 `combined_id/combined_no`）。
