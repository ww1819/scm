package com.scm.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmUserPrintSetting;

/**
 * 用户打印版式设置
 */
public interface ScmUserPrintSettingMapper
{
    ScmUserPrintSetting selectByUserIdAndPrintType(@Param("userId") Long userId, @Param("printType") String printType);

    int insertScmUserPrintSetting(ScmUserPrintSetting row);

    int updateScmUserPrintSetting(ScmUserPrintSetting row);
}
