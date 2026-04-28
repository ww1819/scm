package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmHospitalSupplierApply;

public interface ScmHospitalSupplierApplyMapper
{
    int insertApply(ScmHospitalSupplierApply row);

    int updateApply(ScmHospitalSupplierApply row);

    ScmHospitalSupplierApply selectByApplyId(String applyId);

    ScmHospitalSupplierApply selectLatestByHospitalAndSupplier(ScmHospitalSupplierApply q);

    List<ScmHospitalSupplierApply> selectApplyList(ScmHospitalSupplierApply q);
}
