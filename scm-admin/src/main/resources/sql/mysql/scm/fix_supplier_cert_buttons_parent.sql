-- 将供应商资质登记页按钮重新挂回「供应商资质登记」菜单下
-- 证件查询/新增/修改/删除/审核/导出

SET @supplier_cert_parent_id := (
  SELECT m.menu_id
  FROM sys_menu m
  WHERE m.del_flag = '0'
    AND m.menu_type = 'C'
    AND (m.perms = 'certificate:supplier:view' OR m.menu_name = '供应商资质登记')
  ORDER BY m.menu_id
  LIMIT 1
);

UPDATE sys_menu
SET parent_id = @supplier_cert_parent_id,
    order_num = CASE perms
      WHEN 'certificate:supplier:list' THEN 1
      WHEN 'certificate:supplier:add' THEN 2
      WHEN 'certificate:supplier:edit' THEN 3
      WHEN 'certificate:supplier:remove' THEN 4
      WHEN 'certificate:supplier:audit' THEN 5
      WHEN 'certificate:supplier:export' THEN 6
      ELSE order_num
    END,
    update_by = 'fix',
    update_time = NOW()
WHERE del_flag = '0'
  AND menu_type = 'F'
  AND @supplier_cert_parent_id IS NOT NULL
  AND perms IN (
    'certificate:supplier:list',
    'certificate:supplier:add',
    'certificate:supplier:edit',
    'certificate:supplier:remove',
    'certificate:supplier:audit',
    'certificate:supplier:export'
  );

-- 校验
SELECT m.menu_id, m.menu_name, m.parent_id, p.menu_name AS parent_name, m.perms, m.order_num
FROM sys_menu m
LEFT JOIN sys_menu p ON p.menu_id = m.parent_id
WHERE m.del_flag = '0'
  AND m.menu_type = 'F'
  AND m.perms IN (
    'certificate:supplier:list',
    'certificate:supplier:add',
    'certificate:supplier:edit',
    'certificate:supplier:remove',
    'certificate:supplier:audit',
    'certificate:supplier:export'
  )
ORDER BY m.parent_id, m.order_num, m.menu_id;
