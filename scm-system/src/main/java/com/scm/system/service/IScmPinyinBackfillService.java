package com.scm.system.service;

import java.util.Map;

/**
 * 医院、供应商、角色首拼简码批量回填
 */
public interface IScmPinyinBackfillService
{
    /**
     * 按名称用 {@link com.scm.common.utils.PinyinUtils} 生成并写库。
     *
     * @param overwrite 为 true 时覆盖已有非空首拼
     * @return hospitalUpdated、supplierUpdated、roleUpdated 更新条数
     */
    Map<String, Integer> backfillHospitalAndSupplierPinyin(boolean overwrite);
}
