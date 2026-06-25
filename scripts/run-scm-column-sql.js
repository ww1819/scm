const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

const DB = {
  host: 'rm-bp1tov1b3948fc5inbo.mysql.rds.aliyuncs.com',
  port: 3306,
  user: 'spd',
  password: 'Spd@456ww',
  database: 'scm_test',
  multipleStatements: true
};

const SQL_BASE = path.join(__dirname, '../scm-admin/src/main/resources/sql/mysql/scm');

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
  const conn = await mysql.createConnection(DB);
  try {
    await runScript(conn, 'procedure.sql');
    const columnResult = await runScript(conn, 'column.sql');
    const [rows] = await conn.query(
      "SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'scm_supplier_certificate' AND COLUMN_NAME = 'register_date'"
    );
    if (rows.length > 0) {
      console.log('\n验证 register_date 字段:', rows[0]);
    } else {
      console.error('\nregister_date 字段仍未创建，请检查 column.sql 执行日志');
      process.exitCode = 1;
    }
    if (columnResult.fail > 0) {
      console.warn(`\n注意: column.sql 有 ${columnResult.fail} 条语句执行失败（多为已存在结构，可结合日志判断）`);
    }
  } finally {
    await conn.end();
  }
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
