package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmHospitalSupplierModifyApply;

public interface ScmHospitalSupplierModifyApplyMapper
{
    int insert(ScmHospitalSupplierModifyApply row);

    int update(ScmHospitalSupplierModifyApply row);

    ScmHospitalSupplierModifyApply selectById(@Param("modifyApplyId") String modifyApplyId);

    int countPendingByRelationId(@Param("relationId") Long relationId);

    List<ScmHospitalSupplierModifyApply> selectList(ScmHospitalSupplierModifyApply query);
}
