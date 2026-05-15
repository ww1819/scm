package com.scm.system.constants;

/**
 * 用户打印设置区分打印版式类型（与 scm_user_print_setting.print_type 一致）
 */
public final class ScmPrintPageType
{
    /** 医用耗材质量验收单 */
    public static final String ACCEPTANCE = "ACCEPTANCE";

    /** 物资配送单 */
    public static final String DELIVERY = "DELIVERY";

    private ScmPrintPageType()
    {
    }

    public static boolean isValid(String printType)
    {
        return ACCEPTANCE.equals(printType) || DELIVERY.equals(printType);
    }
}
