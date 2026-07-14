package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmReconciliation;

/**
 * 对账表 数据层
 */
public interface ScmReconciliationMapper
{
    List<ScmReconciliation> selectReconciliationList(ScmReconciliation query);
}
