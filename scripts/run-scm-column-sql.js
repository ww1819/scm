/**
 * 手工执行 SCM 增量 SQL：procedure.sql + column.sql
 *
 * 用法：
 *   node scripts/run-scm-column-sql.js
 *
 * 数据库连接优先级（后者覆盖前者）：
 *   1. 环境变量 SCM_DB_HOST / SCM_DB_PORT / SCM_DB_USER / SCM_DB_PASSWORD / SCM_DB_NAME
 *   2. scm-admin/src/main/resources/application-druid.yml 中 spring.datasource.druid.master
 */
const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

const SQL_BASE = path.join(__dirname, '../scm-admin/src/main/resources/sql/mysql/scm');
const DRUID_YML = path.join(__dirname, '../scm-admin/src/main/resources/application-druid.yml');

function parseStatements(content) {
  const statements = [];
  let current = [];
  for (const line of content.split(/\r?\n/)) {
    if (line.trim() === '/') {
      const stmt = current.join('\n').trim();
      if (stmt) {
        statements.push(stmt);
      }
      current = [];
    } else {
      current.push(line);
    }
  }
  const tail = current.join('\n').trim();
  if (tail) {
    statements.push(tail);
  }
  return statements;
}

function isCommentOrBlankOnly(sql) {
  const s = sql.trim();
  if (!s) {
    return true;
  }
  let inBlock = false;
  const kept = [];
  for (const line of s.split(/\r?\n/)) {
    const t = line.trim();
    if (inBlock) {
      if (t.endsWith('*/')) {
        inBlock = false;
      }
      continue;
    }
    if (t.startsWith('/*')) {
      inBlock = !t.includes('*/');
      continue;
    }
    if (!t || t.startsWith('--')) {
      continue;
    }
    kept.push(t);
  }
  return kept.join(' ').trim().length === 0;
}

function readDruidMasterConfig() {
  if (!fs.existsSync(DRUID_YML)) {
    return {};
  }
  const text = fs.readFileSync(DRUID_YML, 'utf8');
  const section = text.split('master:')[1];
  if (!section) {
    return {};
  }
  const pick = (key) => {
    const m = section.match(new RegExp('^\\s*' + key + ':\\s*(.+)$', 'm'));
    if (!m) {
      return '';
    }
    return m[1].trim().replace(/^['"]|['"]$/g, '');
  };
  const url = pick('url');
  let host = process.env.SCM_DB_HOST || '';
  let port = process.env.SCM_DB_PORT || '3306';
  let database = process.env.SCM_DB_NAME || '';
  if (url) {
    const hostMatch = url.match(/\/\/([^:/]+)(?::(\d+))?/);
    const dbMatch = url.match(/\/([^/?]+)(?:\?|$)/);
    if (hostMatch) {
      host = hostMatch[1];
      if (hostMatch[2]) {
        port = hostMatch[2];
      }
    }
    if (dbMatch) {
      database = dbMatch[1];
    }
  }
  return {
    host,
    port: Number(port, 10) || 3306,
    user: process.env.SCM_DB_USER || pick('username'),
    password: process.env.SCM_DB_PASSWORD || pick('password'),
    database
  };
}

function buildDbConfig() {
  const fromYml = readDruidMasterConfig();
  const cfg = {
    host: process.env.SCM_DB_HOST || fromYml.host,
    port: Number(process.env.SCM_DB_PORT || fromYml.port || 3306, 10),
    user: process.env.SCM_DB_USER || fromYml.user,
    password: process.env.SCM_DB_PASSWORD || fromYml.password,
    database: process.env.SCM_DB_NAME || fromYml.database,
    multipleStatements: true
  };
  if (!cfg.host || !cfg.user || !cfg.database) {
    throw new Error('数据库配置不完整，请设置 SCM_DB_* 环境变量或检查 application-druid.yml');
  }
  return cfg;
}

async function executeOne(conn, sql) {
  const [results] = await conn.query(sql);
  if (Array.isArray(results)) {
    for (const row of results) {
      if (Array.isArray(row)) {
        for (const item of row) {
          if (item && typeof item === 'object') {
            const msg = item['执行结果'] || item.add_table_index_result || item.result;
            if (msg) {
              console.log('  ->', msg);
            }
          }
        }
      } else if (row && typeof row === 'object') {
        const msg = row['执行结果'] || row.add_table_index_result || row.result;
        if (msg) {
          console.log('  ->', msg);
        }
      }
    }
  }
}

async function runScript(conn, scriptName) {
  const filePath = path.join(SQL_BASE, scriptName);
  if (!fs.existsSync(filePath)) {
    console.log(`跳过：脚本不存在 ${scriptName}`);
    return { ok: 0, skip: 0, fail: 0 };
  }
  console.log(`\n开始执行: ${scriptName}`);
  const statements = parseStatements(fs.readFileSync(filePath, 'utf8'));
  let ok = 0;
  let skip = 0;
  let fail = 0;
  for (const sql of statements) {
    if (isCommentOrBlankOnly(sql)) {
      skip++;
      continue;
    }
    try {
      await executeOne(conn, sql);
      ok++;
    } catch (e) {
      fail++;
      console.warn(`失败 [${scriptName}]: ${sql.substring(0, 100).replace(/\s+/g, ' ')}...`);
      console.warn(`  原因: ${e.message}`);
    }
  }
  console.log(`完成 ${scriptName}: 成功 ${ok}, 跳过 ${skip}, 失败 ${fail}`);
  return { ok, skip, fail };
}

async function main() {
  const cfg = buildDbConfig();
  console.log(`连接数据库 ${cfg.user}@${cfg.host}:${cfg.port}/${cfg.database}`);
  const conn = await mysql.createConnection(cfg);
  try {
    const procResult = await runScript(conn, 'procedure.sql');
    const columnResult = await runScript(conn, 'column.sql');
    const [rows] = await conn.query(
      "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'scm_reconciliation'"
    );
    if (rows.length > 0) {
      console.log('\n验证 scm_reconciliation 表: 已存在');
    }
    const totalFail = (procResult.fail || 0) + (columnResult.fail || 0);
    if (totalFail > 0) {
      console.warn(`\n注意: 共 ${totalFail} 条语句执行失败（多为已存在结构，可结合日志判断）`);
    } else {
      console.log('\n增量 SQL 执行完成。');
    }
  } finally {
    await conn.end();
  }
}

main().catch((e) => {
  console.error(e.message || e);
  process.exit(1);
});
