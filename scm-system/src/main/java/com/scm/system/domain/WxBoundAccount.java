package com.scm.system.domain;

/**
 * 当前微信已绑定的供应商账号（绑定页列表）。
 */
public class WxBoundAccount
{
    private Long userId;

    private String loginName;

    private String userName;

    /** 该账号关联的供应商名称，顿号分隔 */
    private String supplierNames;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getLoginName()
    {
        return loginName;
    }

    public void setLoginName(String loginName)
    {
        this.loginName = loginName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getSupplierNames()
    {
        return supplierNames;
    }

    public void setSupplierNames(String supplierNames)
    {
        this.supplierNames = supplierNames;
    }
}
