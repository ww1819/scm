package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.annotation.Excel;
import com.scm.common.annotation.Excel.ColumnType;
import com.scm.common.core.domain.BaseEntity;

/**
 * 配送明细表 scm_delivery_detail
 * 
 * @author scm
 */
public class DeliveryDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    @Excel(name = "明细ID", cellType = ColumnType.NUMERIC)
    private Long detailId;

    /** 配送单ID */
    private Long deliveryId;

    /** 订单明细ID */
    private Long orderDetailId;

    /** 中设明细主键 zs_tp_order_detail.id */
    private String zsOrderDetailId;

    /** 物资ID */
    private Long materialId;

    /** 产品编码 */
    @Excel(name = "产品编码")
    private String materialCode;

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String materialName;

    /** 规格 */
    @Excel(name = "规格")
    private String specification;

    /** 型号 */
    @Excel(name = "型号")
    private String model;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 剩余配送数量 */
    @Excel(name = "剩余配送数量", cellType = ColumnType.NUMERIC)
    private BigDecimal remainingQuantity;

    /** 配送数量 */
    @Excel(name = "配送数量", cellType = ColumnType.NUMERIC)
    private BigDecimal deliveryQuantity;

    /** 单价 */
    @Excel(name = "单价", cellType = ColumnType.NUMERIC)
    private BigDecimal price;

    /** 金额 */
    @Excel(name = "金额", cellType = ColumnType.NUMERIC)
    private BigDecimal amount;

    /** 打包系数 */
    private BigDecimal packCoefficient;

    /** 批号 */
    @Excel(name = "批号")
    private String batchNo;

    /** 主条码 */
    @Excel(name = "主条码")
    private String mainBarcode;

    /** 辅条码 */
    @Excel(name = "辅条码")
    private String auxBarcode;

    /** 生产日期 */
    @Excel(name = "生产日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** 有效期 */
    @Excel(name = "有效期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 注册证号 */
    @Excel(name = "注册证号")
    private String registerNo;

    /** 配送单号 */
    @Excel(name = "配送单号")
    private String deliveryNo;

    /** 所属配送单审核状态（主表 audit_status） */
    private String deliveryAuditStatus;

    /** 本系统订单ID（主表快照） */
    private Long refOrderId;

    /** 本系统订单号 */
    private String refOrderNo;

    /** 中设订单主键 */
    private String refZsOrderId;

    /** 我方订单明细订货数量 */
    private BigDecimal refOrderLineQty;

    /** 我方订单明细产品编码 */
    private String refOrderMaterialCode;

    /** 我方订单明细产品名称 */
    private String refOrderMaterialName;

    /** 中设明细数量 sl */
    private BigDecimal refZsLineQty;

    /** 中设明细编码 */
    private String refZsMaterialCode;

    /** 中设明细名称 */
    private String refZsMaterialName;

    /**
     * 订单行申请数量上限（仅用于前端校验，不落库；中设行对应 zs 明细数量）
     */
    private BigDecimal lineApplyQty;

    /** 本明细生成的条码（jsfs=3 时，查询带出） */
    private List<DeliveryDetailBarcode> detailBarcodes;

    @NotNull(message = "配送单ID不能为空")
    public Long getDeliveryId()
    {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId)
    {
        this.deliveryId = deliveryId;
    }

    public Long getOrderDetailId()
    {
        return orderDetailId;
    }

    public void setOrderDetailId(Long orderDetailId)
    {
        this.orderDetailId = orderDetailId;
    }

    public String getZsOrderDetailId()
    {
        return zsOrderDetailId;
    }

    public void setZsOrderDetailId(String zsOrderDetailId)
    {
        this.zsOrderDetailId = zsOrderDetailId;
    }

    @NotNull(message = "物资ID不能为空")
    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialCode()
    {
        return materialCode;
    }

    public void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getSpecification()
    {
        return specification;
    }

    public void setSpecification(String specification)
    {
        this.specification = specification;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public BigDecimal getRemainingQuantity()
    {
        return remainingQuantity;
    }

    public void setRemainingQuantity(BigDecimal remainingQuantity)
    {
        this.remainingQuantity = remainingQuantity;
    }

    @NotNull(message = "配送数量不能为空")
    public BigDecimal getDeliveryQuantity()
    {
        return deliveryQuantity;
    }

    public void setDeliveryQuantity(BigDecimal deliveryQuantity)
    {
        this.deliveryQuantity = deliveryQuantity;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getPackCoefficient()
    {
        return packCoefficient;
    }

    public void setPackCoefficient(BigDecimal packCoefficient)
    {
        this.packCoefficient = packCoefficient;
    }

    @Size(min = 0, max = 50, message = "批号不能超过50个字符")
    public String getBatchNo()
    {
        return batchNo;
    }

    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo;
    }

    @Size(min = 0, max = 128, message = "主条码不能超过128个字符")
    public String getMainBarcode()
    {
        return mainBarcode;
    }

    public void setMainBarcode(String mainBarcode)
    {
        this.mainBarcode = mainBarcode;
    }

    @Size(min = 0, max = 128, message = "辅条码不能超过128个字符")
    public String getAuxBarcode()
    {
        return auxBarcode;
    }

    public void setAuxBarcode(String auxBarcode)
    {
        this.auxBarcode = auxBarcode;
    }

    public Date getProductionDate()
    {
        return productionDate;
    }

    public void setProductionDate(Date productionDate)
    {
        this.productionDate = productionDate;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    @Size(min = 0, max = 200, message = "生产厂家不能超过200个字符")
    public String getManufacturer()
    {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }

    @Size(min = 0, max = 100, message = "注册证号不能超过100个字符")
    public String getRegisterNo()
    {
        return registerNo;
    }

    public void setRegisterNo(String registerNo)
    {
        this.registerNo = registerNo;
    }

    public String getDeliveryNo()
    {
        return deliveryNo;
    }

    public void setDeliveryNo(String deliveryNo)
    {
        this.deliveryNo = deliveryNo;
    }

    public String getDeliveryAuditStatus()
    {
        return deliveryAuditStatus;
    }

    public void setDeliveryAuditStatus(String deliveryAuditStatus)
    {
        this.deliveryAuditStatus = deliveryAuditStatus;
    }

    public Long getRefOrderId()
    {
        return refOrderId;
    }

    public void setRefOrderId(Long refOrderId)
    {
        this.refOrderId = refOrderId;
    }

    public String getRefOrderNo()
    {
        return refOrderNo;
    }

    public void setRefOrderNo(String refOrderNo)
    {
        this.refOrderNo = refOrderNo;
    }

    public String getRefZsOrderId()
    {
        return refZsOrderId;
    }

    public void setRefZsOrderId(String refZsOrderId)
    {
        this.refZsOrderId = refZsOrderId;
    }

    public BigDecimal getRefOrderLineQty()
    {
        return refOrderLineQty;
    }

    public void setRefOrderLineQty(BigDecimal refOrderLineQty)
    {
        this.refOrderLineQty = refOrderLineQty;
    }

    public String getRefOrderMaterialCode()
    {
        return refOrderMaterialCode;
    }

    public void setRefOrderMaterialCode(String refOrderMaterialCode)
    {
        this.refOrderMaterialCode = refOrderMaterialCode;
    }

    public String getRefOrderMaterialName()
    {
        return refOrderMaterialName;
    }

    public void setRefOrderMaterialName(String refOrderMaterialName)
    {
        this.refOrderMaterialName = refOrderMaterialName;
    }

    public BigDecimal getRefZsLineQty()
    {
        return refZsLineQty;
    }

    public void setRefZsLineQty(BigDecimal refZsLineQty)
    {
        this.refZsLineQty = refZsLineQty;
    }

    public String getRefZsMaterialCode()
    {
        return refZsMaterialCode;
    }

    public void setRefZsMaterialCode(String refZsMaterialCode)
    {
        this.refZsMaterialCode = refZsMaterialCode;
    }

    public String getRefZsMaterialName()
    {
        return refZsMaterialName;
    }

    public void setRefZsMaterialName(String refZsMaterialName)
    {
        this.refZsMaterialName = refZsMaterialName;
    }

    public BigDecimal getLineApplyQty()
    {
        return lineApplyQty;
    }

    public void setLineApplyQty(BigDecimal lineApplyQty)
    {
        this.lineApplyQty = lineApplyQty;
    }

    public List<DeliveryDetailBarcode> getDetailBarcodes()
    {
        return detailBarcodes;
    }

    public void setDetailBarcodes(List<DeliveryDetailBarcode> detailBarcodes)
    {
        this.detailBarcodes = detailBarcodes;
    }

    public Long getDetailId()
    {
        return detailId;
    }

    public void setDetailId(Long detailId)
    {
        this.detailId = detailId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("detailId", getDetailId())
            .append("deliveryId", getDeliveryId())
            .append("orderDetailId", getOrderDetailId())
            .append("materialId", getMaterialId())
            .append("materialCode", getMaterialCode())
            .append("materialName", getMaterialName())
            .append("specification", getSpecification())
            .append("model", getModel())
            .append("unit", getUnit())
            .append("remainingQuantity", getRemainingQuantity())
            .append("deliveryQuantity", getDeliveryQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("packCoefficient", getPackCoefficient())
            .append("batchNo", getBatchNo())
            .append("mainBarcode", getMainBarcode())
            .append("auxBarcode", getAuxBarcode())
            .append("productionDate", getProductionDate())
            .append("expireDate", getExpireDate())
            .append("manufacturer", getManufacturer())
            .append("registerNo", getRegisterNo())
            .append("deliveryNo", getDeliveryNo())
            .toString();
    }
}

