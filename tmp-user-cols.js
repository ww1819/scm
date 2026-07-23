const mysql = require('C:/Users/Administrator/AppData/Local/Temp/scm-mysql-tmp/node_modules/mysql2/promise');
(async () => {
  const c = await mysql.createConnection({
    host: 'rm-bp1tov1b3948fc5inbo.mysql.rds.aliyuncs.com',
    user: 'spd',
    password: 'Spd@456ww',
    database: 'scm_test'
  });
  const [cols] = await c.query('SHOW COLUMNS FROM sys_user');
  console.log(cols.map(x => x.Field + ':' + x.Type).join('\n'));
  await c.end();
})().catch(e => { console.error(e); process.exit(1); });
