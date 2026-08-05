# 从 TSV（省\t市\t区）生成幂等导入 SQL：医承云配 → 省 → 市 → 区县
# 用法: powershell -File generate_multi_province_regions_sql.ps1 <tsvPath> <outSqlPath>
$ErrorActionPreference = "Stop"
if ($args.Count -lt 2) { throw "Usage: generate_multi_province_regions_sql.ps1 <tsvPath> <outSqlPath>" }
$src = $args[0]
$out = $args[1]
$utf8 = New-Object System.Text.UTF8Encoding $false
if (-not (Test-Path -LiteralPath $src)) { throw "Source not found: $src" }

function SqlEsc([string]$s) {
    if ($null -eq $s) { return "" }
    return $s.Replace("'", "''")
}

$provSet = New-Object System.Collections.Generic.List[string]
$provKeys = @{}
$cityRows = New-Object System.Collections.Generic.List[hashtable]
$cityKeys = @{}
$distRows = New-Object System.Collections.Generic.List[hashtable]
$distKeys = @{}

foreach ($line in [System.IO.File]::ReadAllLines($src, $utf8)) {
    $line = $line.Trim()
    if ([string]::IsNullOrWhiteSpace($line)) { continue }
    if ($line.StartsWith("#")) { continue }
    $parts = $line -split "`t"
    if ($parts.Count -lt 3) { continue }
    $p = $parts[0].Trim()
    $c = $parts[1].Trim()
    $d = $parts[2].Trim()
    if ([string]::IsNullOrWhiteSpace($p) -or [string]::IsNullOrWhiteSpace($c) -or [string]::IsNullOrWhiteSpace($d)) { continue }

    if (-not $provKeys.ContainsKey($p)) {
        $provKeys[$p] = $true
        $provSet.Add($p)
    }
    $ck = "$p|$c"
    if (-not $cityKeys.ContainsKey($ck)) {
        $cityKeys[$ck] = $true
        $cityRows.Add(@{ P = $p; C = $c })
    }
    $dk = "$p|$c|$d"
    if (-not $distKeys.ContainsKey($dk)) {
        $distKeys[$dk] = $true
        $distRows.Add(@{ P = $p; C = $c; D = $d })
    }
}

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine(@"
-- =============================================================================
-- 多省市区县导入（医承云配 → 省 → 市 → 区县）
-- 已存在同级同名未删除部门则跳过，可重复执行
-- provinces=$($provSet.Count) cities=$($cityRows.Count) districts=$($distRows.Count)
-- =============================================================================

SET @root_id := (
    SELECT d.dept_id FROM sys_dept d
    WHERE d.del_flag = '0' AND d.dept_name = '医承云配'
    ORDER BY d.dept_id LIMIT 1
);
SELECT IF(@root_id IS NULL, '错误：未找到部门「医承云配」', CONCAT('根部门 dept_id=', @root_id)) AS check_root;

DROP TEMPORARY TABLE IF EXISTS _ins_prov;
CREATE TEMPORARY TABLE _ins_prov (seq INT AUTO_INCREMENT PRIMARY KEY, nm VARCHAR(100) NOT NULL);

"@)

foreach ($p in $provSet) {
    [void]$sb.AppendLine("INSERT INTO _ins_prov (nm) VALUES ('$(SqlEsc $p)');")
}

[void]$sb.AppendLine(@"

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT
    @root_id,
    IF(IFNULL(pr.ancestors, '') REGEXP '^([0-9]+,)*[0-9]+$', CONCAT(pr.ancestors, ',', pr.dept_id), CONCAT('0,', pr.dept_id)),
    t.nm,
    IFNULL((SELECT MAX(o.order_num) FROM sys_dept o WHERE o.parent_id = @root_id), 0) + t.seq,
    NULL, NULL, NULL, '0', '0', 'admin', NOW()
FROM _ins_prov t
JOIN sys_dept pr ON pr.dept_id = @root_id
LEFT JOIN sys_dept ex ON ex.parent_id = @root_id AND ex.del_flag = '0' AND ex.dept_name = t.nm
WHERE @root_id IS NOT NULL AND ex.dept_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS _ins_prov;

DROP TEMPORARY TABLE IF EXISTS _ins_city;
CREATE TEMPORARY TABLE _ins_city (seq INT AUTO_INCREMENT PRIMARY KEY, pnm VARCHAR(100) NOT NULL, cnm VARCHAR(100) NOT NULL);

"@)

foreach ($row in $cityRows) {
    [void]$sb.AppendLine("INSERT INTO _ins_city (pnm, cnm) VALUES ('$(SqlEsc $row.P)', '$(SqlEsc $row.C)');")
}

[void]$sb.AppendLine(@"

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT
    p.dept_id,
    IF(IFNULL(p.ancestors, '') REGEXP '^([0-9]+,)*[0-9]+$', CONCAT(p.ancestors, ',', p.dept_id), CONCAT('0,', p.dept_id)),
    t.cnm,
    IFNULL((SELECT MAX(o.order_num) FROM sys_dept o WHERE o.parent_id = p.dept_id), 0) + t.seq,
    NULL, NULL, NULL, '0', '0', 'admin', NOW()
FROM _ins_city t
JOIN sys_dept p ON p.parent_id = @root_id AND p.del_flag = '0' AND p.dept_name = t.pnm
LEFT JOIN sys_dept ex ON ex.parent_id = p.dept_id AND ex.del_flag = '0' AND ex.dept_name = t.cnm
WHERE @root_id IS NOT NULL AND ex.dept_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS _ins_city;

DROP TEMPORARY TABLE IF EXISTS _ins_dist;
CREATE TEMPORARY TABLE _ins_dist (seq INT AUTO_INCREMENT PRIMARY KEY, pnm VARCHAR(100) NOT NULL, cnm VARCHAR(100) NOT NULL, dnm VARCHAR(100) NOT NULL);

"@)

foreach ($row in $distRows) {
    [void]$sb.AppendLine("INSERT INTO _ins_dist (pnm, cnm, dnm) VALUES ('$(SqlEsc $row.P)', '$(SqlEsc $row.C)', '$(SqlEsc $row.D)');")
}

[void]$sb.AppendLine(@"

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT
    c.dept_id,
    IF(IFNULL(c.ancestors, '') REGEXP '^([0-9]+,)*[0-9]+$', CONCAT(c.ancestors, ',', c.dept_id), CONCAT('0,', c.dept_id)),
    t.dnm,
    IFNULL((SELECT MAX(o.order_num) FROM sys_dept o WHERE o.parent_id = c.dept_id), 0) + t.seq,
    NULL, NULL, NULL, '0', '0', 'admin', NOW()
FROM _ins_dist t
JOIN sys_dept p ON p.parent_id = @root_id AND p.del_flag = '0' AND p.dept_name = t.pnm
JOIN sys_dept c ON c.parent_id = p.dept_id AND c.del_flag = '0' AND c.dept_name = t.cnm
LEFT JOIN sys_dept ex ON ex.parent_id = c.dept_id AND ex.del_flag = '0' AND ex.dept_name = t.dnm
WHERE @root_id IS NOT NULL AND ex.dept_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS _ins_dist;

SELECT 'multi_province_regions 执行完成' AS msg;
SELECT COUNT(*) AS province_cnt FROM sys_dept WHERE parent_id = @root_id AND del_flag = '0';
"@)

$dir = Split-Path -Parent $out
if (-not (Test-Path -LiteralPath $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
[System.IO.File]::WriteAllText($out, $sb.ToString(), $utf8)
Write-Host "Wrote provinces=$($provSet.Count) cities=$($cityRows.Count) districts=$($distRows.Count) -> $out"
