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

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（关联供应商档案或冗余展示名） */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 订单供应商名称（主表冗余，可与供应商档案一致） */
    @Excel(name = "订单供应商名称")
    private String orderSupplierName;

    /** 订单仓库ID */
    private Long warehouseId;

    /** 订单科室ID */
    private Long orderDeptId;

    /** 订单科室名称（与 apply_dept/科室展示一致时可与 department 同步） */
    @Excel(name = "订单科室名称")
    private String orderDeptName;

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
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

