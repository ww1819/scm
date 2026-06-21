package com.scm.common.constant;

/**
 * 统一文件存储常量
 */
public final class ScmFileConstants
{
    private ScmFileConstants()
    {
    }

    /** 腾讯云 COS */
    public static final String STORAGE_COS = "cos";

    /** 本地磁盘 */
    public static final String STORAGE_LOCAL = "local";

    /** COS 测试页上传 */
    public static final String MODULE_COS_TEST = "cos_test";

    /** 供应商资质证件 */
    public static final String MODULE_SUPPLIER_CERTIFICATE = "supplier_certificate";

    /** 产品证件 */
    public static final String MODULE_PRODUCT_CERTIFICATE = "product_certificate";

    /** 证件图片最大大小（500KB） */
    public static final long MAX_CERTIFICATE_IMAGE_BYTES = 500L * 1024L;
}
