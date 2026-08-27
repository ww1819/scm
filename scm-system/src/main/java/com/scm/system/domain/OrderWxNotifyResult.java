package com.scm.system.domain;

/**
 * 订单微信模板发送结果。
 */
public class OrderWxNotifyResult
{
    private int recipientCount;
    private int successCount;
    private int failCount;
    private String message;

    public int getRecipientCount()
    {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount)
    {
        this.recipientCount = recipientCount;
    }

    public int getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(int successCount)
    {
        this.successCount = successCount;
    }

    public int getFailCount()
    {
        return failCount;
    }

    public void setFailCount(int failCount)
    {
        this.failCount = failCount;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
