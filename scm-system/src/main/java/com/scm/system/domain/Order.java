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
 * 订单主表 scm_order
 * 
 * @author scm
 */
public class Order extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @Excel(name = "订单ID", cellType = ColumnType.NUMERIC)
    private Long orderId;

    /** 订单编号 */
    @Excel(name = "订单编号")
    private String orderNo;

    /** 医院ID */
    private Long hospitalId;

    /** 医院名称 */
    @Excel(name = "医院名称")
    private String hospitalName;

    /** 供应商ID（平台 scm_supplier.supplier_id） */
    private Long supplierId;

    /** 平台供应商编码（scm_supplier.supplier_code） */
    @Excel(name = "供应商编码")
    private String supplierCode;

    /** SPD 采购订单供应商主键 purchase_order.supplier_id（字符串；与平台 scm_supplier.supplier_id 区分） */
    private String spdSupplierId;

    /** 供应商名称（关联供应商档案或冗余展示名） */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 订单供应商名称（主表冗余，可与供应商档案一致） */
    @Excel(name = "订单供应商名称")
    private String orderSupplierName;

    /** 订单仓库ID */
    private Long warehouseId;

    /** 订单仓库编码（SPD fd_warehouse.code 快照） */
    private String warehouseCode;

    /** 订单科室ID */
    private Long orderDeptId;

    /** 订单科室名称（与 apply_dept/科室展示一致时可与 department 同步） */
    @Excel(name = "订单科室名称")
    private String orderDeptName;

    /** 订单科室编码（SPD fd_department.code 快照） */
    private String orderDeptCode;

    /** 订单金额 */
    @Excel(name = "订单金额", cellType = ColumnType.NUMERIC)
    private BigDecimal orderAmount;

    /** 要货仓库 */
    @Excel(name = "要货仓库")
    private String warehouse;

    /** 订单日期 */
    @Excel(name = "订单日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date orderDate;

    /** 订单状态（0待接收 1已接收 2配送中 3已完成 4已取消） */
    @Excel(name = "订单状态", readConverterExp = "0=待接收,1=已接收,2=配送中,3=已完成,4=已取消")
    private String orderStatus;

    /** 申请科室 */
    @Excel(name = "申请科室")
    private String department;

    /** 医院经办人 */
    @Excel(name = "医院经办人")
    private String hospitalHandler;

    /** 计划号（从备注「从采购计划JH…生成」解析） */
    @Excel(name = "计划号")
    private String planNo;

    /** 接收人登录名 */
    private String receiveBy;

    /** 接收人姓名快照 */
    @Excel(name = "接收人")
    private String receiveByNameSnapshot;

    /** 列表展示用接收人（快照优先） */
    private String receiveByDisplay;

    /** 接收时间 */
    @Excel(name = "接收日期", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;

    /** 订单明细 */
    private List<OrderDetail> orderDetails;

    @Size(min = 0, max = 50, message = "订单编号不能超过50个字符")
    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName()
    {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getSupplierCode()
    {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode)
    {
        this.supplierCode = supplierCode;
    }

    public String getSpdSupplierId()
    {
        return spdSupplierId;
    }

    public void setSpdSupplierId(String spdSupplierId)
    {
        this.spdSupplierId = spdSupplierId;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getOrderSupplierName()
    {
        return orderSupplierName;
    }

    public void setOrderSupplierName(String orderSupplierName)
    {
        this.orderSupplierName = orderSupplierName;
    }

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseCode()
    {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode)
    {
        this.warehouseCode = warehouseCode;
    }

    public Long getOrderDeptId()
    {
        return orderDeptId;
    }

    public void setOrderDeptId(Long orderDeptId)
    {
        this.orderDeptId = orderDeptId;
    }

    public String getOrderDeptName()
    {
        return orderDeptName;
    }

    public void setOrderDeptName(String orderDeptName)
    {
        this.orderDeptName = orderDeptName;
    }

    public String getOrderDeptCode()
    {
        return orderDeptCode;
    }

    public void setOrderDeptCode(String orderDeptCode)
    {
        this.orderDeptCode = orderDeptCode;
    }

    public BigDecimal getOrderAmount()
    {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount)
    {
        this.orderAmount = orderAmount;
    }

    @Size(min = 0, max = 200, message = "要货仓库不能超过200个字符")
    public String getWarehouse()
    {
        return warehouse;
    }

    public void setWarehouse(String warehouse)
    {
        this.warehouse = warehouse;
    }

    public Date getOrderDate()
    {
        return orderDate;
    }

    public void setOrderDate(Date orderDate)
    {
        this.orderDate = orderDate;
    }

    public String getOrderStatus()
    {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus)
    {
        this.orderStatus = orderStatus;
    }

    @Size(min = 0, max = 200, message = "申请科室不能超过200个字符")
    public String getDepartment()
    {
        return department;
    }

    public void setDepartment(String department)
    {
        this.department = department;
    }

    public String getHospitalHandler()
    {
        return hospitalHandler;
    }

    public void setHospitalHandler(String hospitalHandler)
    {
        this.hospitalHandler = hospitalHandler;
    }

    public String getPlanNo()
    {
        return planNo;
    }

    public void setPlanNo(String planNo)
    {
        this.planNo = planNo;
    }

    public String getReceiveBy()
    {
        return receiveBy;
    }

    public void setReceiveBy(String receiveBy)
    {
        this.receiveBy = receiveBy;
    }

    public String getReceiveByNameSnapshot()
    {
        return receiveByNameSnapshot;
    }

    public void setReceiveByNameSnapshot(String receiveByNameSnapshot)
    {
        this.receiveByNameSnapshot = receiveByNameSnapshot;
    }

    public String getReceiveByDisplay()
    {
        return receiveByDisplay;
    }

    public void setReceiveByDisplay(String receiveByDisplay)
    {
        this.receiveByDisplay = receiveByDisplay;
    }

    public Date getReceiveTime()
    {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime)
    {
        this.receiveTime = receiveTime;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public List<OrderDetail> getOrderDetails()
    {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails)
    {
        this.orderDetails = orderDetails;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("hospitalId", getHospitalId())
            .append("hospitalName", getHospitalName())
            .append("supplierId", getSupplierId())
            .append("supplierCode", getSupplierCode())
            .append("spdSupplierId", getSpdSupplierId())
            .append("supplierName", getSupplierName())
            .append("orderSupplierName", getOrderSupplierName())
            .append("warehouseId", getWarehouseId())
            .append("orderDeptId", getOrderDeptId())
            .append("orderDeptName", getOrderDeptName())
            .append("orderAmount", getOrderAmount())
            .append("warehouse", getWarehouse())
            .append("orderDate", getOrderDate())
            .append("orderStatus", getOrderStatus())
            .append("department", getDepartment())
            .append("hospitalHandler", getHospitalHandler())
            .append("planNo", getPlanNo())
            .append("receiveBy", getReceiveBy())
            .append("receiveByNameSnapshot", getReceiveByNameSnapshot())
            .append("receiveByDisplay", getReceiveByDisplay())
            .append("receiveTime", getReceiveTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

