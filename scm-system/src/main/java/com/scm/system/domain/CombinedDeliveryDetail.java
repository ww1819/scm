package com.scm.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.scm.common.core.domain.BaseEntity;

/**
 * 合单配送明细 scm_combined_delivery_detail（主键 UUID7）
 */
public class CombinedDeliveryDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 合单明细ID（UUID7） */
    private String detailId;
    /** 合单ID（UUID7） */
    private String combinedId;
    /** 订单ID（字符串外键入库；实体 Long 对接旧订单表） */
    private Long orderId;
    private String orderNo;
    private Long orderDetailId;
    private Long deliveryId;
    private String deliveryNo;
    private Long deliveryDetailId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String specification;
    private String model;
    private String unit;
    private BigDecimal deliveryQuantity;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal packCoefficient;
    private String batchNo;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date productionDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireDate;
    private String manufacturer;
    private String registerNo;
    /** 候选行：可配送上限（不落库） */
    private BigDecimal undeliveredQty;
    /** 候选行：医院/仓库/地址（不落库） */
    private Long hospitalId;
    private String hospitalName;
    private String warehouse;
    private String deliveryAddress;

    public String getDetailId() { return detailId; }
    public void setDetailId(String detailId) { this.detailId = detailId; }
    public String getCombinedId() { return combinedId; }
    public void setCombinedId(String combinedId) { this.combinedId = combinedId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(Long orderDetailId) { this.orderDetailId = orderDetailId; }
    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }
    public String getDeliveryNo() { return deliveryNo; }
    public void setDeliveryNo(String deliveryNo) { this.deliveryNo = deliveryNo; }
    public Long getDeliveryDetailId() { return deliveryDetailId; }
    public void setDeliveryDetailId(Long deliveryDetailId) { this.deliveryDetailId = deliveryDetailId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getDeliveryQuantity() { return deliveryQuantity; }
    public void setDeliveryQuantity(BigDecimal deliveryQuantity) { this.deliveryQuantity = deliveryQuantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPackCoefficient() { return packCoefficient; }
    public void setPackCoefficient(BigDecimal packCoefficient) { this.packCoefficient = packCoefficient; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Date getProductionDate() { return productionDate; }
    public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getRegisterNo() { return registerNo; }
    public void setRegisterNo(String registerNo) { this.registerNo = registerNo; }
    public BigDecimal getUndeliveredQty() { return undeliveredQty; }
    public void setUndeliveredQty(BigDecimal undeliveredQty) { this.undeliveredQty = undeliveredQty; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
