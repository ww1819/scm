package com.scm.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.HospitalUser;

/**
 * 医院用户 数据层
 *
 * @author scm
 */
public interface HospitalUserMapper
{
    /**
     * 按用户查询一条医院用户（用于用户维护表单回填）
     */
    HospitalUser selectHospitalUserByUserId(Long userId);

    /**
     * 新增医院用户
     */
    int insertHospitalUser(HospitalUser hospitalUser);

    /**
     * 按用户删除全部医院用户关联
     */
    int deleteHospitalUserByUserId(@Param("userId") Long userId);
}
