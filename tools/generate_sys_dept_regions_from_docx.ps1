# 从 _docx_extract_regions.txt 生成 insert_regions_from_docx_sys_dept.sql
# 依赖：与 insert_provincial_regions_sys_dept.sql 相同，根部门名为「医承云配」
$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding $false
$src = Join-Path (Join-Path $PSScriptRoot "..") "_docx_extract_regions.txt"
if (-not (Test-Path -LiteralPath $src)) { throw "Source not found: $src" }
$repoRoot = Join-Path $PSScriptRoot ".."
$out = Join-Path $repoRoot "scm-admin\src\main\resources\sql\mysql\scm\insert_regions_from_docx_sys_dept.sql"

function SqlEsc([string]$s) {
    if ($null -eq $s) { return "" }
    return $s.Replace("'", "''")
}

function CleanJunk([string]$s) {
    if ($null -eq $s) { return "" }
    $t = $s.Trim()
    $t = $t -replace '全国行政区划信息查询平台', ''
    $t = $t.Trim()
    return $t
}

function Split-Districts([string]$text) {
    $t = CleanJunk $text
    if ([string]::IsNullOrWhiteSpace($t)) { return @() }
    $parts = New-Object System.Collections.Generic.List[string]
    foreach ($seg in ($t -split '；')) {
        $seg = $seg.Trim()
        if ([string]::IsNullOrWhiteSpace($seg)) { continue }
        foreach ($one in ($seg -split '、')) {
            $one = (CleanJunk $one).Trim()
            if ($one.Length -gt 0) { $parts.Add($one) }
        }
    }
    return $parts.ToArray()
}

$lines = [System.IO.File]::ReadAllLines($src, $utf8)

# 结构: 每条 @{ P=省名; C=市名; D=区县名 }
$districtRows = New-Object System.Collections.Generic.List[hashtable]
$cityRows = New-Object System.Collections.Generic.List[hashtable]  # P,C 去重
$provSet = New-Object System.Collections.Generic.HashSet[string]

function Add-City($p, $c) {
    if ([string]::IsNullOrWhiteSpace($p) -or [string]::IsNullOrWhiteSpace($c)) { return }
    [void]$script:provSet.Add($p)
    $key = "$p|`u{001F}$c"
    $found = $false
    foreach ($x in $script:cityRows) {
        if ($x.K -eq $key) { $found = $true; break }
    }
    if (-not $found) {
        $script:cityRows.Add(@{ K = $key; P = $p; C = $c })
    }
}

function Add-Districts($p, $c, $districtList) {
    if ([string]::IsNullOrWhiteSpace($p) -or [string]::IsNullOrWhiteSpace($c)) { return }
    [void]$script:provSet.Add($p)
    Add-City $p $c
    if ($null -eq $districtList -or $districtList.Count -eq 0) {
        $script:districtRows.Add(@{ P = $p; C = $c; D = "市辖区" })
        return
    }
    foreach ($d in $districtList) {
        if ([string]::IsNullOrWhiteSpace($d)) { continue }
        $script:districtRows.Add(@{ P = $p; C = $c; D = $d })
    }
}

function Parse-CityLineBody($province, $cityName, $body) {
    Add-Districts $province $cityName (Split-Districts $body)
}

$i = 0
$inDirect = $false
$inSpecialAdmin = $false

while ($i -lt $lines.Count) {
    $line = $lines[$i].Trim()
    $i++

    if ($line.Length -eq 0) { continue }
    if ($line -match '^全国行政区划') { continue }

    # 直辖市段
    if ($line -match '^一[、.].*直辖') {
        $inDirect = $true
        $inSpecialAdmin = $false
        continue
    }

    # 特别行政区段标题
    if ($line -match '^三十[、.]特别行政区') {
        $inDirect = $false
        $inSpecialAdmin = $true
        continue
    }

    if ($inDirect) {
        if ($line -match '^[二三四五六七八九十百千]+[、.]') {
            $inDirect = $false
            $i--
            continue
        }
        if ($line -match '^(\d+)\.\s*(.+)$') {
            $prov = $matches[2].Trim()
            while ($i -lt $lines.Count) {
                $L = $lines[$i].Trim()
                if ($L.Length -eq 0) { $i++; continue }
                if ($L -match '^(\d+)\.\s' -or $L -match '^[二三四五六七八九十百千]+[、.]') { break }
                if ($L -match '^(市辖区|县|自治县)[：:](.+)$') {
                    $cityPart = $matches[1]
                    Parse-CityLineBody $prov $cityPart $matches[2]
                }
                $i++
            }
            continue
        }
    }

    if ($inSpecialAdmin) {
        if ($line -match '^[二三四五六七八九十百千]+[、.]') { $inSpecialAdmin = $false; $i--; continue }
        if ($line -match '香港特别行政区|澳门特别行政区') {
            Add-Districts $line "市辖区" @($line)
        }
        continue
    }

    # 省 / 自治区标题：二、河北省（冀，石家庄）
    if ($line -match '^[一二三四五六七八九十百千]+[、.](.+?)（') {
        $prov = $matches[1].Trim()
        if ($prov -eq '直辖市' -or $prov -match '^特别行政区') { continue }

        while ($i -lt $lines.Count) {
            $L = $lines[$i].Trim()
            if ($L.Length -eq 0) { $i++; continue }

            # 文档中「三十、特别行政区」无括号，否则会误当作新疆下的正文被吞掉
            if ($L -match '^三十[、.]特别行政区\s*$') { break }

            if ($L -match '^[一二三四五六七八九十百千]+[、.].+（') {
                break
            }

            # 台湾省略写一行
            if ($prov -eq '台湾省' -and $L.StartsWith('（略')) {
                if ($L -match '：(.+)\）') {
                    $inner = $matches[1]
                    foreach ($seg in ($inner -split '；')) {
                        foreach ($nm in ($seg -split '、')) {
                            $nm = (CleanJunk $nm).Trim()
                            if ($nm.Length -gt 0) {
                                Add-Districts $prov $nm @("市辖区")
                            }
                        }
                    }
                }
                $i++
                continue
            }

            # 市：... ：区县
            if ($L -match '^(.+?[省市州盟]|.+?地区)[：:](.+)$') {
                $cityName = $matches[1].Trim()
                Parse-CityLineBody $prov $cityName $matches[2]
                $i++
                continue
            }

            # 济源市（省直辖县级市） / 仙桃市（省直辖） / 神农架林区（省直辖）
            if ($L -match '^(.+?)（省直辖') {
                $cn = $matches[1].Trim()
                Add-Districts $prov $cn @($cn)
                $i++
                continue
            }

            # 东莞市（无市辖区...） / 嘉峪关市（无市辖区） / 中山市
            if ($L -match '^(.+?)（无市辖区') {
                $cn = $matches[1].Trim()
                Add-Districts $prov $cn @("市辖区")
                $i++
                continue
            }

            # 儋州市：那大镇... — 仍解析冒号后；无结构化区县则整段作一个「市辖区」子项
            if ($L -match '^(.+?[市州])[：:](.+)$') {
                $cityName = $matches[1].Trim()
                $rest = $matches[2].Trim()
                $ds = Split-Districts $rest
                if ($ds.Count -eq 0) { Add-Districts $prov $cityName @("市辖区") }
                else { Add-Districts $prov $cityName $ds }
                $i++
                continue
            }

            $i++
        }
        continue
    }
}

# ---------- 生成 SQL ----------
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine(@"
-- =============================================================================
-- 根据用户整理的《全国行政区划》文档生成的省 / 市 / 区县三级部门数据
-- 挂载在部门「医承云配」下；已存在同名且未删除的同级部门则跳过
-- 执行前请确认 sys_dept.dept_name 长度足够（超长名称需调整表字段）
-- =============================================================================

SET @root_id := (
    SELECT d.dept_id FROM sys_dept d
    WHERE d.del_flag = '0' AND d.dept_name = '医承云配'
    ORDER BY d.dept_id LIMIT 1
);
SELECT IF(@root_id IS NULL, '错误：未找到部门「医承云配」', CONCAT('根部门 dept_id = ', @root_id)) AS check_root;

"@)

[void]$sb.AppendLine(@"
DROP TEMPORARY TABLE IF EXISTS _ins_prov;
CREATE TEMPORARY TABLE _ins_prov (seq INT AUTO_INCREMENT PRIMARY KEY, nm VARCHAR(100) NOT NULL);

"@)

$provList = @($provSet | Sort-Object)
$pi = 0
foreach ($p in $provList) {
    $pi++
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

"@)

[void]$sb.AppendLine(@"
DROP TEMPORARY TABLE IF EXISTS _ins_city;
CREATE TEMPORARY TABLE _ins_city (seq INT AUTO_INCREMENT PRIMARY KEY, pnm VARCHAR(100) NOT NULL, cnm VARCHAR(100) NOT NULL);

"@)

$ci = 0
foreach ($row in $cityRows) {
    $ci++
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

"@)

[void]$sb.AppendLine(@"
DROP TEMPORARY TABLE IF EXISTS _ins_dist;
CREATE TEMPORARY TABLE _ins_dist (seq INT AUTO_INCREMENT PRIMARY KEY, pnm VARCHAR(100) NOT NULL, cnm VARCHAR(100) NOT NULL, dnm VARCHAR(100) NOT NULL);

"@)

foreach ($row in $districtRows) {
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

SELECT 'insert_regions_from_docx_sys_dept 执行完成' AS msg;
"@)

[System.IO.File]::WriteAllText($out, $sb.ToString(), $utf8)
Write-Host "Wrote $($districtRows.Count) district rows, $($cityRows.Count) cities, $($provSet.Count) provinces -> $out"
