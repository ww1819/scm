package com.scm.common.constant;

/**
 * SCM 权限模型：菜单/角色类型与内置角色键
 */
public final class ScmAuthConstants
{
    private ScmAuthConstants()
    {
    }

    public static final String AUTH_PLATFORM = "platform";
    public static final String AUTH_HOSPITAL = "hospital";
    public static final String AUTH_SUPPLIER = "supplier";
    /** 医院和供应商共同可见（可按医院授予供应商） */
    public static final String AUTH_HOSPITAL_SUPPLIER = "hospital_supplier";

    public static final String ROLE_TYPE_PLATFORM = "platform";
    public static final String ROLE_TYPE_HOSPITAL = "hospital";
    public static final String ROLE_TYPE_SUPPLIER = "supplier";

    /** 每医院唯一的管理员角色 */
    public static final String ROLE_KEY_HOSPITAL_ADMIN = "hospital_admin";
    /** 每医院默认职工角色 */
    public static final String ROLE_KEY_HOSPITAL_STAFF = "hospital_staff";
    /** 每供应商唯一的管理员角色 */
    public static final String ROLE_KEY_SUPPLIER_ADMIN = "supplier_admin";
    public static final String ROLE_KEY_SUPPLIER_SALES = "supplier_sales";
}
