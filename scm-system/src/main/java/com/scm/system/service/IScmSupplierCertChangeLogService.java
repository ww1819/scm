package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.ScmSupplierCertChangeLog;

/**
 * 供应商资质变更记录（医院/供应商按数据域查询）
 */
public interface IScmSupplierCertChangeLogService
{
    List<ScmSupplierCertChangeLog> selectChangeLogList(ScmSupplierCertChangeLog query);
}
