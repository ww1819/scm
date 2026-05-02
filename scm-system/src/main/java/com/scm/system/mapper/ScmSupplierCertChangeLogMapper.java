package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmSupplierCertChangeLog;

/**
 * 供应商资质变更记录
 */
public interface ScmSupplierCertChangeLogMapper
{
    int insertChangeLog(ScmSupplierCertChangeLog row);

    List<ScmSupplierCertChangeLog> selectChangeLogList(ScmSupplierCertChangeLog query);
}
