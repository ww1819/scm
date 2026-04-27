package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ScmHospitalSupplierPermission;

public interface IScmHospitalSupplierPermissionService
{
    List<Long> listForbidSubmitHospitalIds(Long supplierId);

    List<ScmHospitalSupplierPermission> selectList(ScmHospitalSupplierPermission query);

    int saveOrUpdate(ScmHospitalSupplierPermission row, String operBy);

    int removeLogical(Long id, String operBy);

    /** 当前供应商用户向指定医院提交业务数据前校验 */
    void assertSubmitAllowed(Long hospitalId, Long supplierId);

    /** 当前供应商用户关联指定医院前校验 */
    void assertBindAllowed(Long hospitalId, Long supplierId);
}
