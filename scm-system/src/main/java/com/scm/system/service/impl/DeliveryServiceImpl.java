package com.scm.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.DeliveryRefOrderSource;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.Order;
import com.scm.system.domain.OrderDetail;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.system.domain.ScmOrderDetailDeliveryRel;
import com.scm.system.domain.ZsTpOrder;
import com.scm.system.domain.ZsTpOrderDetail;
import com.scm.system.domain.ZsTpOrderDetailDeliveryRel;
import com.scm.system.domain.vo.OrderDetailDeliveryTraceVo;
import com.scm.system.domain.vo.OrderLineDeliveryQtyVo;
import com.scm.system.domain.vo.ZsTpOrderForDeliveryVo;
import com.scm.system.mapper.DeliveryDetailMapper;
import com.scm.system.mapper.DeliveryMapper;
import com.scm.system.mapper.HospitalSupplierMapper;
import com.scm.system.mapper.OrderDeliveryTraceMapper;
import com.scm.system.mapper.OrderDetailMapper;
import com.scm.system.mapper.OrderMapper;
import com.scm.system.mapper.ScmOrderDetailDeliveryRelMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.ZsTpOrderDetailDeliveryRelMapper;
import com.scm.system.mapper.ZsTpOrderMapper;
import com.scm.system.service.IDeliveryService;
import com.scm.system.service.IScmHospitalContextService;
import com.scm.system.service.IScmHospitalSupplierMenuScopeService;
import com.scm.system.service.IScmHospitalSupplierPermissionService;
import com.scm.system.service.IScmSupplierContextService;
import com.scm.system.service.ScmBarcodeSeedService;

/**
 * 配送单 服务层实现
 *
 * @author scm
 */
@Service
public class DeliveryServiceImpl implements IDeliveryService
{
    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private DeliveryDetailMapper deliveryDetailMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ZsTpOrderMapper zsTpOrderMapper;

    @Autowired
    private ScmOrderDetailDeliveryRelMapper scmOrderDetailDeliveryRelMapper;

    @Autowired
    private ZsTpOrderDetailDeliveryRelMapper zsTpOrderDetailDeliveryRelMapper;

    @Autowired
    private OrderDeliveryTraceMapper orderDeliveryTraceMapper;

    @Autowired
    private ScmBarcodeSeedService scmBarcodeSeedService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;
    @Autowired
    private IScmHospitalContextService scmHospitalContextService;

    @Autowired
    private IScmHospitalSupplierPermissionService hospitalSupplierPermissionService;
    @Autowired
    private IScmHospitalSupplierMenuScopeService scmHospitalSupplierMenuScopeService;
    @Autowired
    private HospitalSupplierMapper hospitalSupplierMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;

    /**
     * 查询配送单信息
     *
     * @param deliveryId 配送单ID
     * @return 配送单信息
     */
    @Override
    public Delivery selectDeliveryById(Long deliveryId)
    {
        Delivery delivery = deliveryMapper.selectDeliveryById(deliveryId);
        if (delivery != null)
        {
            assertDeliveryViewScope(delivery);
            List<DeliveryDetail> details = deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(deliveryId);
            enrichDetailLineApplyQty(details);
            scmBarcodeSeedService.attachDetailBarcodes(details, deliveryId);
            delivery.setDeliveryDetails(details);
        }
        return delivery;
    }

    private void assertDeliveryViewScope(Delivery delivery)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null && delivery.getHospitalId() != null && !hospitalCtx.equals(delivery.getHospitalId()))
        {
            throw new ServiceException("无权查看其他医院配送单");
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx != null && delivery.getSupplierId() != null && !supplierCtx.equals(delivery.getSupplierId()))
        {
            throw new ServiceException("无权查看其他供应商配送单");
        }
    }

    /**
     * 查询配送单列表
     *
     * @param delivery 配送单信息
     * @return 配送单集合
     */
    @Override
    public List<Delivery> selectDeliveryList(Delivery delivery)
    {
        applySupplierHospitalDataScope(delivery);
        return deliveryMapper.selectDeliveryList(delivery);
    }

    /**
     * 供应商登录：强制本供应商维度，并排除「禁止提交」的医院
     */
    private void applySupplierHospitalDataScope(Delivery delivery)
    {
        Long userId = ShiroUtils.getUserId();
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null)
        {
            delivery.setHospitalId(hospitalCtx);
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
                delivery.getParams().put("scopePairBlock", Boolean.TRUE);
                return;
            }
            delivery.getParams().put("roleSupplierIds", roleSupplierIds);
        }
        delivery.setSupplierId(supplierCtx);
        java.util.List<Long> forbid = hospitalSupplierPermissionService.listForbidSubmitHospitalIds(supplierCtx);
        if (forbid != null && !forbid.isEmpty())
        {
            delivery.getParams().put("excludeHospitalIds", forbid);
        }
        scmHospitalSupplierMenuScopeService.applyMenuPairScopeToParams(delivery.getParams(), ShiroUtils.getUserId());
    }

    private void assertSupplierHospitalSubmit(Delivery delivery)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null)
        {
            if (delivery.getHospitalId() != null && !hospitalCtx.equals(delivery.getHospitalId()))
            {
                throw new ServiceException("无权访问其他医院数据");
            }
            delivery.setHospitalId(hospitalCtx);
            if (delivery.getSupplierId() != null)
            {
                assertHospitalSupplierBound(hospitalCtx, delivery.getSupplierId());
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
        Long hid = delivery.getHospitalId();
        if (hid == null)
        {
            return;
        }
        Long sid = delivery.getSupplierId() != null ? delivery.getSupplierId() : supplierCtx;
        if (!supplierCtx.equals(sid))
        {
            throw new ServiceException("无权代其他供应商提交配送数据");
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
     * 新增配送单信息
     *
     * @param delivery 配送单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertDelivery(Delivery delivery)
    {
        enrichDeliverySnapshot(delivery);
        assertSupplierHospitalSubmit(delivery);
        enrichDeliveryDetailPackCoefficients(delivery);
        validateDeliveryDetailPackQuantities(delivery.getDeliveryDetails());
        validateDeliveryRefLineQuantities(delivery, null);
        if (StringUtils.isEmpty(delivery.getDeliveryStatus()))
        {
            delivery.setDeliveryStatus("0"); // 默认未审核
        }
        if (StringUtils.isEmpty(delivery.getAuditStatus()))
        {
            delivery.setAuditStatus("0"); // 待审核
        }
        // 如果配送单号为空，自动生成唯一编号
        if (StringUtils.isEmpty(delivery.getDeliveryNo()))
        {
            delivery.setDeliveryNo(generateDeliveryNo());
        }
        delivery.setCreateTime(DateUtils.getNowDate());

        // 计算配送金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (delivery.getDeliveryDetails() != null && !delivery.getDeliveryDetails().isEmpty())
        {
            for (DeliveryDetail detail : delivery.getDeliveryDetails())
            {
                if (detail.getPrice() != null && detail.getDeliveryQuantity() != null)
                {
                    BigDecimal amount = detail.getPrice().multiply(detail.getDeliveryQuantity());
                    detail.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
        }
        delivery.setDeliveryAmount(totalAmount);

        int rows = deliveryMapper.insertDelivery(delivery);

        // 保存配送明细
        if (delivery.getDeliveryDetails() != null && !delivery.getDeliveryDetails().isEmpty())
        {
            for (DeliveryDetail detail : delivery.getDeliveryDetails())
            {
                detail.setDeliveryId(delivery.getDeliveryId());
            }
            deliveryDetailMapper.batchInsertDeliveryDetail(delivery.getDeliveryDetails());

            // 更新订单明细的剩余待配送数
            if (delivery.getOrderId() != null)
            {
                updateOrderRemainingQuantity(delivery);
            }
            insertOrderDeliveryDetailRelations(delivery);
            List<DeliveryDetail> savedForBarcode = deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(delivery.getDeliveryId());
            scmBarcodeSeedService.createZsDeliveryDetailBarcodesIfNeeded(delivery, savedForBarcode);
        }

        if (rows > 0)
        {
            maybeAutoConfirmZsTpOrderOnDeliverySave(delivery);
        }
        return rows;
    }

    /**
     * 配送单保存时：若引用了未确认的第三方订单，则自动确认，确认人为配送单制单人（create_by，缺省为 update_by 或当前登录名）
     */
    private void maybeAutoConfirmZsTpOrderOnDeliverySave(Delivery delivery)
    {
        if (delivery == null || StringUtils.isEmpty(delivery.getZsOrderId()))
        {
            return;
        }
        if (StringUtils.isEmpty(StringUtils.trimToNull(delivery.getCreateBy())) && delivery.getDeliveryId() != null)
        {
            Delivery persisted = deliveryMapper.selectDeliveryById(delivery.getDeliveryId());
            if (persisted != null && StringUtils.isNotEmpty(persisted.getCreateBy()))
            {
                delivery.setCreateBy(persisted.getCreateBy());
            }
        }
        ZsTpOrder head = zsTpOrderMapper.selectZsTpOrderById(delivery.getZsOrderId());
        if (head == null || "1".equals(StringUtils.trimToNull(head.getVoidStatus())))
        {
            return;
        }
        if ("1".equals(StringUtils.trimToNull(head.getConfirmStatus())))
        {
            return;
        }
        String confirmBy = resolveDeliveryDocumentCreator(delivery);
        Date now = DateUtils.getNowDate();
        zsTpOrderMapper.updateZsTpOrderConfirm(delivery.getZsOrderId(), confirmBy, now);
    }

    private String resolveDeliveryDocumentCreator(Delivery delivery)
    {
        String a = StringUtils.trimToNull(delivery.getCreateBy());
        if (a != null)
        {
            return a;
        }
        String b = StringUtils.trimToNull(delivery.getUpdateBy());
        if (b != null)
        {
            return b;
        }
        return ShiroUtils.getLoginName();
    }

    /**
     * 校验配送单是否允许进入编辑（未审核等）
     */
    @Override
    public void assertDeliveryEditable(Long deliveryId)
    {
        if (deliveryId == null)
        {
            throw new ServiceException("配送单ID不能为空");
        }
        Delivery existing = deliveryMapper.selectDeliveryById(deliveryId);
        assertDeliveryNotAudited(existing, "修改");
    }

    /**
     * 修改配送单信息
     *
     * @param delivery 配送单信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDelivery(Delivery delivery)
    {
        assertDeliveryEditable(delivery.getDeliveryId());

        delivery.setUpdateTime(DateUtils.getNowDate());
        enrichDeliverySnapshot(delivery);
        assertSupplierHospitalSubmit(delivery);
        enrichDeliveryDetailPackCoefficients(delivery);
        validateDeliveryDetailPackQuantities(delivery.getDeliveryDetails());
        validateDeliveryRefLineQuantities(delivery, delivery.getDeliveryId());

        // 如果修改了明细，重新计算配送金额
        if (delivery.getDeliveryDetails() != null && !delivery.getDeliveryDetails().isEmpty())
        {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (DeliveryDetail detail : delivery.getDeliveryDetails())
            {
                if (detail.getPrice() != null && detail.getDeliveryQuantity() != null)
                {
                    BigDecimal amount = detail.getPrice().multiply(detail.getDeliveryQuantity());
                    detail.setAmount(amount);
                    totalAmount = totalAmount.add(amount);
                }
            }
            delivery.setDeliveryAmount(totalAmount);
        }

        int r = deliveryMapper.updateDelivery(delivery);
        if (r > 0)
        {
            if (delivery.getDeliveryDetails() != null)
            {
                persistDeliveryDetailsOnUpdate(delivery);
            }
            maybeAutoConfirmZsTpOrderOnDeliverySave(delivery);
        }
        return r;
    }

    /**
     * 修改配送单时合并写明细：删除新列表中不存在的旧行；已有行在原记录上更新；纯新增行插入。
     */
    private void persistDeliveryDetailsOnUpdate(Delivery delivery)
    {
        Long deliveryId = delivery.getDeliveryId();
        if (deliveryId == null)
        {
            return;
        }
        List<DeliveryDetail> newLines = delivery.getDeliveryDetails();
        if (newLines == null)
        {
            return;
        }
        if (newLines.isEmpty())
        {
            throw new ServiceException("配送明细不能为空");
        }
        Delivery persisted = deliveryMapper.selectDeliveryById(deliveryId);
        if (persisted == null)
        {
            throw new ServiceException("配送单不存在");
        }
        if (delivery.getTenantId() == null && persisted.getTenantId() != null)
        {
            delivery.setTenantId(persisted.getTenantId());
        }
        List<DeliveryDetail> oldLines = deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(deliveryId);
        Map<Long, DeliveryDetail> oldById = new HashMap<>();
        if (oldLines != null)
        {
            for (DeliveryDetail o : oldLines)
            {
                if (o != null && o.getDetailId() != null)
                {
                    oldById.put(o.getDetailId(), o);
                }
            }
        }
        Set<Long> keptDetailIds = new HashSet<>();
        for (DeliveryDetail n : newLines)
        {
            if (n != null && n.getDetailId() != null)
            {
                keptDetailIds.add(n.getDetailId());
            }
        }
        String detailDelBy = resolveDelByForDetailOp(delivery);
        if (oldLines != null)
        {
            for (DeliveryDetail old : oldLines)
            {
                if (old != null && old.getDetailId() != null && !keptDetailIds.contains(old.getDetailId()))
                {
                    removeDeliveryDetailAndSideEffects(persisted, old, detailDelBy);
                }
            }
        }

        String savedCreateBy = delivery.getCreateBy();
        if (StringUtils.isEmpty(StringUtils.trimToNull(delivery.getCreateBy())))
        {
            delivery.setCreateBy(StringUtils.isNotEmpty(delivery.getUpdateBy()) ? delivery.getUpdateBy()
                : persisted.getCreateBy());
        }
        String createBy = StringUtils.trimToEmpty(delivery.getCreateBy());
        String timeStr = DateUtils.getTime();

        for (DeliveryDetail cur : newLines)
        {
            if (cur == null)
            {
                continue;
            }
            cur.setDeliveryId(deliveryId);
            Long cid = cur.getDetailId();
            DeliveryDetail old = cid != null ? oldById.get(cid) : null;
            if (old == null)
            {
                cur.setDetailId(null);
                deliveryDetailMapper.insertDeliveryDetail(cur);
                subtractOrderLineRemaining(delivery.getOrderId(), cur);
                insertOneOrderDeliveryDetailRel(delivery, cur, createBy, timeStr);
                continue;
            }
            if (!deliveryId.equals(old.getDeliveryId()))
            {
                throw new ServiceException("配送明细不属于当前配送单");
            }
            if (deliveryLineRefChanged(delivery, old, cur))
            {
                addBackOrderLineRemaining(persisted.getOrderId(), old);
                String did = String.valueOf(old.getDetailId());
                scmOrderDetailDeliveryRelMapper.deleteByDeliveryDetailId(did);
                zsTpOrderDetailDeliveryRelMapper.deleteByDeliveryDetailId(did);
                scmBarcodeSeedService.deleteBarcodesByDeliveryDetailId(old.getDetailId());
                deliveryDetailMapper.updateDeliveryDetail(cur);
                subtractOrderLineRemaining(delivery.getOrderId(), cur);
                insertOneOrderDeliveryDetailRel(delivery, cur, createBy, timeStr);
            }
            else
            {
                applyOrderRemainingOnDetailQtyOrOrderLineChange(persisted.getOrderId(), delivery.getOrderId(), old, cur);
                deliveryDetailMapper.updateDeliveryDetail(cur);
            }
        }

        delivery.setCreateBy(savedCreateBy);
        if (StringUtils.isNotEmpty(delivery.getZsOrderId()) && "3".equals(StringUtils.trimToNull(delivery.getZsJsfs())))
        {
            scmBarcodeSeedService.deleteBarcodesByDeliveryId(deliveryId);
            scmBarcodeSeedService.createZsDeliveryDetailBarcodesIfNeeded(delivery,
                deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(deliveryId));
        }
    }

    private static boolean deliveryLineRefChanged(Delivery delivery, DeliveryDetail prev, DeliveryDetail cur)
    {
        if (StringUtils.isNotEmpty(delivery.getZsOrderId()))
        {
            return !StringUtils.equals(StringUtils.trimToEmpty(prev.getZsOrderDetailId()),
                StringUtils.trimToEmpty(cur.getZsOrderDetailId()));
        }
        return !Objects.equals(prev.getOrderDetailId(), cur.getOrderDetailId());
    }

    private void removeDeliveryDetailAndSideEffects(Delivery persisted, DeliveryDetail old, String delBy)
    {
        if (old == null || old.getDetailId() == null)
        {
            return;
        }
        addBackOrderLineRemaining(persisted.getOrderId(), old);
        String did = String.valueOf(old.getDetailId());
        scmOrderDetailDeliveryRelMapper.deleteByDeliveryDetailId(did);
        zsTpOrderDetailDeliveryRelMapper.deleteByDeliveryDetailId(did);
        scmBarcodeSeedService.deleteBarcodesByDeliveryDetailId(old.getDetailId());
        deliveryDetailMapper.deleteDeliveryDetailById(old.getDetailId(), StringUtils.trimToEmpty(delBy));
    }

    private String resolveDelByForDetailOp(Delivery delivery)
    {
        if (delivery != null && StringUtils.isNotEmpty(StringUtils.trimToNull(delivery.getUpdateBy())))
        {
            return StringUtils.trimToEmpty(delivery.getUpdateBy());
        }
        return StringUtils.trimToEmpty(ShiroUtils.getLoginName());
    }

    private void addBackOrderLineRemaining(Long orderId, DeliveryDetail deliveryDetail)
    {
        if (orderId == null || deliveryDetail == null || deliveryDetail.getOrderDetailId() == null)
        {
            return;
        }
        OrderDetail orderDetail = orderDetailMapper.selectOrderDetailById(deliveryDetail.getOrderDetailId());
        if (orderDetail == null || orderDetail.getOrderId() == null || !orderId.equals(orderDetail.getOrderId()))
        {
            return;
        }
        if (deliveryDetail.getDeliveryQuantity() == null)
        {
            return;
        }
        int rem = orderDetail.getRemainingQuantity() != null ? orderDetail.getRemainingQuantity().intValue() : 0;
        BigDecimal remaining = BigDecimal.valueOf(rem).add(deliveryDetail.getDeliveryQuantity());
        orderDetail.setRemainingQuantity(remaining.intValue());
        orderDetailMapper.updateOrderDetail(orderDetail);
    }

    private void subtractOrderLineRemaining(Long orderId, DeliveryDetail deliveryDetail)
    {
        if (orderId == null || deliveryDetail == null || deliveryDetail.getOrderDetailId() == null)
        {
            return;
        }
        OrderDetail orderDetail = orderDetailMapper.selectOrderDetailById(deliveryDetail.getOrderDetailId());
        if (orderDetail == null || orderDetail.getOrderId() == null || !orderId.equals(orderDetail.getOrderId()))
        {
            return;
        }
        BigDecimal qty = deliveryDetail.getDeliveryQuantity() != null ? deliveryDetail.getDeliveryQuantity() : BigDecimal.ZERO;
        int base = orderDetail.getRemainingQuantity() != null ? orderDetail.getRemainingQuantity().intValue() : 0;
        BigDecimal remaining = new BigDecimal(base).subtract(qty);
        if (remaining.compareTo(BigDecimal.ZERO) < 0)
        {
            remaining = BigDecimal.ZERO;
        }
        orderDetail.setRemainingQuantity(remaining.intValue());
        orderDetailMapper.updateOrderDetail(orderDetail);
    }

    private void applyOrderRemainingOnDetailQtyOrOrderLineChange(Long persistedOrderId, Long newOrderId, DeliveryDetail old,
        DeliveryDetail cur)
    {
        if (newOrderId == null)
        {
            return;
        }
        BigDecimal oq = old.getDeliveryQuantity() != null ? old.getDeliveryQuantity() : BigDecimal.ZERO;
        BigDecimal nq = cur.getDeliveryQuantity() != null ? cur.getDeliveryQuantity() : BigDecimal.ZERO;
        Long oOd = old.getOrderDetailId();
        Long nOd = cur.getOrderDetailId();
        if (Objects.equals(oOd, nOd) && Objects.equals(persistedOrderId, newOrderId) && oOd != null)
        {
            BigDecimal delta = nq.subtract(oq);
            if (delta.compareTo(BigDecimal.ZERO) != 0)
            {
                applyOrderDetailRemainingDelta(newOrderId, nOd, delta);
            }
            return;
        }
        if (!Objects.equals(persistedOrderId, newOrderId))
        {
            addBackOrderLineRemaining(persistedOrderId, old);
            subtractOrderLineRemaining(newOrderId, cur);
            return;
        }
        if (oOd == null && nOd != null)
        {
            subtractOrderLineRemaining(newOrderId, cur);
            return;
        }
        if (oOd != null && nOd == null)
        {
            addBackOrderLineRemaining(persistedOrderId, old);
            return;
        }
        if (oOd != null && nOd != null && !oOd.equals(nOd))
        {
            addBackOrderLineRemaining(persistedOrderId, old);
            subtractOrderLineRemaining(newOrderId, cur);
        }
    }

    private void applyOrderDetailRemainingDelta(Long orderId, Long orderDetailId, BigDecimal deltaSubtractFromRemaining)
    {
        if (orderId == null || orderDetailId == null || deltaSubtractFromRemaining == null)
        {
            return;
        }
        OrderDetail orderDetail = orderDetailMapper.selectOrderDetailById(orderDetailId);
        if (orderDetail == null || orderDetail.getOrderId() == null || !orderId.equals(orderDetail.getOrderId()))
        {
            return;
        }
        int base = orderDetail.getRemainingQuantity() != null ? orderDetail.getRemainingQuantity().intValue() : 0;
        BigDecimal remaining = new BigDecimal(base).subtract(deltaSubtractFromRemaining);
        if (remaining.compareTo(BigDecimal.ZERO) < 0)
        {
            remaining = BigDecimal.ZERO;
        }
        orderDetail.setRemainingQuantity(remaining.intValue());
        orderDetailMapper.updateOrderDetail(orderDetail);
    }

    private void insertOneOrderDeliveryDetailRel(Delivery delivery, DeliveryDetail dd, String createBy, String timeStr)
    {
        if (delivery.getDeliveryId() == null || dd == null || dd.getDetailId() == null)
        {
            return;
        }
        String tenantId = delivery.getTenantId();
        String deliveryIdStr = String.valueOf(delivery.getDeliveryId());
        String deliveryNo = StringUtils.trimToEmpty(delivery.getDeliveryNo());

        if (StringUtils.isNotEmpty(delivery.getZsOrderId()))
        {
            if (StringUtils.isEmpty(dd.getZsOrderDetailId()))
            {
                return;
            }
            ZsTpOrderDetailDeliveryRel r = new ZsTpOrderDetailDeliveryRel();
            r.setId(IdUtils.simpleUuid7());
            r.setOrderDetailId(dd.getZsOrderDetailId());
            r.setOrderId(delivery.getZsOrderId());
            r.setOrderNo(StringUtils.trimToEmpty(delivery.getOrderNo()));
            r.setDeliveryId(deliveryIdStr);
            r.setDeliveryNo(deliveryNo);
            r.setDeliveryDetailId(String.valueOf(dd.getDetailId()));
            r.setCreateTime(timeStr);
            r.setCreateBy(createBy);
            r.setTenantId(tenantId);
            zsTpOrderDetailDeliveryRelMapper.batchInsert(java.util.Collections.singletonList(r));
            return;
        }
        if (delivery.getOrderId() != null && dd.getOrderDetailId() != null)
        {
            ScmOrderDetailDeliveryRel r = new ScmOrderDetailDeliveryRel();
            r.setId(IdUtils.simpleUuid7());
            r.setOrderDetailId(String.valueOf(dd.getOrderDetailId()));
            r.setOrderId(String.valueOf(delivery.getOrderId()));
            r.setOrderNo(StringUtils.trimToEmpty(delivery.getOrderNo()));
            r.setDeliveryId(deliveryIdStr);
            r.setDeliveryNo(deliveryNo);
            r.setDeliveryDetailId(String.valueOf(dd.getDetailId()));
            r.setCreateTime(timeStr);
            r.setCreateBy(createBy);
            r.setTenantId(tenantId);
            scmOrderDetailDeliveryRelMapper.batchInsert(java.util.Collections.singletonList(r));
        }
    }

    /**
     * 批量删除配送单信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeliveryByIds(String ids)
    {
        String[] deliveryIds = Convert.toStrArray(ids);
        for (String deliveryId : deliveryIds)
        {
            Delivery d = deliveryMapper.selectDeliveryById(Long.parseLong(deliveryId));
            assertDeliveryDeletable(d);
        }
        for (String deliveryId : deliveryIds)
        {
            Delivery d = deliveryMapper.selectDeliveryById(Long.parseLong(deliveryId));
            String delBy = StringUtils.trimToEmpty(d != null && StringUtils.isNotEmpty(d.getUpdateBy()) ? d.getUpdateBy()
                : ShiroUtils.getLoginName());
            scmOrderDetailDeliveryRelMapper.deleteByDeliveryId(deliveryId);
            zsTpOrderDetailDeliveryRelMapper.deleteByDeliveryId(deliveryId);
            scmBarcodeSeedService.deleteBarcodesByDeliveryId(Long.parseLong(deliveryId));
            deliveryDetailMapper.deleteDeliveryDetailByDeliveryId(Long.parseLong(deliveryId), delBy);
        }
        return deliveryMapper.deleteDeliveryByIds(deliveryIds);
    }

    /**
     * 删除配送单信息
     *
     * @param deliveryId 配送单ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteDeliveryById(Long deliveryId)
    {
        Delivery d = deliveryMapper.selectDeliveryById(deliveryId);
        assertDeliveryDeletable(d);
        String did = String.valueOf(deliveryId);
        scmOrderDetailDeliveryRelMapper.deleteByDeliveryId(did);
        zsTpOrderDetailDeliveryRelMapper.deleteByDeliveryId(did);
        scmBarcodeSeedService.deleteBarcodesByDeliveryId(deliveryId);
        String delBy = StringUtils.trimToEmpty(d != null && StringUtils.isNotEmpty(d.getUpdateBy()) ? d.getUpdateBy()
            : ShiroUtils.getLoginName());
        deliveryDetailMapper.deleteDeliveryDetailByDeliveryId(deliveryId, delBy);
        return deliveryMapper.deleteDeliveryById(deliveryId);
    }

    private void assertDeliveryDeletable(Delivery d)
    {
        assertDeliveryNotAudited(d, "删除");
    }

    /**
     * 前端按订单行/第三方行校验「申请数量」上限：补全 lineApplyQty（不落库，仅展示与校验用）
     */
    private void enrichDetailLineApplyQty(List<DeliveryDetail> details)
    {
        if (details == null || details.isEmpty())
        {
            return;
        }
        for (DeliveryDetail dd : details)
        {
            if (dd == null || dd.getLineApplyQty() != null)
            {
                continue;
            }
            if (dd.getRefOrderLineQty() != null)
            {
                dd.setLineApplyQty(dd.getRefOrderLineQty());
            }
            else if (dd.getRefZsLineQty() != null)
            {
                dd.setLineApplyQty(dd.getRefZsLineQty());
            }
        }
    }

    /**
     * 已审核（含兼容旧数据：仅单据状态为已审核）则不允许变更。
     */
    private void assertDeliveryNotAudited(Delivery d, String actionLabel)
    {
        if (d == null)
        {
            throw new ServiceException("配送单不存在");
        }
        if ("1".equals(d.getAuditStatus()))
        {
            throw new ServiceException("已审核的配送单不允许" + actionLabel + "，配送单号：" + StringUtils.trimToEmpty(d.getDeliveryNo()));
        }
        if (StringUtils.isEmpty(d.getAuditStatus()) && "1".equals(d.getDeliveryStatus()))
        {
            throw new ServiceException("已审核的配送单不允许" + actionLabel + "，配送单号：" + StringUtils.trimToEmpty(d.getDeliveryNo()));
        }
    }

    /**
     * 根据订单ID查询订单信息（用于引用订单）
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    @Override
    public Order selectOrderForDelivery(Long orderId)
    {
        Order order = orderMapper.selectOrderById(orderId);
        if (order != null)
        {
            assertOrderViewScopeForDelivery(order);
            List<OrderDetail> details = orderDetailMapper.selectOrderDetailListByOrderId(orderId);
            enrichScmOrderDetailsDeliveryQty(details, orderId);
            order.setOrderDetails(details);
        }
        return order;
    }

    /** 与 {@link com.scm.system.service.impl.OrderServiceImpl#assertOrderViewScope} 一致，供引用订单生成配送单 */
    private void assertOrderViewScopeForDelivery(Order order)
    {
        if (order == null)
        {
            return;
        }
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
     * 按配送关联汇总已审核/待审核/已拒绝数量，计算可再申请配送数量（与订单明细页一致）
     */
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

    @Override
    public List<ZsTpOrder> selectZsTpOrderList(ZsTpOrder query)
    {
        normalizeZsTpOrderSearchParams(query);
        applyZsTpOrderQueryDataScope(query);
        return zsTpOrderMapper.selectZsTpOrderList(query);
    }

    @Override
    public ZsTpOrder selectZsTpOrderById(String id)
    {
        return zsTpOrderMapper.selectZsTpOrderById(id);
    }

    @Override
    public List<ZsTpOrder> selectZsTpOrderQueryList(ZsTpOrder query)
    {
        normalizeZsTpOrderSearchParams(query);
        applyZsTpOrderQueryDataScope(query);
        return zsTpOrderMapper.selectZsTpOrderQueryList(query);
    }

    @Override
    public ZsTpOrder selectZsTpOrderHeadForView(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            throw new ServiceException("第三方订单主键不能为空");
        }
        ZsTpOrder head = zsTpOrderMapper.selectZsTpOrderByIdJoined(zsOrderId);
        if (head == null)
        {
            throw new ServiceException("第三方订单不存在或已删除");
        }
        assertZsTpOrderViewScope(head);
        return head;
    }

    @Override
    public void assertZsTpOrderViewScope(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            throw new ServiceException("第三方订单主键不能为空");
        }
        ZsTpOrder head = zsTpOrderMapper.selectZsTpOrderById(zsOrderId);
        if (head == null)
        {
            throw new ServiceException("第三方订单不存在或已删除");
        }
        assertZsTpOrderViewScope(head);
    }

    private void assertZsTpOrderViewScope(ZsTpOrder head)
    {
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx != null)
        {
            if (head.getHospitalId() == null || !hospitalCtx.equals(head.getHospitalId()))
            {
                throw new ServiceException("无权查看其他医院订单");
            }
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx != null)
        {
            if (head.getSupplierId() == null || !supplierCtx.equals(head.getSupplierId()))
            {
                throw new ServiceException("无权查看其他供应商订单");
            }
        }
    }

    private void applyZsTpOrderQueryDataScope(ZsTpOrder query)
    {
        if (query == null)
        {
            return;
        }
        if (query.getParams() == null)
        {
            query.setParams(new HashMap<>());
        }
        Long userId = ShiroUtils.getUserId();
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(userId);
        if (hospitalCtx != null)
        {
            query.setHospitalId(hospitalCtx);
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(userId);
        if (supplierCtx == null)
        {
            return;
        }
        List<Long> roleSupplierIds = resolveUserRoleSupplierIds(userId);
        if (!roleSupplierIds.isEmpty())
        {
            if (!roleSupplierIds.contains(supplierCtx))
            {
                query.getParams().put("scopePairBlock", Boolean.TRUE);
                return;
            }
            query.getParams().put("roleSupplierIds", roleSupplierIds);
        }
        query.setSupplierId(supplierCtx);
        List<Long> forbid = hospitalSupplierPermissionService.listForbidSubmitHospitalIds(supplierCtx);
        if (forbid != null && !forbid.isEmpty())
        {
            query.getParams().put("excludeHospitalIds", forbid);
        }
        scmHospitalSupplierMenuScopeService.applyMenuPairScopeToParams(query.getParams(), userId);
    }

    /**
     * 推送时间：若前端仅传 yyyy-MM-dd，补全开始 00:00:00、结束 23:59:59（已含时分秒则不处理）
     */
    private void normalizeZsTpOrderSearchParams(ZsTpOrder query)
    {
        if (query == null)
        {
            return;
        }
        if (query.getParams() == null)
        {
            query.setParams(new HashMap<>());
        }
        Map<String, Object> p = query.getParams();
        Object pb = p.get("pushBegin");
        if (pb instanceof String)
        {
            String s = StringUtils.trim((String) pb);
            if (StringUtils.isNotEmpty(s) && !s.contains(":") && s.length() <= 10)
            {
                p.put("pushBegin", s + " 00:00:00");
            }
        }
        Object pe = p.get("pushEnd");
        if (pe instanceof String)
        {
            String s = StringUtils.trim((String) pe);
            if (StringUtils.isNotEmpty(s) && !s.contains(":") && s.length() <= 10)
            {
                p.put("pushEnd", s + " 23:59:59");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmZsTpOrder(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            throw new ServiceException("第三方订单主键不能为空");
        }
        Long supplierCtx = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (supplierCtx == null)
        {
            throw new ServiceException("仅供应商账号可确认第三方订单");
        }
        ZsTpOrder head = zsTpOrderMapper.selectZsTpOrderById(zsOrderId);
        if (head == null)
        {
            throw new ServiceException("第三方订单不存在或已删除");
        }
        assertZsTpOrderViewScope(head);
        if (!supplierCtx.equals(head.getSupplierId()))
        {
            throw new ServiceException("无权确认该订单");
        }
        Date now = DateUtils.getNowDate();
        String by = ShiroUtils.getLoginName();
        int n = zsTpOrderMapper.updateZsTpOrderConfirm(zsOrderId, by, now);
        if (n == 0)
        {
            throw new ServiceException("确认失败：订单已确认、已作废或不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidZsTpOrder(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            throw new ServiceException("第三方订单主键不能为空");
        }
        Long hospitalCtx = scmHospitalContextService.resolveHospitalIdForUser(ShiroUtils.getUserId());
        if (hospitalCtx == null)
        {
            throw new ServiceException("仅医院账号可作废第三方订单");
        }
        ZsTpOrder head = zsTpOrderMapper.selectZsTpOrderById(zsOrderId);
        if (head == null)
        {
            throw new ServiceException("第三方订单不存在或已删除");
        }
        assertZsTpOrderViewScope(head);
        if (!hospitalCtx.equals(head.getHospitalId()))
        {
            throw new ServiceException("无权作废该订单");
        }
        Date now = DateUtils.getNowDate();
        String by = ShiroUtils.getLoginName();
        int n = zsTpOrderMapper.updateZsTpOrderVoid(zsOrderId, by, now);
        if (n == 0)
        {
            throw new ServiceException("作废失败：订单已作废或不存在");
        }
    }

    @Override
    public ZsTpOrderForDeliveryVo selectZsTpOrderForDelivery(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            throw new ServiceException("第三方订单主键不能为空");
        }
        ZsTpOrder head = zsTpOrderMapper.selectZsTpOrderById(zsOrderId);
        if (head == null)
        {
            throw new ServiceException("第三方订单不存在或已删除");
        }
        if ("1".equals(StringUtils.trimToNull(head.getVoidStatus())))
        {
            throw new ServiceException("该第三方订单已作废，不可再引用生成配送单");
        }
        List<ZsTpOrderDetail> lines = zsTpOrderMapper.selectZsTpOrderDetailListByOrderId(zsOrderId);
        ZsTpOrderForDeliveryVo vo = new ZsTpOrderForDeliveryVo();
        vo.setZsOrderId(head.getId());
        vo.setOrderNo(StringUtils.trimToEmpty(head.getDh()));
        vo.setWarehouse(StringUtils.trimToEmpty(head.getCk()));
        vo.setOrderAmount(head.getSheetJe() != null
            ? head.getSheetJe().setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO);
        vo.setOrderDate(head.getCreateTime());
        StringBuilder remark = new StringBuilder();
        if (StringUtils.isNotEmpty(head.getKsmc()))
        {
            remark.append("科室:").append(head.getKsmc()).append("；");
        }
        if (StringUtils.isNotEmpty(head.getCk()))
        {
            remark.append("仓库:").append(head.getCk()).append("；");
        }
        if (StringUtils.isNotEmpty(head.getSup()))
        {
            remark.append("供应商:").append(head.getSup()).append("；");
        }
        if (StringUtils.isNotEmpty(head.getBz()))
        {
            remark.append(head.getBz());
        }
        vo.setRemark(remark.toString());
        vo.setZsCustomerId(StringUtils.trimToEmpty(head.getCustomer()));
        vo.setSrcOrderSupplierId(resolveSrcOrderSupplierId(head));
        vo.setScmSupCode(StringUtils.trimToEmpty(head.getScmSupCode()));
        vo.setSrcOrderSupplierName(StringUtils.trimToEmpty(head.getSup()));
        vo.setSrcOrderWarehouseId(StringUtils.trimToEmpty(head.getCkno()));
        vo.setSrcOrderWarehouseName(StringUtils.trimToEmpty(head.getCk()));
        vo.setSrcOrderDeptId(StringUtils.trimToEmpty(head.getKsbh()));
        vo.setSrcOrderDeptName(StringUtils.trimToEmpty(head.getKsmc()));
        vo.setScmSupplierId(parseLongOrNull(head.getScmSupplierId()));
        vo.setHospitalId(parseLongOrNull(head.getScmHospitalId()));
        vo.setSpdSupplierId(StringUtils.trimToEmpty(head.getSupno()));

        Map<String, OrderLineDeliveryQtyVo> zsLineAgg = new HashMap<>();
        if (lines != null && !lines.isEmpty())
        {
            List<OrderLineDeliveryQtyVo> aggRows = orderDeliveryTraceMapper.selectZsOrderLineDeliveryQtyByZsOrderId(zsOrderId);
            for (OrderLineDeliveryQtyVo row : aggRows)
            {
                if (row != null && StringUtils.isNotEmpty(row.getLineKey()))
                {
                    zsLineAgg.put(row.getLineKey(), row);
                }
            }
        }
        List<DeliveryDetail> details = new ArrayList<>();
        if (lines != null)
        {
            for (ZsTpOrderDetail line : lines)
            {
                if (line == null)
                {
                    continue;
                }
                if ("1".equals(StringUtils.trimToNull(line.getDelFlag())))
                {
                    continue;
                }
                DeliveryDetail d = mapZsDetailLine(line, zsLineAgg);
                if (d.getDeliveryQuantity() == null || d.getDeliveryQuantity().compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                details.add(d);
            }
        }
        vo.setDeliveryDetails(details);
        return vo;
    }

    private DeliveryDetail mapZsDetailLine(ZsTpOrderDetail line, Map<String, OrderLineDeliveryQtyVo> zsLineAgg)
    {
        DeliveryDetail d = new DeliveryDetail();
        d.setMaterialId(0L);
        d.setZsOrderDetailId(line.getId());
        d.setOrderDetailId(null);
        d.setMaterialCode(StringUtils.trimToEmpty(line.getCode()));
        d.setMaterialName(StringUtils.trimToEmpty(line.getName()));
        d.setSpecification(StringUtils.trimToEmpty(line.getGg()));
        d.setModel(StringUtils.trimToEmpty(line.getBzl()));
        d.setUnit(StringUtils.trimToEmpty(line.getDw()));
        BigDecimal sl = line.getSl() != null ? line.getSl() : BigDecimal.ZERO;
        d.setRefZsLineQty(sl);
        OrderLineDeliveryQtyVo q = line != null && StringUtils.isNotEmpty(line.getId()) ? zsLineAgg.get(line.getId()) : null;
        BigDecimal a = (q != null && q.getAuditedQty() != null) ? q.getAuditedQty() : BigDecimal.ZERO;
        BigDecimal p = (q != null && q.getPendingQty() != null) ? q.getPendingQty() : BigDecimal.ZERO;
        BigDecimal rj = (q != null && q.getRejectedQty() != null) ? q.getRejectedQty() : BigDecimal.ZERO;
        BigDecimal available = sl.subtract(a).subtract(p).subtract(rj);
        if (available.compareTo(BigDecimal.ZERO) < 0)
        {
            available = BigDecimal.ZERO;
        }
        BigDecimal dj = line.getDj() != null ? line.getDj() : BigDecimal.ZERO;
        BigDecimal je;
        if (available.compareTo(sl) == 0 && line.getJe() != null)
        {
            je = line.getJe().setScale(2, RoundingMode.HALF_UP);
        }
        else
        {
            je = available.multiply(dj).setScale(2, RoundingMode.HALF_UP);
        }
        d.setDeliveryQuantity(available);
        d.setRemainingQuantity(available);
        d.setPrice(dj.setScale(4, RoundingMode.HALF_UP));
        d.setAmount(je);
        d.setManufacturer(StringUtils.trimToEmpty(line.getSccj()));
        d.setRegisterNo(StringUtils.trimToEmpty(line.getZcz()));
        d.setBatchNo("");
        d.setMainBarcode("");
        d.setAuxBarcode("");
        if (line.getDsb() != null && line.getDsb().compareTo(BigDecimal.ZERO) > 0)
        {
            d.setPackCoefficient(line.getDsb());
        }
        d.setLineApplyQty(available);
        return d;
    }

    /**
     * 引用本系统订单时，若前端未带打包系数，则按订单明细行补全。
     */
    private void enrichDeliveryDetailPackCoefficients(Delivery delivery)
    {
        if (delivery == null || delivery.getDeliveryDetails() == null || delivery.getDeliveryDetails().isEmpty())
        {
            return;
        }
        if (StringUtils.isNotEmpty(delivery.getZsOrderId()))
        {
            List<ZsTpOrderDetail> lines = zsTpOrderMapper.selectZsTpOrderDetailListByOrderId(delivery.getZsOrderId());
            Map<String, ZsTpOrderDetail> byId = new HashMap<>();
            if (lines != null)
            {
                for (ZsTpOrderDetail line : lines)
                {
                    if (line != null && StringUtils.isNotEmpty(line.getId()))
                    {
                        byId.put(line.getId(), line);
                    }
                }
            }
            for (DeliveryDetail d : delivery.getDeliveryDetails())
            {
                if (d.getPackCoefficient() != null)
                {
                    continue;
                }
                if (StringUtils.isEmpty(d.getZsOrderDetailId()))
                {
                    continue;
                }
                ZsTpOrderDetail line = byId.get(d.getZsOrderDetailId());
                if (line != null && line.getDsb() != null && line.getDsb().compareTo(BigDecimal.ZERO) > 0)
                {
                    d.setPackCoefficient(line.getDsb());
                }
            }
            return;
        }
        if (delivery.getOrderId() == null)
        {
            return;
        }
        for (DeliveryDetail d : delivery.getDeliveryDetails())
        {
            if (d.getPackCoefficient() != null)
            {
                continue;
            }
            if (d.getOrderDetailId() == null)
            {
                continue;
            }
            OrderDetail od = orderDetailMapper.selectOrderDetailById(d.getOrderDetailId());
            if (od != null && od.getPackCoefficient() != null)
            {
                d.setPackCoefficient(od.getPackCoefficient());
            }
        }
    }

    /**
     * 打包系数为正数时，配送数量须为其整数倍。
     */
    private void validateDeliveryDetailPackQuantities(List<DeliveryDetail> details)
    {
        if (details == null || details.isEmpty())
        {
            return;
        }
        int row = 1;
        for (DeliveryDetail d : details)
        {
            BigDecimal coeff = d.getPackCoefficient();
            if (coeff == null || coeff.compareTo(BigDecimal.ZERO) <= 0)
            {
                row++;
                continue;
            }
            BigDecimal qty = d.getDeliveryQuantity();
            if (qty == null)
            {
                row++;
                continue;
            }
            BigDecimal rem = qty.remainder(coeff);
            if (rem.compareTo(BigDecimal.ZERO) != 0)
            {
                throw new ServiceException(String.format("第%d行：配送数量必须是打包系数(%s)的整数倍，当前数量：%s",
                    row,
                    coeff.stripTrailingZeros().toPlainString(),
                    qty.stripTrailingZeros().toPlainString()));
            }
            row++;
        }
    }

    /**
     * 保存前校验：引用本系统订单或第三方订单时，各订单行上配送数量合计不得超过「订货量 − 已占用（已审核/待审核/已拒绝）」；
     * 修改配送单时排除本单历史占用后再比上限，避免编辑未审核单时误报。
     */
    private void validateDeliveryRefLineQuantities(Delivery delivery, Long excludeDeliveryId)
    {
        if (delivery == null || delivery.getDeliveryDetails() == null || delivery.getDeliveryDetails().isEmpty())
        {
            return;
        }
        int row = 1;
        for (DeliveryDetail dd : delivery.getDeliveryDetails())
        {
            if (dd == null)
            {
                row++;
                continue;
            }
            if (dd.getDeliveryQuantity() != null && dd.getDeliveryQuantity().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException(String.format("第%d行：配送数量不能为负数", row));
            }
            row++;
        }
        if (StringUtils.isNotEmpty(delivery.getZsOrderId()))
        {
            validateZsTpDeliveryLineApplyCaps(delivery, excludeDeliveryId);
            return;
        }
        if (delivery.getOrderId() != null)
        {
            validateScmOrderDeliveryLineApplyCaps(delivery, excludeDeliveryId);
        }
    }

    private static BigDecimal lineRemainingApplyCap(BigDecimal lineOrderQty, OrderLineDeliveryQtyVo q)
    {
        BigDecimal oq = lineOrderQty != null ? lineOrderQty : BigDecimal.ZERO;
        BigDecimal a = (q != null && q.getAuditedQty() != null) ? q.getAuditedQty() : BigDecimal.ZERO;
        BigDecimal p = (q != null && q.getPendingQty() != null) ? q.getPendingQty() : BigDecimal.ZERO;
        BigDecimal rj = (q != null && q.getRejectedQty() != null) ? q.getRejectedQty() : BigDecimal.ZERO;
        BigDecimal und = oq.subtract(a).subtract(p).subtract(rj);
        if (und.compareTo(BigDecimal.ZERO) < 0)
        {
            return BigDecimal.ZERO;
        }
        return und;
    }

    private static Map<String, OrderLineDeliveryQtyVo> toLineQtyMap(List<OrderLineDeliveryQtyVo> agg)
    {
        Map<String, OrderLineDeliveryQtyVo> map = new HashMap<>();
        if (agg == null)
        {
            return map;
        }
        for (OrderLineDeliveryQtyVo row : agg)
        {
            if (row != null && StringUtils.isNotEmpty(row.getLineKey()))
            {
                map.put(row.getLineKey(), row);
            }
        }
        return map;
    }

    private void validateScmOrderDeliveryLineApplyCaps(Delivery delivery, Long excludeDeliveryId)
    {
        Long orderId = delivery.getOrderId();
        if (orderId == null)
        {
            return;
        }
        Map<Long, BigDecimal> sumByOrderDetail = new HashMap<>();
        for (DeliveryDetail dd : delivery.getDeliveryDetails())
        {
            if (dd == null || dd.getOrderDetailId() == null)
            {
                continue;
            }
            BigDecimal q = dd.getDeliveryQuantity() != null ? dd.getDeliveryQuantity() : BigDecimal.ZERO;
            sumByOrderDetail.merge(dd.getOrderDetailId(), q, BigDecimal::add);
        }
        if (sumByOrderDetail.isEmpty())
        {
            return;
        }
        List<OrderLineDeliveryQtyVo> agg = excludeDeliveryId == null
            ? orderDeliveryTraceMapper.selectScmOrderLineDeliveryQtyByOrderId(orderId)
            : orderDeliveryTraceMapper.selectScmOrderLineDeliveryQtyByOrderIdExcludeDelivery(orderId, excludeDeliveryId);
        Map<String, OrderLineDeliveryQtyVo> aggMap = toLineQtyMap(agg);
        for (Map.Entry<Long, BigDecimal> e : sumByOrderDetail.entrySet())
        {
            Long odId = e.getKey();
            BigDecimal sumQ = e.getValue();
            OrderDetail od = orderDetailMapper.selectOrderDetailById(odId);
            if (od == null)
            {
                throw new ServiceException("订单明细不存在，订单明细ID：" + odId);
            }
            if (od.getOrderId() == null || !od.getOrderId().equals(orderId))
            {
                throw new ServiceException("配送明细与订单不匹配（物料：" + StringUtils.trimToEmpty(od.getMaterialName()) + "）");
            }
            BigDecimal oq = od.getOrderQuantity() == null ? BigDecimal.ZERO
                : BigDecimal.valueOf(od.getOrderQuantity().longValue());
            OrderLineDeliveryQtyVo q = aggMap.get(String.valueOf(odId));
            BigDecimal cap = lineRemainingApplyCap(oq, q);
            if (sumQ.compareTo(cap) > 0)
            {
                throw new ServiceException(String.format("订单可配送数量超限【%s %s】：本单合计 %s，当前最多可配送 %s",
                    StringUtils.trimToEmpty(od.getMaterialCode()),
                    StringUtils.trimToEmpty(od.getMaterialName()),
                    sumQ.stripTrailingZeros().toPlainString(),
                    cap.stripTrailingZeros().toPlainString()));
            }
        }
    }

    private void validateZsTpDeliveryLineApplyCaps(Delivery delivery, Long excludeDeliveryId)
    {
        String zsOrderId = delivery.getZsOrderId();
        if (StringUtils.isEmpty(zsOrderId))
        {
            return;
        }
        Map<String, BigDecimal> sumByZsLine = new HashMap<>();
        for (DeliveryDetail dd : delivery.getDeliveryDetails())
        {
            if (dd == null || StringUtils.isEmpty(dd.getZsOrderDetailId()))
            {
                continue;
            }
            BigDecimal q = dd.getDeliveryQuantity() != null ? dd.getDeliveryQuantity() : BigDecimal.ZERO;
            sumByZsLine.merge(dd.getZsOrderDetailId(), q, BigDecimal::add);
        }
        if (sumByZsLine.isEmpty())
        {
            return;
        }
        List<ZsTpOrderDetail> zsLines = zsTpOrderMapper.selectZsTpOrderDetailListByOrderId(zsOrderId);
        Map<String, ZsTpOrderDetail> lineById = new HashMap<>();
        if (zsLines != null)
        {
            for (ZsTpOrderDetail z : zsLines)
            {
                if (z != null && StringUtils.isNotEmpty(z.getId()))
                {
                    lineById.put(z.getId(), z);
                }
            }
        }
        List<OrderLineDeliveryQtyVo> agg = excludeDeliveryId == null
            ? orderDeliveryTraceMapper.selectZsOrderLineDeliveryQtyByZsOrderId(zsOrderId)
            : orderDeliveryTraceMapper.selectZsOrderLineDeliveryQtyByZsOrderIdExcludeDelivery(zsOrderId, excludeDeliveryId);
        Map<String, OrderLineDeliveryQtyVo> aggMap = toLineQtyMap(agg);
        for (Map.Entry<String, BigDecimal> e : sumByZsLine.entrySet())
        {
            String lineId = e.getKey();
            BigDecimal sumQ = e.getValue();
            ZsTpOrderDetail zline = lineById.get(lineId);
            if (zline == null)
            {
                throw new ServiceException("第三方订单明细不存在，行ID：" + lineId);
            }
            if (StringUtils.isNotEmpty(zline.getOrderId()) && !zsOrderId.equals(zline.getOrderId()))
            {
                throw new ServiceException("配送明细与第三方订单不匹配（物料：" + StringUtils.trimToEmpty(zline.getName()) + "）");
            }
            BigDecimal oq = zline.getSl() != null ? zline.getSl() : BigDecimal.ZERO;
            OrderLineDeliveryQtyVo q = aggMap.get(lineId);
            BigDecimal cap = lineRemainingApplyCap(oq, q);
            if (sumQ.compareTo(cap) > 0)
            {
                throw new ServiceException(String.format("第三方订单可配送数量超限【%s %s】：本单合计 %s，当前最多可配送 %s",
                    StringUtils.trimToEmpty(zline.getCode()),
                    StringUtils.trimToEmpty(zline.getName()),
                    sumQ.stripTrailingZeros().toPlainString(),
                    cap.stripTrailingZeros().toPlainString()));
            }
        }
    }

    /**
     * 保存前从关联的第三方订单或本系统订单补全订单侧快照字段（字符串），便于客户端按配送单入库引用。
     */
    private void enrichDeliverySnapshot(Delivery d)
    {
        if (d == null)
        {
            return;
        }
        if (StringUtils.isEmpty(d.getZsOrderId()))
        {
            d.setZsJsfs(null);
        }
        if (StringUtils.isNotEmpty(d.getZsOrderId()))
        {
            ZsTpOrder z = zsTpOrderMapper.selectZsTpOrderById(d.getZsOrderId());
            if (z != null)
            {
                if ("1".equals(StringUtils.trimToNull(z.getVoidStatus())))
                {
                    throw new ServiceException("该第三方订单已作废，不可生成配送单");
                }
                if (d.getHospitalId() == null)
                {
                    d.setHospitalId(parseLongOrNull(z.getScmHospitalId()));
                }
                if (StringUtils.isEmpty(d.getZsCustomerId()))
                {
                    d.setZsCustomerId(StringUtils.trimToEmpty(z.getCustomer()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderSupplierId()))
                {
                    d.setSrcOrderSupplierId(resolveSrcOrderSupplierId(z));
                }
                if (StringUtils.isEmpty(d.getSrcOrderSupplierName()))
                {
                    d.setSrcOrderSupplierName(StringUtils.trimToEmpty(z.getSup()));
                }
                if (StringUtils.isEmpty(d.getSpdSupplierId()) && StringUtils.isNotEmpty(z.getSupno()))
                {
                    d.setSpdSupplierId(StringUtils.trimToEmpty(z.getSupno()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderWarehouseId()))
                {
                    d.setSrcOrderWarehouseId(StringUtils.trimToEmpty(z.getCkno()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderWarehouseName()))
                {
                    d.setSrcOrderWarehouseName(StringUtils.trimToEmpty(z.getCk()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderDeptId()))
                {
                    d.setSrcOrderDeptId(StringUtils.trimToEmpty(z.getKsbh()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderDeptName()))
                {
                    d.setSrcOrderDeptName(StringUtils.trimToEmpty(z.getKsmc()));
                }
                if (d.getOrderDate() == null && z.getCreateTime() != null)
                {
                    d.setOrderDate(z.getCreateTime());
                }
                if (StringUtils.isEmpty(d.getWarehouse()) && StringUtils.isNotEmpty(z.getCk()))
                {
                    d.setWarehouse(StringUtils.trimToEmpty(z.getCk()));
                }
                if (StringUtils.isEmpty(d.getSpdTenantId()) && StringUtils.isNotEmpty(z.getCustomer()))
                {
                    // 第三方订单场景下，customer 可作为租户侧标识快照保留
                    d.setSpdTenantId(StringUtils.trimToEmpty(z.getCustomer()));
                }
                if (StringUtils.isEmpty(d.getSpdRefNo()) && StringUtils.isNotEmpty(z.getDh()))
                {
                    d.setSpdRefNo(StringUtils.trimToEmpty(z.getDh()));
                }
                d.setZsJsfs(StringUtils.trimToEmpty(z.getJsfs()));
                fillSupplierIdFromPlatformOrderIfBlank(d, parseLongOrNull(z.getScmSupplierId()));
            }
        }
        else if (d.getOrderId() != null)
        {
            d.setZsJsfs(null);
            Order o = orderMapper.selectOrderById(d.getOrderId());
            if (o != null)
            {
                if (StringUtils.isEmpty(d.getZsCustomerId()))
                {
                    d.setZsCustomerId("");
                }
                if (StringUtils.isEmpty(d.getSrcOrderSupplierId()) && o.getSupplierId() != null)
                {
                    d.setSrcOrderSupplierId(String.valueOf(o.getSupplierId()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderSupplierName()))
                {
                    String name = StringUtils.isNotEmpty(o.getOrderSupplierName()) ? o.getOrderSupplierName()
                        : o.getSupplierName();
                    d.setSrcOrderSupplierName(StringUtils.trimToEmpty(name));
                }
                if (StringUtils.isEmpty(d.getSrcOrderWarehouseId()) && o.getWarehouseId() != null)
                {
                    d.setSrcOrderWarehouseId(String.valueOf(o.getWarehouseId()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderWarehouseName()))
                {
                    d.setSrcOrderWarehouseName(StringUtils.trimToEmpty(o.getWarehouse()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderDeptId()) && o.getOrderDeptId() != null)
                {
                    d.setSrcOrderDeptId(String.valueOf(o.getOrderDeptId()));
                }
                if (StringUtils.isEmpty(d.getSrcOrderDeptName()))
                {
                    String dn = StringUtils.isNotEmpty(o.getOrderDeptName()) ? o.getOrderDeptName()
                        : o.getDepartment();
                    d.setSrcOrderDeptName(StringUtils.trimToEmpty(dn));
                }
                if (d.getOrderDate() == null && o.getOrderDate() != null)
                {
                    d.setOrderDate(o.getOrderDate());
                }
                if (StringUtils.isEmpty(d.getWarehouse()) && StringUtils.isNotEmpty(o.getWarehouse()))
                {
                    d.setWarehouse(StringUtils.trimToEmpty(o.getWarehouse()));
                }
                if (StringUtils.isEmpty(d.getSpdSupplierId()) && StringUtils.isNotEmpty(o.getSpdSupplierId()))
                {
                    d.setSpdSupplierId(StringUtils.trimToEmpty(o.getSpdSupplierId()));
                }
                if (StringUtils.isEmpty(d.getSpdTenantId()) && StringUtils.isNotEmpty(o.getTenantId()))
                {
                    d.setSpdTenantId(StringUtils.trimToEmpty(o.getTenantId()));
                }
                if (StringUtils.isEmpty(d.getSpdRefNo()))
                {
                    String refNo = StringUtils.isNotEmpty(d.getOrderNo()) ? d.getOrderNo() : o.getOrderNo();
                    if (StringUtils.isNotEmpty(refNo))
                    {
                        d.setSpdRefNo(StringUtils.trimToEmpty(refNo));
                    }
                }
                fillSupplierIdFromPlatformOrderIfBlank(d, o.getSupplierId());
            }
        }
        fillRefOrderSource(d);
    }

    /**
     * 按 zs_order_id / order_id 写入 ref_order_source；第三方优先于第一方。
     */
    private static void fillRefOrderSource(Delivery d)
    {
        if (d == null)
        {
            return;
        }
        if (StringUtils.isNotEmpty(d.getZsOrderId()))
        {
            d.setRefOrderSource(DeliveryRefOrderSource.ZS);
        }
        else if (d.getOrderId() != null)
        {
            d.setRefOrderSource(DeliveryRefOrderSource.SCM);
        }
        else
        {
            d.setRefOrderSource(null);
        }
    }

    /** 从订单侧平台供应商主键（bigint）解析，非法则忽略 */
    private static Long parseLongOrNull(String raw)
    {
        if (StringUtils.isEmpty(raw))
        {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty())
        {
            return null;
        }
        try
        {
            return Long.parseLong(t);
        }
        catch (NumberFormatException ex)
        {
            return null;
        }
    }

    /**
     * 第三方订单场景下，src_order_supplier_id 优先写平台供应商主键（scm_supplier_id），
     * 保持与本系统订单场景（supplier_id）语义一致；若映射缺失则兼容回退 supno。
     */
    private static String resolveSrcOrderSupplierId(ZsTpOrder order)
    {
        if (order == null)
        {
            return "";
        }
        Long scmSupplierId = parseLongOrNull(order.getScmSupplierId());
        if (scmSupplierId != null)
        {
            return String.valueOf(scmSupplierId);
        }
        return StringUtils.trimToEmpty(order.getSupno());
    }

    /**
     * 供应商端列表/审核按 {@code scm_delivery.supplier_id} 过滤；引用订单生成配送单时若未带主键则按订单平台供应商补全。
     */
    private static void fillSupplierIdFromPlatformOrderIfBlank(Delivery d, Long platformSupplierId)
    {
        if (d == null || platformSupplierId == null)
        {
            return;
        }
        if (d.getSupplierId() == null)
        {
            d.setSupplierId(platformSupplierId);
        }
    }

    /**
     * 查询配送明细列表
     *
     * @param deliveryId 配送单ID
     * @return 明细集合
     */
    @Override
    public List<DeliveryDetail> selectDeliveryDetailListByDeliveryId(Long deliveryId)
    {
        return deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(deliveryId);
    }

    @Override
    public List<DeliveryDetail> selectDeliveryDetailList(DeliveryDetail deliveryDetail)
    {
        return deliveryDetailMapper.selectDeliveryDetailList(deliveryDetail);
    }

    /**
     * 审核配送单
     *
     * @param deliveryId 配送单ID
     * @return 结果
     */
    @Override
    public int auditDelivery(Long deliveryId, String auditBy)
    {
        Delivery delivery = deliveryMapper.selectDeliveryById(deliveryId);
        if (delivery == null)
        {
            return 0;
        }
        if ("1".equals(delivery.getAuditStatus()))
        {
            throw new ServiceException("配送单已审核，请勿重复审核");
        }
        delivery.setAuditStatus("1");
        delivery.setAuditBy(StringUtils.trimToEmpty(auditBy));
        delivery.setAuditTime(DateUtils.getNowDate());
        delivery.setDeliveryStatus("1"); // 单据状态：已审核
        delivery.setUpdateTime(DateUtils.getNowDate());
        return deliveryMapper.updateDelivery(delivery);
    }

    /**
     * 生成唯一的配送单号
     *
     * @return 配送单号
     */
    private String generateDeliveryNo()
    {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do
        {
            // 使用时间戳+随机数生成编号
            code = "DEL" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            if (code.length() > 50)
            {
                code = code.substring(0, 50);
            }
            attempt++;
        }
        while (deliveryMapper.selectDeliveryByDeliveryNo(code) != null && attempt < maxAttempts);

        if (attempt >= maxAttempts)
        {
            // 如果10次尝试都失败，使用UUID
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            code = "DEL" + uuid.substring(0, Math.min(20, uuid.length()));
        }

        return code;
    }

    /**
     * 更新订单明细的剩余待配送数
     *
     * @param delivery 配送单信息
     */
    private void updateOrderRemainingQuantity(Delivery delivery)
    {
        if (delivery.getOrderId() == null || delivery.getDeliveryDetails() == null)
        {
            return;
        }

        for (DeliveryDetail deliveryDetail : delivery.getDeliveryDetails())
        {
            if (deliveryDetail.getOrderDetailId() != null)
            {
                OrderDetail orderDetail = orderDetailMapper.selectOrderDetailById(deliveryDetail.getOrderDetailId());
                if (orderDetail != null && orderDetail.getRemainingQuantity() != null)
                {
                    BigDecimal remaining = new BigDecimal(orderDetail.getRemainingQuantity()).subtract(deliveryDetail.getDeliveryQuantity());
                    if (remaining.compareTo(BigDecimal.ZERO) < 0)
                    {
                        remaining = BigDecimal.ZERO;
                    }
                    orderDetail.setRemainingQuantity(remaining.intValue());
                    orderDetailMapper.updateOrderDetail(orderDetail);
                }
            }
        }
    }

    /**
     * 写入订单明细与配送单明细关联（支持同一订单多次配送）
     */
    private void insertOrderDeliveryDetailRelations(Delivery delivery)
    {
        if (delivery.getDeliveryId() == null)
        {
            return;
        }
        List<DeliveryDetail> saved = deliveryDetailMapper.selectDeliveryDetailListByDeliveryId(delivery.getDeliveryId());
        if (saved == null || saved.isEmpty())
        {
            return;
        }
        String timeStr = DateUtils.getTime();
        String createBy = StringUtils.trimToEmpty(delivery.getCreateBy());
        String tenantId = delivery.getTenantId();
        String deliveryIdStr = String.valueOf(delivery.getDeliveryId());
        String deliveryNo = StringUtils.trimToEmpty(delivery.getDeliveryNo());

        if (StringUtils.isNotEmpty(delivery.getZsOrderId()))
        {
            List<ZsTpOrderDetailDeliveryRel> rels = new ArrayList<>();
            String zsOrderId = delivery.getZsOrderId();
            String orderNo = StringUtils.trimToEmpty(delivery.getOrderNo());
            for (DeliveryDetail dd : saved)
            {
                if (StringUtils.isEmpty(dd.getZsOrderDetailId()) || dd.getDetailId() == null)
                {
                    continue;
                }
                ZsTpOrderDetailDeliveryRel r = new ZsTpOrderDetailDeliveryRel();
                r.setId(IdUtils.simpleUuid7());
                r.setOrderDetailId(dd.getZsOrderDetailId());
                r.setOrderId(zsOrderId);
                r.setOrderNo(orderNo);
                r.setDeliveryId(deliveryIdStr);
                r.setDeliveryNo(deliveryNo);
                r.setDeliveryDetailId(String.valueOf(dd.getDetailId()));
                r.setCreateTime(timeStr);
                r.setCreateBy(createBy);
                r.setTenantId(tenantId);
                rels.add(r);
            }
            if (!rels.isEmpty())
            {
                zsTpOrderDetailDeliveryRelMapper.batchInsert(rels);
            }
            return;
        }

        if (delivery.getOrderId() != null)
        {
            List<ScmOrderDetailDeliveryRel> rels = new ArrayList<>();
            String oid = String.valueOf(delivery.getOrderId());
            String orderNo = StringUtils.trimToEmpty(delivery.getOrderNo());
            for (DeliveryDetail dd : saved)
            {
                if (dd.getOrderDetailId() == null || dd.getDetailId() == null)
                {
                    continue;
                }
                ScmOrderDetailDeliveryRel r = new ScmOrderDetailDeliveryRel();
                r.setId(IdUtils.simpleUuid7());
                r.setOrderDetailId(String.valueOf(dd.getOrderDetailId()));
                r.setOrderId(oid);
                r.setOrderNo(orderNo);
                r.setDeliveryId(deliveryIdStr);
                r.setDeliveryNo(deliveryNo);
                r.setDeliveryDetailId(String.valueOf(dd.getDetailId()));
                r.setCreateTime(timeStr);
                r.setCreateBy(createBy);
                r.setTenantId(tenantId);
                rels.add(r);
            }
            if (!rels.isEmpty())
            {
                scmOrderDetailDeliveryRelMapper.batchInsert(rels);
            }
        }
    }

    @Override
    public List<ZsTpOrderDetail> selectZsTpOrderDetailListForView(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            return new ArrayList<>();
        }
        List<ZsTpOrderDetail> list = zsTpOrderMapper.selectZsTpOrderDetailListByOrderId(zsOrderId);
        enrichZsOrderDetailsDeliveryQty(list, zsOrderId);
        return list;
    }

    private void enrichZsOrderDetailsDeliveryQty(List<ZsTpOrderDetail> list, String zsOrderId)
    {
        if (list == null || list.isEmpty() || StringUtils.isEmpty(zsOrderId))
        {
            return;
        }
        List<OrderLineDeliveryQtyVo> agg = orderDeliveryTraceMapper.selectZsOrderLineDeliveryQtyByZsOrderId(zsOrderId);
        Map<String, OrderLineDeliveryQtyVo> map = new HashMap<>();
        for (OrderLineDeliveryQtyVo row : agg)
        {
            if (row != null && StringUtils.isNotEmpty(row.getLineKey()))
            {
                map.put(row.getLineKey(), row);
            }
        }
        for (ZsTpOrderDetail od : list)
        {
            String key = od.getId();
            OrderLineDeliveryQtyVo q = key == null ? null : map.get(key);
            BigDecimal oq = od.getSl() != null ? od.getSl() : BigDecimal.ZERO;
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

    @Override
    public List<Delivery> selectDeliveriesByOrderId(Long orderId)
    {
        if (orderId == null)
        {
            return new ArrayList<>();
        }
        return orderDeliveryTraceMapper.selectDeliveriesByOrderId(orderId);
    }

    @Override
    public List<Delivery> selectDeliveriesByZsOrderId(String zsOrderId)
    {
        if (StringUtils.isEmpty(zsOrderId))
        {
            return new ArrayList<>();
        }
        return orderDeliveryTraceMapper.selectDeliveriesByZsOrderId(zsOrderId);
    }

    @Override
    public List<OrderDetailDeliveryTraceVo> selectTracesByScmOrderDetailId(Long orderDetailId)
    {
        if (orderDetailId == null)
        {
            return new ArrayList<>();
        }
        return orderDeliveryTraceMapper.selectTracesByScmOrderDetailId(orderDetailId);
    }

    @Override
    public List<OrderDetailDeliveryTraceVo> selectTracesByZsOrderDetailId(String zsOrderDetailId)
    {
        if (StringUtils.isEmpty(zsOrderDetailId))
        {
            return new ArrayList<>();
        }
        return orderDeliveryTraceMapper.selectTracesByZsOrderDetailId(zsOrderDetailId);
    }
}

