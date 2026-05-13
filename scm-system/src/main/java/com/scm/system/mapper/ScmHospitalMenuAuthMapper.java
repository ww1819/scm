package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.MenuGrantHospitalAuthPair;
import com.scm.system.domain.ScmHospitalMenuAuth;

public interface ScmHospitalMenuAuthMapper
{
    int deleteByHospitalId(@Param("hospitalId") Long hospitalId);

    int batchInsert(@Param("list") List<ScmHospitalMenuAuth> list);

    /** 忽略 uk_hospital_menu 重复 */
    int batchInsertIgnore(@Param("list") List<ScmHospitalMenuAuth> list);

    List<Long> selectMenuIdsByHospitalId(@Param("hospitalId") Long hospitalId);

    /** 活跃医院下已有白名单 (hospital_id, menu_id)，一次查出供批量赋权比对 */
    List<MenuGrantHospitalAuthPair> selectAuthPairsForActiveHospitals();
}
