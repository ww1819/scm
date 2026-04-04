package com.scm.system.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.scm.system.domain.DeliveryDetail;

/**
 * 引用中设订单生成配送单时，返回给前端的结构化数据
 */
public class ZsTpOrderForDeliveryVo
{
    /** 中设订单主键 zs_tp_order.id */
    private String zsOrderId;

    /** 对应配送单「订单号」字段：中设单号 DH */
    private String orderNo;

    /** 仓库：中设 ck */
    private String warehouse;

    /** 备注（科室名称、原备注等拼接） */
    private String remark;

    /** 订单日期：取中设主表创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date orderDate;

    /** 订单金额 sheet_je */
    private BigDecimal orderAmount;

    /** 已映射为配送明细行 */
    private List<DeliveryDetail> deliveryDetails;

    public String getZsOrderId()
    {
        return zsOrderId;
    }

    public void setZsOrderId(String zsOrderId)
    {
        this.zsOrderId = zsOrderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getWarehouse()
    {
        return warehouse;
    }

    public void setWarehouse(String warehouse)
    {
        this.warehouse = warehouse;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Date getOrderDate()
    {
        return orderDate;
    }

    public void setOrderDate(Date orderDate)
    {
        this.orderDate = orderDate;
    }

    public BigDecimal getOrderAmount()
    {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount)
    {
        this.orderAmount = orderAmount;
    }

    public List<DeliveryDetail> getDeliveryDetails()
    {
        return deliveryDetails;
    }

    public void setDeliveryDetails(List<DeliveryDetail> deliveryDetails)
    {
        this.deliveryDetails = deliveryDetails;
    }
}
