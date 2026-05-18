package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.system.domain.HospitalSupplier;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.system.domain.vo.OrderLineDeliveryQtyVo;
import com.scm.system.mapper.DeliveryMapper;
import com.scm.system.mapper.HospitalSupplierMapper;
import com.scm.system.mapper.OrderDeliveryTraceMapper;
import com.scm.system.mapper.OrderDetailMapper;
import com.scm.system.mapper.OrderMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.service.IOrderService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmHospitalSupplierMenuScopeService;
import com.scm.system.service.IScmHospitalSupplierPermissionService;
import com.scm.system.service.IScmSupplierContextService;
import com.scm.system.service.ScmBarcodeSeedService;

/**
 * 订单 服务层实现
 * 
 * @author scm
 */
@Service
public class OrderServiceImpl implements IOrderService
{
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderDeliveryTraceMapper orderDeliveryTraceMapper;

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private ScmBarcodeSeedService scmBarcodeSeedService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Autowired
    private IScmHospitalSupplierPermissionService hospitalSupplierPermissionService;
    @Autowired
    private IScmHospitalContextService scmHospitalContextService;
    @Autowired
    private IScmHospitalSupplierMenuScopeService scmHospitalSupplierMenuScopeService;
    @Autowired
    private HospitalSupplierMapper hospitalSupplierMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;

    /**
     * 查询订单信息
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    @Override
    public Order selectOrderById(Long orderId)
    {
        Order order = orderMapper.selectOrderById(orderId);
        if (order != null)
        {
            assertOrderViewScope(order);
            order.setOrderDetails(selectOrderDetailListByOrderId(orderId));
        }
        return order;
    }

    private void assertOrderViewScope(Order order)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null && order.getHospitalId() != null && !hospitalCtx.equals(order.getHospitalId()))
        {
            throw new ServiceException("无权查看其他医院订单");
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx != null && order.getSupplierId() != null && !supplierCtx.equals(order.getSupplierId()))
        {
            throw new ServiceException("无权查看其他供应商订单");
        }
    }

    /**
     * 查询订单列表
     * 
     * @param order 订单信息
     * @return 订单集合
     */
    @Override
    public List<Order> selectOrderList(Order order)
    {
        applySupplierHospitalDataScope(order);
        return orderMapper.selectOrderList(order);
    }

    private void applySupplierHospitalDataScope(Order order)
    {
        Long userId = ShiroUtils.getUserId();
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null)
        {
            order.setHospitalId(hospitalCtx);
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx == null)
        {
            return;
        }
        List<Long> roleSupplierIds = resolveUserRoleSupplierIds(userId);
        if (!roleSupplierIds.isEmpty())
        {
            if (!roleSupplierIds.contains(supplierCtx))
            {
                order.getParams().put("scopePairBlock", Boolean.TRUE);
                return;
            }
            order.getParams().put("roleSupplierIds", roleSupplierIds);
        }
        order.setSupplierId(supplierCtx);
        List<Long> forbid = hospitalSupplierPermissionService.listForbidSubmitHospitalIds(supplierCtx);
        if (forbid != null && !forbid.isEmpty())
        {
            order.getParams().put("excludeHospitalIds", forbid);
        }
        scmHospitalSupplierMenuScopeService.applyMenuPairScopeToParams(order.getParams(), ShiroUtils.getUserId());
    }

    private void assertSupplierHospitalSubmit(Order order)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null)
        {
            if (order.getHospitalId() != null && !hospitalCtx.equals(order.getHospitalId()))
            {
                throw new ServiceException("无权访问其他医院数据");
            }
            order.setHospitalId(hospitalCtx);
            if (order.getSupplierId() != null)
            {
                assertHospitalSupplierBound(hospitalCtx, order.getSupplierId());
            }
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx == null)
        {
            return;
        }
        List<Long> roleSupplierIds = resolveUserRoleSupplierIds(ShiroUtils.getUserId());
        if (!roleSupplierIds.isEmpty() && !roleSupplierIds.contains(supplierCtx))
        {
            throw new ServiceException("当前角色无供应商数据权限");
        }
        Long hid = order.getHospitalId();
        if (hid == null)
        {
            return;
        }
        Long sid = order.getSupplierId() != null ? order.getSupplierId() : supplierCtx;
        if (!supplierCtx.equals(sid))
        {
            throw new ServiceException("无权代其他供应商提交订单数据");
        }
        if (!roleSupplierIds.isEmpty() && !roleSupplierIds.contains(sid))
        {
            throw new ServiceException("当前角色无该供应商数据权限");
        }
        hospitalSupplierPermissionService.assertSubmitAllowed(hid, sid);
        assertHospitalSupplierBound(hid, sid);
    }

    private List<Long> resolveUserRoleSupplierIds(Long userId)
    {
        if (userId == null)
        {
            return new ArrayList<>();
        }
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        if (roles == null || roles.isEmpty())
        {
            return new ArrayList<>();
        }
        Set<Long> out = new HashSet<>();
        for (SysRole role : roles)
        {
            if (role != null && role.getSupplierId() != null)
            {
                out.add(role.getSupplierId());
            }
        }
        return new ArrayList<>(out);
    }

    private void assertHospitalSupplierBound(Long hospitalId, Long supplierId)
    {
        if (hospitalId == null || supplierId == null)
        {
            return;
        }
        HospitalSupplier q = new HospitalSupplier();
        q.setHospitalId(hospitalId);
        q.setSupplierId(supplierId);
        List<HospitalSupplier> rels = hospitalSupplierMapper.selectHospitalSupplierList(q);
        if (rels == null || rels.isEmpty())
        {
            throw new ServiceException("医院与供应商未建立有效关联");
        }
    }

    /**
     * 新增订单信息
     * 
     * @param order 订单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertOrder(Order order)
    {
        throw new ServiceException("订单不支持新增，仅可接收或作废");
    }

    /**
     * 修改订单信息
     * 
     * @param order 订单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateOrder(Order order)
    {
        throw new ServiceException("订单不支持修改，仅可接收或作废");
    }

    /**
     * 批量删除订单信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteOrderByIds(String ids)
    {
        throw new ServiceException("订单不支持删除，仅可接收或作废");
    }

    /**
     * 删除订单信息
     * 
     * @param orderId 订单ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteOrderById(Long orderId)
    {
        throw new ServiceException("订单不支持删除，仅可接收或作废");
    }

    /**
     * 接收订单
     * 
     * @param orderId 订单ID
     * @return 结果
     */
    @Override
    public int receiveOrder(Long orderId)
    {
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null)
        {
            return 0;
        }
        order.setOrderStatus("1"); // 已接收
        order.setUpdateTime(DateUtils.getNowDate());
        return orderMapper.updateOrder(order);
    }

    @Override
    public int autoReceiveOrderOnDeliveryReference(Long orderId, String receiverLoginName)
    {
        if (orderId == null)
        {
            return 0;
        }
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null || !"0".equals(StringUtils.trimToNull(order.getOrderStatus())))
        {
            return 0;
        }
        order.setOrderStatus("1");
        order.setUpdateBy(StringUtils.trimToEmpty(receiverLoginName));
        order.setUpdateTime(DateUtils.getNowDate());
        return orderMapper.updateOrder(order);
    }

    /**
     * 作废订单：仅医院可操作；配送中/已完成/已作废不可作废；已有配送单不可作废。
     */
    @Override
    public int voidOrder(Long orderId, String voidBy)
    {
        if (orderId == null)
        {
            return 0;
        }
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx == null)
        {
            throw new ServiceException("仅医院账号可作废订单");
        }
        Order order = orderMapper.selectOrderById(orderId);
        if (order == null)
        {
            return 0;
        }
        assertOrderViewScope(order);
        if (!hospitalCtx.equals(order.getHospitalId()))
        {
            throw new ServiceException("无权作废该订单");
        }
        String status = StringUtils.trimToNull(order.getOrderStatus());
        if ("4".equals(status))
        {
            throw new ServiceException("作废失败：订单已作废或不存在，订单号：" + StringUtils.trimToEmpty(order.getOrderNo()));
        }
        if ("2".equals(status) || "3".equals(status))
        {
            throw new ServiceException("配送中或已完成的订单不允许作废，订单号：" + StringUtils.trimToEmpty(order.getOrderNo()));
        }
        int deliveryCnt = deliveryMapper.countDeliveryByOrderId(orderId);
        if (deliveryCnt > 0)
        {
            throw new ServiceException("该订单已存在配送单（" + deliveryCnt + " 条），不允许作废。订单号："
                + StringUtils.trimToEmpty(order.getOrderNo()));
        }
        order.setOrderStatus("4");
        order.setUpdateBy(StringUtils.trimToEmpty(voidBy));
        order.setUpdateTime(DateUtils.getNowDate());
        int n = orderMapper.updateOrder(order);
        if (n <= 0)
        {
            throw new ServiceException("作废失败，数据可能已变更，请刷新后重试。订单号："
                + StringUtils.trimToEmpty(order.getOrderNo()));
        }
        return n;
    }

    /**
     * 查询订单明细列表
     * 
     * @param orderId 订单ID
     * @return 明细集合
     */
    @Override
    public List<OrderDetail> selectOrderDetailListByOrderId(Long orderId)
    {
        List<OrderDetail> list = orderDetailMapper.selectOrderDetailListByOrderId(orderId);
        enrichScmOrderDetailsDeliveryQty(list, orderId);
        return list;
    }

    private void enrichScmOrderDetailsDeliveryQty(List<OrderDetail> list, Long orderId)
    {
        if (list == null || list.isEmpty() || orderId == null)
        {
            return;
        }
        List<OrderLineDeliveryQtyVo> agg = orderDeliveryTraceMapper.selectScmOrderLineDeliveryQtyByOrderId(orderId);
        Map<String, OrderLineDeliveryQtyVo> map = new HashMap<>();
        for (OrderLineDeliveryQtyVo row : agg)
        {
            if (row != null && StringUtils.isNotEmpty(row.getLineKey()))
            {
                map.put(row.getLineKey(), row);
            }
        }
        for (OrderDetail od : list)
        {
            String key = od.getDetailId() == null ? null : String.valueOf(od.getDetailId());
            OrderLineDeliveryQtyVo q = key == null ? null : map.get(key);
            BigDecimal oq = od.getOrderQuantity() == null ? BigDecimal.ZERO
                : BigDecimal.valueOf(od.getOrderQuantity().longValue());
            BigDecimal a = (q != null && q.getAuditedQty() != null) ? q.getAuditedQty() : BigDecimal.ZERO;
            BigDecimal p = (q != null && q.getPendingQty() != null) ? q.getPendingQty() : BigDecimal.ZERO;
            BigDecimal rj = (q != null && q.getRejectedQty() != null) ? q.getRejectedQty() : BigDecimal.ZERO;
            od.setDeliveredAuditedQty(a);
            od.setDeliveredPendingAuditQty(p);
            BigDecimal und = oq.subtract(a).subtract(p).subtract(rj);
            if (und.compareTo(BigDecimal.ZERO) < 0)
            {
                und = BigDecimal.ZERO;
            }
            od.setUndeliveredQty(und);
        }
    }

    /**
     * 生成唯一的订单编号
     * 
     * @return 订单编号
     */
    private String generateOrderNo()
    {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do
        {
            // 使用时间戳+随机数生成编号
            code = "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            if (code.length() > 50)
            {
                code = code.substring(0, 50);
            }
            attempt++;
        }
        while (orderMapper.selectOrderByOrderNo(code) != null && attempt < maxAttempts);
        
        if (attempt >= maxAttempts)
        {
            // 如果10次尝试都失败，使用UUID
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            code = "ORD" + uuid.substring(0, Math.min(20, uuid.length()));
        }
        
        return code;
    }
}

