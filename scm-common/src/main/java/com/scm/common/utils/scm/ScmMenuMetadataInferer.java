package com.scm.common.utils.scm;

import com.scm.common.constant.ScmAuthConstants;
import com.scm.common.constant.ScmMenuConstants;
import com.scm.common.core.domain.entity.SysMenu;
import com.scm.common.utils.StringUtils;

/**
 * 根据权限串、路由、名称推断 SCM 菜单的 auth_type、hospital_grant_supplier_flag、default_open_scope、menu_biz_category。
 */
public final class ScmMenuMetadataInferer
{
    private ScmMenuMetadataInferer()
    {
    }

    /**
     * @param onlyIfBlank 为 true 时仅填充当前为空的字段（用于保存菜单）
     */
    public static void applyInference(SysMenu menu, boolean onlyIfBlank)
    {
        if (menu == null)
        {
            return;
        }
        String perms = StringUtils.trimToEmpty(menu.getPerms());
        String url = StringUtils.trimToEmpty(menu.getUrl());
        String name = StringUtils.trimToEmpty(menu.getMenuName());

        String authType = inferAuthType(perms, url, name);
        String grant = inferGrantFlag(perms, authType);
        String openScope = inferOpenScope(perms, authType, grant);
        String biz = inferBizCategory(perms, url, name, authType);

        if (!onlyIfBlank || StringUtils.isEmpty(menu.getAuthType()))
        {
            menu.setAuthType(authType);
        }
        if (!onlyIfBlank || StringUtils.isEmpty(menu.getHospitalGrantSupplierFlag()))
        {
            menu.setHospitalGrantSupplierFlag(grant);
        }
        if (!onlyIfBlank || StringUtils.isEmpty(menu.getDefaultOpenScope()))
        {
            menu.setDefaultOpenScope(openScope);
        }
        if (!onlyIfBlank || StringUtils.isEmpty(menu.getMenuBizCategory()))
        {
            menu.setMenuBizCategory(biz);
        }
        ensureTenantOpenFlags(menu, onlyIfBlank);
    }

    /**
     * 根据 auth_type、default_open_scope、hospital_grant_supplier_flag 推导四列（与建档/刷新白名单逻辑一致）。
     * onlyIfBlank 为 true 时仅填充仍为空的开关列，不覆盖用户已选的 0/1。
     */
    private static void ensureTenantOpenFlags(SysMenu menu, boolean onlyIfBlank)
    {
        if (menu == null)
        {
            return;
        }
        String at = StringUtils.trimToEmpty(menu.getAuthType());
        if (StringUtils.isEmpty(at))
        {
            at = ScmAuthConstants.AUTH_PLATFORM;
        }
        String scope = StringUtils.trimToEmpty(menu.getDefaultOpenScope());
        if (StringUtils.isEmpty(scope))
        {
            scope = ScmMenuConstants.OPEN_SCOPE_NONE;
        }
        String grant = StringUtils.trimToEmpty(menu.getHospitalGrantSupplierFlag());

        String hOpen = "0";
        String sOpen = "0";
        if (ScmAuthConstants.AUTH_PLATFORM.equalsIgnoreCase(at))
        {
            hOpen = "0";
            sOpen = "0";
        }
        else if (ScmAuthConstants.AUTH_HOSPITAL.equalsIgnoreCase(at))
        {
            hOpen = ScmMenuConstants.OPEN_SCOPE_NONE.equalsIgnoreCase(scope) ? "0" : "1";
        }
        else if (ScmAuthConstants.AUTH_SUPPLIER.equalsIgnoreCase(at))
        {
            sOpen = ScmMenuConstants.OPEN_SCOPE_NONE.equalsIgnoreCase(scope) ? "0" : "1";
        }
        else if (ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equalsIgnoreCase(at))
        {
            if (ScmMenuConstants.OPEN_SCOPE_NONE.equalsIgnoreCase(scope))
            {
                hOpen = "0";
                sOpen = "0";
            }
            else if (ScmMenuConstants.OPEN_SCOPE_ALL_HOSPITAL.equalsIgnoreCase(scope))
            {
                hOpen = "1";
                sOpen = "0";
            }
            else if (ScmMenuConstants.OPEN_SCOPE_ALL_SUPPLIER.equalsIgnoreCase(scope))
            {
                hOpen = "0";
                sOpen = "1";
            }
            else
            {
                hOpen = "1";
                sOpen = "1";
                if ("1".equals(grant))
                {
                    sOpen = "0";
                }
            }
        }

        if (!onlyIfBlank || isUnset(menu.getDefaultOpenHospital()))
        {
            menu.setDefaultOpenHospital(hOpen);
        }
        if (!onlyIfBlank || isUnset(menu.getDefaultOpenSupplier()))
        {
            menu.setDefaultOpenSupplier(sOpen);
        }
        if (!onlyIfBlank || isUnset(menu.getHospitalAdminOnly()))
        {
            menu.setHospitalAdminOnly("0");
        }
        if (!onlyIfBlank || isUnset(menu.getSupplierAdminOnly()))
        {
            menu.setSupplierAdminOnly("0");
        }
    }

    private static boolean isUnset(String v)
    {
        return v == null || StringUtils.trimToEmpty(v).isEmpty();
    }

    private static String inferAuthType(String perms, String url, String name)
    {
        if (StringUtils.isNotEmpty(perms))
        {
            if (perms.startsWith("system:") || perms.startsWith("scmAuth:"))
            {
                return ScmAuthConstants.AUTH_PLATFORM;
            }
            if (perms.startsWith("tenant:"))
            {
                return ScmAuthConstants.AUTH_PLATFORM;
            }
            if (perms.startsWith("interface:"))
            {
                return ScmAuthConstants.AUTH_PLATFORM;
            }
            if (perms.startsWith("hospital:"))
            {
                if (StringUtils.isNotEmpty(url) && "/hospital/hospital".equals(url))
                {
                    return ScmAuthConstants.AUTH_PLATFORM;
                }
                return ScmAuthConstants.AUTH_HOSPITAL;
            }
            if (perms.startsWith("supplier:"))
            {
                if (StringUtils.isNotEmpty(url) && "/supplier/supplier".equals(url))
                {
                    return ScmAuthConstants.AUTH_PLATFORM;
                }
                return ScmAuthConstants.AUTH_SUPPLIER;
            }
            if (perms.startsWith("delivery:"))
            {
                if (StringUtils.isNotEmpty(url) && url.contains("/query"))
                {
                    return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
                }
                return ScmAuthConstants.AUTH_SUPPLIER;
            }
            if (perms.startsWith("order:") || perms.startsWith("settlement:"))
            {
                return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
            }
            if (perms.startsWith("datacenter:"))
            {
                return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
            }
            if (perms.startsWith("certificate:"))
            {
                if (perms.contains(":audit"))
                {
                    return ScmAuthConstants.AUTH_HOSPITAL;
                }
                return ScmAuthConstants.AUTH_SUPPLIER;
            }
            if (perms.startsWith("material:"))
            {
                return ScmAuthConstants.AUTH_PLATFORM;
            }
        }
        if (url.startsWith("/system/") || url.startsWith("/monitor/") || url.startsWith("/tool/"))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (url.startsWith("/scm/auth"))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (url.startsWith("/tenant/"))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (url.startsWith("/interface/"))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if ("/hospital/hospital".equals(url))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (url.startsWith("/hospital/"))
        {
            return ScmAuthConstants.AUTH_HOSPITAL;
        }
        if ("/supplier/supplier".equals(url))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (url.startsWith("/supplier/"))
        {
            return ScmAuthConstants.AUTH_SUPPLIER;
        }
        if (url.startsWith("/delivery/delivery/query"))
        {
            return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
        }
        if (url.startsWith("/delivery/"))
        {
            return ScmAuthConstants.AUTH_SUPPLIER;
        }
        if (url.startsWith("/order/") || url.startsWith("/settlement/"))
        {
            return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
        }
        if (url.startsWith("/datacenter/"))
        {
            return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
        }
        if (url.startsWith("/certificate/"))
        {
            if (url.contains("/audit"))
            {
                return ScmAuthConstants.AUTH_HOSPITAL;
            }
            return ScmAuthConstants.AUTH_SUPPLIER;
        }
        if (url.startsWith("/material/"))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (name.contains("医院信息维护") || "医院维护".equals(name))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (name.contains("医院") && (name.contains("管理") || name.contains("维护")))
        {
            return ScmAuthConstants.AUTH_HOSPITAL;
        }
        if (name.contains("供应商维护") && !name.contains("企业"))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (name.contains("供应商") && (name.contains("管理") || name.contains("维护")))
        {
            return ScmAuthConstants.AUTH_SUPPLIER;
        }
        if (name.contains("资质证件管理"))
        {
            return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
        }
        if (name.contains("审核") && (name.contains("资质") || name.contains("证件") || name.contains("证照")))
        {
            return ScmAuthConstants.AUTH_HOSPITAL;
        }
        if (name.contains("配送信息查询"))
        {
            return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
        }
        if (name.contains("配送单据申请"))
        {
            return ScmAuthConstants.AUTH_SUPPLIER;
        }
        if (name.contains("资质") || name.contains("证件管理"))
        {
            return ScmAuthConstants.AUTH_SUPPLIER;
        }
        if (name.contains("基础数据") || name.contains("耗材分类") || name.contains("物资"))
        {
            return ScmAuthConstants.AUTH_PLATFORM;
        }
        if (name.contains("数据中心") || (name.contains("采购量") || name.contains("分析报表")))
        {
            return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
        }
        if (name.contains("订单") || name.contains("配送") || name.contains("结算"))
        {
            return ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER;
        }
        return ScmAuthConstants.AUTH_PLATFORM;
    }

    private static String inferGrantFlag(String perms, String authType)
    {
        if (!ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equals(authType))
        {
            return "0";
        }
        if (StringUtils.isNotEmpty(perms)
            && (perms.startsWith("order:") || perms.startsWith("settlement:")))
        {
            return "1";
        }
        if (StringUtils.isNotEmpty(perms) && perms.startsWith("delivery:"))
        {
            return "0";
        }
        if (StringUtils.isNotEmpty(perms) && perms.startsWith("certificate:") && perms.contains(":audit"))
        {
            return "0";
        }
        return "0";
    }

    private static String inferOpenScope(String perms, String authType, String grantFlag)
    {
        if (ScmAuthConstants.AUTH_PLATFORM.equals(authType))
        {
            return ScmMenuConstants.OPEN_SCOPE_NONE;
        }
        if (ScmAuthConstants.AUTH_HOSPITAL.equals(authType))
        {
            return ScmMenuConstants.OPEN_SCOPE_ALL_HOSPITAL;
        }
        if (ScmAuthConstants.AUTH_SUPPLIER.equals(authType))
        {
            return ScmMenuConstants.OPEN_SCOPE_ALL_SUPPLIER;
        }
        if (ScmAuthConstants.AUTH_HOSPITAL_SUPPLIER.equals(authType))
        {
            if ("1".equals(grantFlag))
            {
                return ScmMenuConstants.OPEN_SCOPE_ALL_HOSPITAL;
            }
            return ScmMenuConstants.OPEN_SCOPE_ALL;
        }
        return ScmMenuConstants.OPEN_SCOPE_NONE;
    }

    private static String inferBizCategory(String perms, String url, String name, String authType)
    {
        if (StringUtils.isNotEmpty(perms))
        {
            if (perms.startsWith("scmAuth:"))
            {
                return ScmMenuConstants.BIZ_SCM_AUTH;
            }
            if (perms.startsWith("system:"))
            {
                return ScmMenuConstants.BIZ_PLATFORM_OPS;
            }
            if (perms.startsWith("tenant:"))
            {
                return ScmMenuConstants.BIZ_TENANT;
            }
            if (perms.startsWith("interface:"))
            {
                return ScmMenuConstants.BIZ_INTEGRATION;
            }
            if (perms.startsWith("supplier:"))
            {
                return ScmMenuConstants.BIZ_SUPPLIER_MASTER;
            }
            if (perms.startsWith("hospital:"))
            {
                return ScmMenuConstants.BIZ_HOSPITAL_MASTER;
            }
            if (perms.startsWith("material:"))
            {
                return ScmMenuConstants.BIZ_MASTER_DATA;
            }
            if (perms.startsWith("certificate:"))
            {
                return ScmMenuConstants.BIZ_CERTIFICATE;
            }
            if (perms.startsWith("order:") || perms.startsWith("delivery:"))
            {
                return ScmMenuConstants.BIZ_SUPPLY_CHAIN;
            }
            if (perms.startsWith("settlement:"))
            {
                return ScmMenuConstants.BIZ_SETTLEMENT;
            }
            if (perms.startsWith("datacenter:"))
            {
                return ScmMenuConstants.BIZ_DATACENTER;
            }
        }
        if (url.startsWith("/scm/auth"))
        {
            return ScmMenuConstants.BIZ_SCM_AUTH;
        }
        if (url.startsWith("/tenant/"))
        {
            return ScmMenuConstants.BIZ_TENANT;
        }
        if (url.startsWith("/interface/"))
        {
            return ScmMenuConstants.BIZ_INTEGRATION;
        }
        if (url.startsWith("/supplier/"))
        {
            return ScmMenuConstants.BIZ_SUPPLIER_MASTER;
        }
        if (url.startsWith("/hospital/"))
        {
            return ScmMenuConstants.BIZ_HOSPITAL_MASTER;
        }
        if (url.startsWith("/material/"))
        {
            return ScmMenuConstants.BIZ_MASTER_DATA;
        }
        if (url.startsWith("/certificate/"))
        {
            return ScmMenuConstants.BIZ_CERTIFICATE;
        }
        if (url.startsWith("/order/") || url.startsWith("/delivery/"))
        {
            return ScmMenuConstants.BIZ_SUPPLY_CHAIN;
        }
        if (url.startsWith("/settlement/"))
        {
            return ScmMenuConstants.BIZ_SETTLEMENT;
        }
        if (url.startsWith("/datacenter/"))
        {
            return ScmMenuConstants.BIZ_DATACENTER;
        }
        if (name.contains("数据权限") || name.contains("菜单授权") || name.contains("黑名单"))
        {
            return ScmMenuConstants.BIZ_SCM_AUTH;
        }
        if (name.contains("客户"))
        {
            return ScmMenuConstants.BIZ_TENANT;
        }
        if (ScmAuthConstants.AUTH_HOSPITAL.equals(authType))
        {
            return ScmMenuConstants.BIZ_HOSPITAL_MASTER;
        }
        if (ScmAuthConstants.AUTH_SUPPLIER.equals(authType))
        {
            return ScmMenuConstants.BIZ_SUPPLIER_MASTER;
        }
        return ScmMenuConstants.BIZ_OTHER;
    }
}
