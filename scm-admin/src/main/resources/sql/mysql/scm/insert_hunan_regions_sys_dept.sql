-- =============================================================================
-- 湖南省下市/区县导入（挂载部门树：医承云配 → 湖南省 → 市 → 区县）
-- 已存在同级同名未删除部门则跳过，可重复执行
-- =============================================================================

SET @root_id := (
    SELECT d.dept_id FROM sys_dept d
    WHERE d.del_flag = '0' AND d.dept_name = '医承云配'
    ORDER BY d.dept_id LIMIT 1
);

SET @hunan_id := (
    SELECT p.dept_id FROM sys_dept p
    WHERE p.del_flag = '0' AND p.dept_name = '湖南省'
      AND (@root_id IS NULL OR p.parent_id = @root_id)
    ORDER BY p.dept_id LIMIT 1
);

SELECT
    IF(@root_id IS NULL, '错误：未找到部门「医承云配」', CONCAT('根部门 dept_id=', @root_id)) AS check_root,
    IF(@hunan_id IS NULL, '错误：未找到部门「湖南省」', CONCAT('湖南省 dept_id=', @hunan_id)) AS check_hunan;

-- 导入前：已有市/区县数量
SELECT COUNT(*) AS before_city_cnt FROM sys_dept WHERE parent_id = @hunan_id AND del_flag = '0';
SELECT COUNT(*) AS before_dist_cnt
FROM sys_dept d
JOIN sys_dept c ON c.dept_id = d.parent_id AND c.parent_id = @hunan_id AND c.del_flag = '0'
WHERE d.del_flag = '0' AND @hunan_id IS NOT NULL;

DROP TEMPORARY TABLE IF EXISTS _hn_city;
CREATE TEMPORARY TABLE _hn_city (seq INT AUTO_INCREMENT PRIMARY KEY, cnm VARCHAR(100) NOT NULL);

INSERT INTO _hn_city (cnm) VALUES ('长沙市');
INSERT INTO _hn_city (cnm) VALUES ('株洲市');
INSERT INTO _hn_city (cnm) VALUES ('湘潭市');
INSERT INTO _hn_city (cnm) VALUES ('衡阳市');
INSERT INTO _hn_city (cnm) VALUES ('邵阳市');
INSERT INTO _hn_city (cnm) VALUES ('岳阳市');
INSERT INTO _hn_city (cnm) VALUES ('常德市');
INSERT INTO _hn_city (cnm) VALUES ('张家界市');
INSERT INTO _hn_city (cnm) VALUES ('益阳市');
INSERT INTO _hn_city (cnm) VALUES ('郴州市');
INSERT INTO _hn_city (cnm) VALUES ('永州市');
INSERT INTO _hn_city (cnm) VALUES ('怀化市');
INSERT INTO _hn_city (cnm) VALUES ('娄底市');
INSERT INTO _hn_city (cnm) VALUES ('湘西土家族苗族自治州');

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT
    @hunan_id,
    IF(IFNULL(h.ancestors, '') REGEXP '^([0-9]+,)*[0-9]+$', CONCAT(h.ancestors, ',', h.dept_id), CONCAT('0,', h.dept_id)),
    t.cnm,
    IFNULL((SELECT MAX(o.order_num) FROM sys_dept o WHERE o.parent_id = @hunan_id), 0) + t.seq,
    NULL, NULL, NULL, '0', '0', 'admin', NOW()
FROM _hn_city t
JOIN sys_dept h ON h.dept_id = @hunan_id
LEFT JOIN sys_dept ex ON ex.parent_id = @hunan_id AND ex.del_flag = '0' AND ex.dept_name = t.cnm
WHERE @hunan_id IS NOT NULL AND ex.dept_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS _hn_city;

DROP TEMPORARY TABLE IF EXISTS _hn_dist;
CREATE TEMPORARY TABLE _hn_dist (seq INT AUTO_INCREMENT PRIMARY KEY, cnm VARCHAR(100) NOT NULL, dnm VARCHAR(100) NOT NULL);

INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '芙蓉区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '天心区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '岳麓区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '开福区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '雨花区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '望城区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '长沙县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '宁乡市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('长沙市', '浏阳市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '荷塘区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '芦淞区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '石峰区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '天元区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '渌口区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '攸县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '茶陵县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '炎陵县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('株洲市', '醴陵市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘潭市', '雨湖区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘潭市', '岳塘区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘潭市', '湘潭县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘潭市', '湘乡市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘潭市', '韶山市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '珠晖区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '雁峰区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '石鼓区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '蒸湘区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '南岳区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '衡阳县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '衡南县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '衡山县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '衡东县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '祁东县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '耒阳市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('衡阳市', '常宁市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '双清区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '大祥区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '北塔区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '邵东县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '新邵县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '邵阳县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '隆回县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '洞口县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '绥宁县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '新宁县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '城步苗族自治县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '武冈市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('邵阳市', '邵东市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '岳阳楼区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '云溪区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '君山区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '岳阳县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '华容县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '湘阴县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '平江县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '汨罗市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('岳阳市', '临湘市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '武陵区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '鼎城区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '安乡县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '汉寿县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '澧县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '临澧县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '桃源县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '石门县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('常德市', '津市市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('张家界市', '永定区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('张家界市', '武陵源区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('张家界市', '慈利县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('张家界市', '桑植县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('益阳市', '资阳区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('益阳市', '赫山区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('益阳市', '南县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('益阳市', '桃江县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('益阳市', '安化县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('益阳市', '沅江市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '北湖区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '苏仙区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '桂阳县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '宜章县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '永兴县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '嘉禾县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '临武县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '汝城县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '桂东县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '安仁县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('郴州市', '资兴市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '零陵区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '冷水滩区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '祁阳县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '东安县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '双牌县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '道县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '江永县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '宁远县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '蓝山县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '新田县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '江华瑶族自治县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('永州市', '祁阳市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '鹤城区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '中方县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '沅陵县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '辰溪县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '溆浦县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '会同县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '麻阳苗族自治县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '新晃侗族自治县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '芷江侗族自治县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '靖州苗族侗族自治县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '通道侗族自治县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('怀化市', '洪江市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('娄底市', '娄星区');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('娄底市', '双峰县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('娄底市', '新化县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('娄底市', '冷水江市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('娄底市', '涟源市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘西土家族苗族自治州', '吉首市');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘西土家族苗族自治州', '泸溪县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘西土家族苗族自治州', '凤凰县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘西土家族苗族自治州', '花垣县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘西土家族苗族自治州', '保靖县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘西土家族苗族自治州', '古丈县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘西土家族苗族自治州', '永顺县');
INSERT INTO _hn_dist (cnm, dnm) VALUES ('湘西土家族苗族自治州', '龙山县');

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT
    c.dept_id,
    IF(IFNULL(c.ancestors, '') REGEXP '^([0-9]+,)*[0-9]+$', CONCAT(c.ancestors, ',', c.dept_id), CONCAT('0,', c.dept_id)),
    t.dnm,
    IFNULL((SELECT MAX(o.order_num) FROM sys_dept o WHERE o.parent_id = c.dept_id), 0) + t.seq,
    NULL, NULL, NULL, '0', '0', 'admin', NOW()
FROM _hn_dist t
JOIN sys_dept c ON c.parent_id = @hunan_id AND c.del_flag = '0' AND c.dept_name = t.cnm
LEFT JOIN sys_dept ex ON ex.parent_id = c.dept_id AND ex.del_flag = '0' AND ex.dept_name = t.dnm
WHERE @hunan_id IS NOT NULL AND ex.dept_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS _hn_dist;

-- 导入后统计 + 缺失清单（相对本次目标）
SELECT COUNT(*) AS after_city_cnt FROM sys_dept WHERE parent_id = @hunan_id AND del_flag = '0';
SELECT COUNT(*) AS after_dist_cnt
FROM sys_dept d
JOIN sys_dept c ON c.dept_id = d.parent_id AND c.parent_id = @hunan_id AND c.del_flag = '0'
WHERE d.del_flag = '0' AND @hunan_id IS NOT NULL;

SELECT 'insert_hunan_regions_sys_dept 执行完成' AS msg;
