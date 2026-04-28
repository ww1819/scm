package com.scm.common.constant;

/**
 * SCM 菜单：默认开放范围、业务分类（与 sys_menu.default_open_scope / menu_biz_category 对应）
 */
public final class ScmMenuConstants
{
    private ScmMenuConstants()
    {
    }

    /** 不进入医院/供应商建档白名单自动同步（仅角色或手工授权） */
    public static final String OPEN_SCOPE_NONE = "none";
    /** 新建医院时默认写入医院菜单白名单 */
    public static final String OPEN_SCOPE_ALL_HOSPITAL = "all_hospital";
    /** 新建供应商时默认写入供应商菜单白名单（全院级 menu_id） */
    public static final String OPEN_SCOPE_ALL_SUPPLIER = "all_supplier";
    /** 医院与供应商建档均默认同步 */
    public static final String OPEN_SCOPE_ALL = "all";

    public static final String BIZ_OTHER = "other";
    public static final String BIZ_PLATFORM_OPS = "platform_ops";
    public static final String BIZ_SCM_AUTH = "scm_auth";
    public static final String BIZ_TENANT = "tenant";
    public static final String BIZ_SUPPLIER_MASTER = "supplier_master";
    public static final String BIZ_HOSPITAL_MASTER = "hospital_master";
    public static final String BIZ_MASTER_DATA = "master_data";
    public static final String BIZ_CERTIFICATE = "certificate";
    public static final String BIZ_SUPPLY_CHAIN = "supply_chain";
    public static final String BIZ_SETTLEMENT = "settlement";
    public static final String BIZ_DATACENTER = "datacenter";
    public static final String BIZ_INTEGRATION = "integration";
}
