package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmHospitalMenuAuth;

public interface ScmHospitalMenuAuthMapper
{
    int deleteByHospitalId(@Param("hospitalId") Long hospitalId);

    int batchInsert(@Param("list") List<ScmHospitalMenuAuth> list);

    List<Long> selectMenuIdsByHospitalId(@Param("hospitalId") Long hospitalId);
}
