# scm/sql（历史 / 手工脚本）

本目录**不会**被 `SqlInitRunner` 自动执行。

| 用途 | 去哪 |
|------|------|
| 服务启动要生效的结构变更 | `scm-admin/src/main/resources/sql/mysql/scm/`（见该处 README） |
| 需求手跑说明稿 | `docs/sql/`（定稿后并入官方目录，原稿改为指针） |
| Quartz / 若依初始化等 | 本目录保留，按需手工执行 |

请勿再往本目录追加「上线必跑」的增量 DDL。
