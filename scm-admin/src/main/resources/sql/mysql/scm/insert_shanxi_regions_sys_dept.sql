-- =============================================================================
-- 山西省下市/区县导入（医承云配 → 山西省 → 市 → 区县）
-- 已存在同级同名未删除部门则跳过，可重复执行
-- =============================================================================

SET @root_id := (
    SELECT d.dept_id FROM sys_dept d
    WHERE d.del_flag = '0' AND d.dept_name = '医承云配'
    ORDER BY d.dept_id LIMIT 1
);

SET @prov_id := (
    SELECT p.dept_id FROM sys_dept p
    WHERE p.del_flag = '0' AND p.dept_name = '山西省'
      AND (@root_id IS NULL OR p.parent_id = @root_id)
    ORDER BY p.dept_id LIMIT 1
);

SELECT
    IF(@root_id IS NULL, '错误：未找到部门「医承云配」', CONCAT('根部门 dept_id=', @root_id)) AS check_root,
    IF(@prov_id IS NULL, '错误：未找到部门「山西省」', CONCAT('山西省 dept_id=', @prov_id)) AS check_prov;

SELECT COUNT(*) AS before_city_cnt FROM sys_dept WHERE parent_id = @prov_id AND del_flag = '0';
SELECT COUNT(*) AS before_dist_cnt
FROM sys_dept d
JOIN sys_dept c ON c.dept_id = d.parent_id AND c.parent_id = @prov_id AND c.del_flag = '0'
WHERE d.del_flag = '0' AND @prov_id IS NOT NULL;

DROP TEMPORARY TABLE IF EXISTS _sx_city;
CREATE TEMPORARY TABLE _sx_city (seq INT AUTO_INCREMENT PRIMARY KEY, cnm VARCHAR(100) NOT NULL);

INSERT INTO _sx_city (cnm) VALUES ('太原市');
INSERT INTO _sx_city (cnm) VALUES ('大同市');
INSERT INTO _sx_city (cnm) VALUES ('阳泉市');
INSERT INTO _sx_city (cnm) VALUES ('长治市');
INSERT INTO _sx_city (cnm) VALUES ('晋城市');
INSERT INTO _sx_city (cnm) VALUES ('朔州市');
INSERT INTO _sx_city (cnm) VALUES ('晋中市');
INSERT INTO _sx_city (cnm) VALUES ('运城市');
INSERT INTO _sx_city (cnm) VALUES ('忻州市');
INSERT INTO _sx_city (cnm) VALUES ('临汾市');
INSERT INTO _sx_city (cnm) VALUES ('吕梁市');

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT
    @prov_id,
    IF(IFNULL(h.ancestors, '') REGEXP '^([0-9]+,)*[0-9]+$', CONCAT(h.ancestors, ',', h.dept_id), CONCAT('0,', h.dept_id)),
    t.cnm,
    IFNULL((SELECT MAX(o.order_num) FROM sys_dept o WHERE o.parent_id = @prov_id), 0) + t.seq,
    NULL, NULL, NULL, '0', '0', 'admin', NOW()
FROM _sx_city t
JOIN sys_dept h ON h.dept_id = @prov_id
LEFT JOIN sys_dept ex ON ex.parent_id = @prov_id AND ex.del_flag = '0' AND ex.dept_name = t.cnm
WHERE @prov_id IS NOT NULL AND ex.dept_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS _sx_city;

DROP TEMPORARY TABLE IF EXISTS _sx_dist;
CREATE TEMPORARY TABLE _sx_dist (seq INT AUTO_INCREMENT PRIMARY KEY, cnm VARCHAR(100) NOT NULL, dnm VARCHAR(100) NOT NULL);

INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '小店区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '迎泽区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '杏花岭区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '尖草坪区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '万柏林区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '晋源区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '清徐县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '阳曲县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '娄烦县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('太原市', '古交市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '新荣区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '平城区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '云冈区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '云州区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '阳高县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '天镇县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '广灵县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '灵丘县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '浑源县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('大同市', '左云县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('阳泉市', '城区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('阳泉市', '矿区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('阳泉市', '郊区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('阳泉市', '平定县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('阳泉市', '盂县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '潞州区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '上党区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '屯留区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '襄垣县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '平顺县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '黎城县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '壶关县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '长子县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '武乡县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '沁县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '沁源县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('长治市', '潞城市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋城市', '城区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋城市', '沁水县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋城市', '阳城县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋城市', '陵川县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋城市', '泽州县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋城市', '高平市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('朔州市', '朔城区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('朔州市', '平鲁区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('朔州市', '山阴县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('朔州市', '应县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('朔州市', '右玉县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('朔州市', '怀仁市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '榆次区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '太谷区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '榆社县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '左权县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '和顺县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '昔阳县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '寿阳县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '祁县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '平遥县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '灵石县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('晋中市', '介休市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '盐湖区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '临猗县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '万荣县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '闻喜县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '稷山县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '新绛县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '绛县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '垣曲县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '夏县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '平陆县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '芮城县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '永济市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('运城市', '河津市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '忻府区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '定襄县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '五台县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '代县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '繁峙县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '宁武县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '静乐县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '神池县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '五寨县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '岢岚县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '河曲县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '保德县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '偏关县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('忻州市', '原平市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '尧都区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '曲沃县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '翼城县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '襄汾县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '洪洞县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '古县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '安泽县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '浮山县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '吉县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '乡宁县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '大宁县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '隰县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '永和县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '蒲县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '汾西县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '侯马市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('临汾市', '霍州市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '离石区');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '文水县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '交城县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '兴县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '临县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '柳林县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '石楼县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '岚县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '方山县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '中阳县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '交口县');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '孝义市');
INSERT INTO _sx_dist (cnm, dnm) VALUES ('吕梁市', '汾阳市');

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT
    c.dept_id,
    IF(IFNULL(c.ancestors, '') REGEXP '^([0-9]+,)*[0-9]+$', CONCAT(c.ancestors, ',', c.dept_id), CONCAT('0,', c.dept_id)),
    t.dnm,
    IFNULL((SELECT MAX(o.order_num) FROM sys_dept o WHERE o.parent_id = c.dept_id), 0) + t.seq,
    NULL, NULL, NULL, '0', '0', 'admin', NOW()
FROM _sx_dist t
JOIN sys_dept c ON c.parent_id = @prov_id AND c.del_flag = '0' AND c.dept_name = t.cnm
LEFT JOIN sys_dept ex ON ex.parent_id = c.dept_id AND ex.del_flag = '0' AND ex.dept_name = t.dnm
WHERE @prov_id IS NOT NULL AND ex.dept_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS _sx_dist;

SELECT COUNT(*) AS after_city_cnt FROM sys_dept WHERE parent_id = @prov_id AND del_flag = '0';
SELECT COUNT(*) AS after_dist_cnt
FROM sys_dept d
JOIN sys_dept c ON c.dept_id = d.parent_id AND c.parent_id = @prov_id AND c.del_flag = '0'
WHERE d.del_flag = '0' AND @prov_id IS NOT NULL;

SELECT 'insert_shanxi_regions_sys_dept 执行完成' AS msg;
