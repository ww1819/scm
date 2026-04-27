-- =============================================================================
-- 省级行政区：在部门「医承云配」下插入 34 个一级子部门（不要求 parent_id=0）
-- 含 23 省、5 自治区、4 直辖市、2 特别行政区；已存在同名且未删除的同级部门则跳过
-- 子节点 ancestors：若父级 ancestors 为合法「数字,数字,...」则追加 ,父dept_id；否则用 0,父dept_id
-- =============================================================================

SET @root_id := (
    SELECT d.dept_id
    FROM sys_dept d
    WHERE d.del_flag = '0'
      AND d.dept_name = '医承云配'
    ORDER BY d.dept_id
    LIMIT 1
);

SELECT IF(@root_id IS NULL, '错误：未找到部门「医承云配」', CONCAT('根部门 dept_id = ', @root_id)) AS check_root;

SET @base_order := IFNULL((SELECT MAX(t.order_num) FROM sys_dept t WHERE t.parent_id = @root_id), 0);

SET @child_ancestors := (
    SELECT IF(
        IFNULL(p.ancestors, '') REGEXP '^([0-9]+,)*[0-9]+$',
        CONCAT(p.ancestors, ',', p.dept_id),
        CONCAT('0,', p.dept_id)
    )
    FROM sys_dept p
    WHERE p.dept_id = @root_id
    LIMIT 1
);

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT
    @root_id,
    @child_ancestors,
    r.nm,
    @base_order + r.seq,
    NULL,
    NULL,
    NULL,
    '0',
    '0',
    'admin',
    NOW()
FROM (
    SELECT 1 AS seq, '河北省' AS nm UNION ALL SELECT 2, '山西省' UNION ALL SELECT 3, '辽宁省' UNION ALL SELECT 4, '吉林省' UNION ALL SELECT 5, '黑龙江省'
    UNION ALL SELECT 6, '江苏省' UNION ALL SELECT 7, '浙江省' UNION ALL SELECT 8, '安徽省' UNION ALL SELECT 9, '福建省' UNION ALL SELECT 10, '江西省'
    UNION ALL SELECT 11, '山东省' UNION ALL SELECT 12, '河南省' UNION ALL SELECT 13, '湖北省' UNION ALL SELECT 14, '湖南省' UNION ALL SELECT 15, '广东省'
    UNION ALL SELECT 16, '海南省' UNION ALL SELECT 17, '四川省' UNION ALL SELECT 18, '贵州省' UNION ALL SELECT 19, '云南省' UNION ALL SELECT 20, '陕西省'
    UNION ALL SELECT 21, '甘肃省' UNION ALL SELECT 22, '青海省' UNION ALL SELECT 23, '台湾省'
    UNION ALL SELECT 24, '内蒙古自治区' UNION ALL SELECT 25, '广西壮族自治区' UNION ALL SELECT 26, '西藏自治区' UNION ALL SELECT 27, '宁夏回族自治区' UNION ALL SELECT 28, '新疆维吾尔自治区'
    UNION ALL SELECT 29, '北京市' UNION ALL SELECT 30, '天津市' UNION ALL SELECT 31, '上海市' UNION ALL SELECT 32, '重庆市'
    UNION ALL SELECT 33, '香港特别行政区' UNION ALL SELECT 34, '澳门特别行政区'
) r
WHERE @root_id IS NOT NULL
  AND @child_ancestors IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_dept x
      WHERE x.parent_id = @root_id
        AND x.del_flag = '0'
        AND x.dept_name = r.nm
  );
