package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.ScmHospitalSupplierScopePair;

/**
 * 医院-供应商菜单联合数据范围
 */
public interface ScmHospitalSupplierScopeMapper
{
    /**
     * 医院端：需医院授予供应商（hospital_grant_supplier_flag=1）时，本院在该菜单下的 (医院,供应商) 对
     */
    List<ScmHospitalSupplierScopePair> selectPairsHospitalGrantMenu(@Param("hospitalId") Long hospitalId, @Param("menuId") Long menuId);

    /**
     * 医院端：非「按院授予」时，本院已绑定且供应商具备全院级菜单白名单的 (医院,供应商) 对
     */
    List<ScmHospitalSupplierScopePair> selectPairsHospitalGlobalSupplierMenu(@Param("hospitalId") Long hospitalId, @Param("menuId") Long menuId);

    /**
     * 供应商端：按院授予时，本供应商在该菜单下的 (医院,供应商) 对
     */
    List<ScmHospitalSupplierScopePair> selectPairsSupplierGrantMenu(@Param("supplierId") Long supplierId, @Param("menuId") Long menuId);

    /**
     * 供应商端：全院级菜单白名单时，本供应商已绑定医院的 (医院,供应商) 对
     */
    List<ScmHospitalSupplierScopePair> selectPairsSupplierGlobalMenu(@Param("supplierId") Long supplierId, @Param("menuId") Long menuId);
}
