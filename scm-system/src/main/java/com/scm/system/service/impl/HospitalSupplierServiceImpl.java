package com.scm.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.PageUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.common.exception.ServiceException;
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.domain.HospitalSupplierChangeLog;
import com.scm.system.domain.ScmHospitalSupplierApply;
import com.scm.system.domain.ScmHospitalSupplierApplyLog;
import com.scm.system.domain.ScmHospitalSupplierModifyApply;
import com.scm.system.domain.ScmNoticeReceiver;
import com.scm.system.domain.SysNotice;
import com.scm.system.domain.ScmSupplierCertApplyBundle;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierCertificate;
import com.scm.system.mapper.HospitalSupplierChangeLogMapper;
import com.scm.system.mapper.HospitalSupplierMapper;
import com.scm.system.mapper.ScmHospitalSupplierApplyLogMapper;
import com.scm.system.mapper.ScmHospitalSupplierApplyMapper;
import com.scm.system.mapper.ScmHospitalSupplierModifyApplyMapper;
import com.scm.system.mapper.ScmNoticeReceiverMapper;
import com.scm.system.mapper.ScmSupplierCertApplyBundleMapper;
import com.scm.system.mapper.SupplierCertificateMapper;
import com.scm.system.service.IHospitalSupplierService;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IScmHospitalSupplierPermissionService;
import com.scm.system.service.IScmScopeBootstrapService;
import com.scm.system.service.IScmSupplierContextService;
import com.scm.system.service.ISysNoticeService;
import com.scm.system.service.ISupplierService;
import com.scm.system.service.ISupplierUserService;

/**
 * 医院供应商关联 服务层实现
 * 
 * @author scm
 */
@Service
public class HospitalSupplierServiceImpl implements IHospitalSupplierService
{
    @Autowired
    private HospitalSupplierMapper hospitalSupplierMapper;

    @Autowired
    private IScmHospitalSupplierPermissionService hospitalSupplierPermissionService;

    @Autowired
    private IScmSupplierContextService scmSupplierContextService;

    @Autowired
    private HospitalSupplierChangeLogMapper hospitalSupplierChangeLogMapper;

    @Autowired
    private IHospitalService hospitalService;

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private ISysNoticeService noticeService;

    @Autowired
    private ScmHospitalSupplierApplyMapper hospitalSupplierApplyMapper;

    @Autowired
    private ScmHospitalSupplierModifyApplyMapper hospitalSupplierModifyApplyMapper;

    @Autowired
    private ScmHospitalSupplierApplyLogMapper hospitalSupplierApplyLogMapper;

    @Autowired
    private ScmNoticeReceiverMapper noticeReceiverMapper;

    @Autowired
    private ISupplierUserService supplierUserService;

    @Autowired
    private SupplierCertificateMapper supplierCertificateMapper;

    @Autowired
    private ScmSupplierCertApplyBundleMapper scmSupplierCertApplyBundleMapper;

    @Autowired
    private IScmScopeBootstrapService scmScopeBootstrapService;

    /**
     * 查询医院供应商关联信息
     * 
     * @param relationId 关联ID
     * @return 关联信息
     */
    @Override
    public HospitalSupplier selectHospitalSupplierById(Long relationId)
    {
        return hospitalSupplierMapper.selectHospitalSupplierById(relationId);
    }

    /**
     * 查询医院供应商关联列表
     * 
     * @param hospitalSupplier 关联信息
     * @return 关联集合
     */
    @Override
    public List<HospitalSupplier> selectHospitalSupplierList(HospitalSupplier hospitalSupplier)
    {
        return hospitalSupplierMapper.selectHospitalSupplierList(hospitalSupplier);
    }

    /**
     * 根据供应商ID查询关联的医院列表
     * 
     * @param supplierId 供应商ID
     * @return 关联集合
     */
    @Override
    public List<HospitalSupplier> selectHospitalSupplierBySupplierId(Long supplierId)
    {
        return hospitalSupplierMapper.selectHospitalSupplierBySupplierId(supplierId);
    }

    @Override
    public List<HospitalSupplier> selectSupplierLinkedHospitalsForProduct(Long supplierId)
    {
        /*
         * 产品证件等接口在 startPage() 之后会校验医院关联并调用本方法；若不抑制分页，
         * PageHelper 会把列表的 order by expire_date 套在 selectHospitalSupplierList 上导致语法错误。
         */
        return PageUtils.callWithoutPaging(() -> {
            HospitalSupplier q = new HospitalSupplier();
            q.setSupplierId(supplierId);
            q.setStatus("0");
            q.setAuditStatus("1");
            q.setDisableStatus("0");
            return hospitalSupplierMapper.selectHospitalSupplierList(q);
        });
    }

    /**
     * 新增医院供应商关联
     * 
     * @param hospitalSupplier 关联信息
     * @return 结果
     */
    @Override
    public int insertHospitalSupplier(HospitalSupplier hospitalSupplier)
    {
        validateSupplyDateRange(hospitalSupplier.getSupplyStartDate(), hospitalSupplier.getSupplyEndDate());
        Long ctxSupplier = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
        if (ctxSupplier != null && hospitalSupplier.getSupplierId() != null
            && ctxSupplier.equals(hospitalSupplier.getSupplierId()))
        {
            hospitalSupplierPermissionService.assertBindAllowed(hospitalSupplier.getHospitalId(),
                hospitalSupplier.getSupplierId());
        }
        if (StringUtils.isEmpty(hospitalSupplier.getStatus()))
        {
            hospitalSupplier.setStatus("0"); // 默认正常
        }
        if (StringUtils.isEmpty(hospitalSupplier.getBindStatus()))
        {
            hospitalSupplier.setBindStatus("1"); // 默认已绑定
        }
        if (StringUtils.isEmpty(hospitalSupplier.getAuditStatus()))
        {
            hospitalSupplier.setAuditStatus("0"); // 默认待审核
        }
        if (StringUtils.isEmpty(hospitalSupplier.getDisableStatus()))
        {
            hospitalSupplier.setDisableStatus("0"); // 默认启用
        }
        if (hospitalSupplier.getBindTime() == null)
        {
            hospitalSupplier.setBindTime(DateUtils.getNowDate());
        }
        int rows = hospitalSupplierMapper.insertHospitalSupplier(hospitalSupplier);
        if (rows > 0)
        {
            insertChangeLog(hospitalSupplier, "CREATE", hospitalSupplier.getCreateBy(), null, hospitalSupplier);
            if ("1".equals(StringUtils.trimToEmpty(hospitalSupplier.getAuditStatus()))
                && hospitalSupplier.getHospitalId() != null && hospitalSupplier.getSupplierId() != null)
            {
                scmScopeBootstrapService.applyDefaultHospitalGrantedSupplierMenus(hospitalSupplier.getHospitalId(),
                    hospitalSupplier.getSupplierId(), hospitalSupplier.getCreateBy());
            }
        }
        return rows;
    }

    /**
     * 修改医院供应商关联
     * 
     * @param hospitalSupplier 关联信息
     * @return 结果
     */
    @Override
    public int updateHospitalSupplier(HospitalSupplier hospitalSupplier)
    {
        validateSupplyDateRange(hospitalSupplier.getSupplyStartDate(), hospitalSupplier.getSupplyEndDate());
        HospitalSupplier before = hospitalSupplierMapper.selectHospitalSupplierById(hospitalSupplier.getRelationId());
        int rows = hospitalSupplierMapper.updateHospitalSupplier(hospitalSupplier);
        if (rows > 0)
        {
            HospitalSupplier after = hospitalSupplierMapper.selectHospitalSupplierById(hospitalSupplier.getRelationId());
            insertChangeLog(after, "UPDATE", hospitalSupplier.getUpdateBy(), before, after);
            if (after != null && after.getHospitalId() != null && after.getSupplierId() != null
                && "1".equals(StringUtils.trimToEmpty(after.getAuditStatus())))
            {
                String beforeAudit = before == null ? "" : StringUtils.trimToEmpty(before.getAuditStatus());
                if (!"1".equals(beforeAudit))
                {
                    scmScopeBootstrapService.applyDefaultHospitalGrantedSupplierMenus(after.getHospitalId(),
                        after.getSupplierId(), hospitalSupplier.getUpdateBy());
                }
            }
        }
        return rows;
    }

    /**
     * 批量删除医院供应商关联
     * 
     * @param relationIds 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteHospitalSupplierByIds(String relationIds)
    {
        String[] ids = Convert.toStrArray(relationIds);
        java.util.List<HospitalSupplier> willDelete = new java.util.ArrayList<>();
        for (String id : ids)
        {
            Long relationId = Long.valueOf(id);
            HospitalSupplier relation = hospitalSupplierMapper.selectHospitalSupplierById(relationId);
            if (relation == null)
            {
                continue;
            }
            assertRelationDeletable(relation);
            willDelete.add(relation);
        }
        int rows = hospitalSupplierMapper.deleteHospitalSupplierByIds(ids);
        for (HospitalSupplier relation : willDelete)
        {
            insertChangeLog(relation, "DELETE", currentOperBy(), null, relation);
        }
        return rows;
    }

    /**
     * 根据供应商ID删除所有关联
     * 
     * @param supplierId 供应商ID
     * @return 结果
     */
    @Override
    public int deleteHospitalSupplierBySupplierId(Long supplierId)
    {
        HospitalSupplier q = new HospitalSupplier();
        q.setSupplierId(supplierId);
        List<HospitalSupplier> relations = hospitalSupplierMapper.selectHospitalSupplierList(q);
        if (relations != null)
        {
            for (HospitalSupplier relation : relations)
            {
                assertRelationDeletable(relation);
            }
        }
        int rows = hospitalSupplierMapper.deleteHospitalSupplierBySupplierId(supplierId);
        if (relations != null)
        {
            for (HospitalSupplier relation : relations)
            {
                insertChangeLog(relation, "DELETE", currentOperBy(), null, relation);
            }
        }
        return rows;
    }

    /**
     * 保存供应商的配送公司（先删除旧的，再插入新的）
     * 
     * @param supplierId 供应商ID
     * @param hospitalIds 医院ID数组
     * @param createBy 创建人
     * @return 结果
     */
    @Override
    @Transactional
    public int saveSupplierHospitals(Long supplierId, String[] hospitalIds, String createBy)
    {
        HospitalSupplier q = new HospitalSupplier();
        q.setSupplierId(supplierId);
        List<HospitalSupplier> existing = hospitalSupplierMapper.selectHospitalSupplierList(q);
        java.util.Set<Long> targetHospitalIds = new java.util.HashSet<>();
        if (hospitalIds != null)
        {
            for (String hospitalIdStr : hospitalIds)
            {
                if (StringUtils.isNotEmpty(hospitalIdStr))
                {
                    try
                    {
                        targetHospitalIds.add(Long.parseLong(hospitalIdStr));
                    }
                    catch (NumberFormatException e)
                    {
                        // ignore invalid id
                    }
                }
            }
        }

        // 删除目标外关联（受保护校验约束）
        if (existing != null)
        {
            for (HospitalSupplier rel : existing)
            {
                if (rel.getHospitalId() != null && !targetHospitalIds.contains(rel.getHospitalId()))
                {
                    assertRelationDeletable(rel);
                    hospitalSupplierMapper.deleteHospitalSupplierById(rel.getRelationId());
                    insertChangeLog(rel, "DELETE", createBy, null, rel);
                }
            }
        }

        // 补充新增关联
        if (!targetHospitalIds.isEmpty())
        {
            for (Long hospitalId : targetHospitalIds)
            {
                boolean exists = false;
                if (existing != null)
                {
                    for (HospitalSupplier rel : existing)
                    {
                        if (rel.getHospitalId() != null && rel.getHospitalId().equals(hospitalId))
                        {
                            exists = true;
                            break;
                        }
                    }
                }
                if (exists)
                {
                    continue;
                }
                Long ctxSupplier = scmSupplierContextService.resolveSupplierIdForUser(ShiroUtils.getUserId());
                if (ctxSupplier != null && ctxSupplier.equals(supplierId))
                {
                    hospitalSupplierPermissionService.assertBindAllowed(hospitalId, supplierId);
                }
                HospitalSupplier hospitalSupplier = new HospitalSupplier();
                hospitalSupplier.setSupplierId(supplierId);
                hospitalSupplier.setHospitalId(hospitalId);
                hospitalSupplier.setBindStatus("1");
                hospitalSupplier.setBindTime(DateUtils.getNowDate());
                hospitalSupplier.setBindBy(createBy);
                hospitalSupplier.setStatus("0");
                hospitalSupplier.setDisableStatus("0");
                hospitalSupplier.setAuditStatus("0");
                hospitalSupplier.setCreateBy(createBy);
                hospitalSupplierMapper.insertHospitalSupplier(hospitalSupplier);
                insertChangeLog(hospitalSupplier, "CREATE", createBy, null, hospitalSupplier);
            }
        }

        return 1;
    }

    @Override
    @Transactional
    public int submitAssociationFromSupplier(Long supplierId, Long hospitalId, Date supplyStartDate, Date supplyEndDate,
        String createBy, String contractNo, String applyReason, String contactPerson, String contactPhone)
    {
        if (supplierId == null || hospitalId == null)
        {
            throw new ServiceException("供应商与医院不能为空");
        }
        validateSupplyDateRange(supplyStartDate, supplyEndDate);
        hospitalSupplierPermissionService.assertBindAllowed(hospitalId, supplierId);

        HospitalSupplier q = new HospitalSupplier();
        q.setSupplierId(supplierId);
        q.setHospitalId(hospitalId);
        HospitalSupplier existing = hospitalSupplierMapper.selectHospitalSupplierByHospitalAndSupplier(q);
        if (existing != null)
        {
            throw new ServiceException("该医院与供应商关联已存在，请勿重复发起");
        }

        ScmHospitalSupplierApply aq = new ScmHospitalSupplierApply();
        aq.setSupplierId(String.valueOf(supplierId));
        aq.setHospitalId(String.valueOf(hospitalId));
        ScmHospitalSupplierApply latest = hospitalSupplierApplyMapper.selectLatestByHospitalAndSupplier(aq);
        if (latest != null && "0".equals(latest.getAuditStatus()))
        {
            throw new ServiceException("已存在待审核申请，请勿重复提交");
        }

        ScmHospitalSupplierApply apply = new ScmHospitalSupplierApply();
        apply.setApplyId(IdUtils.dashedUuid7());
        apply.setSupplierId(String.valueOf(supplierId));
        apply.setHospitalId(String.valueOf(hospitalId));
        apply.setSupplierName(getSupplierName(supplierId));
        apply.setHospitalName(getHospitalName(hospitalId));
        apply.setSupplyStartDate(supplyStartDate);
        apply.setSupplyEndDate(supplyEndDate);
        apply.setContractNo(contractNo);
        apply.setApplyReason(applyReason);
        apply.setContactPerson(contactPerson);
        apply.setContactPhone(contactPhone);
        apply.setAuditStatus("0");
        apply.setDelFlag("0");
        apply.setCreateBy(createBy);
        int rows = hospitalSupplierApplyMapper.insertApply(apply);
        if (rows > 0)
        {
            insertApplyLog(apply, "SUBMIT", createBy, null, apply);
            snapshotSupplierCertificatesForApply(apply);
        }
        return rows;
    }

    private void snapshotSupplierCertificatesForApply(ScmHospitalSupplierApply apply)
    {
        SupplierCertificate q = new SupplierCertificate();
        q.setSupplierId(Long.valueOf(apply.getSupplierId()));
        List<SupplierCertificate> certs = supplierCertificateMapper.selectSupplierCertificateList(q);
        String json = certs == null ? "[]" : JSON.toJSONString(certs);
        ScmSupplierCertApplyBundle bundle = new ScmSupplierCertApplyBundle();
        bundle.setId(IdUtils.dashedUuid7());
        bundle.setApplyId(apply.getApplyId());
        bundle.setHospitalId(apply.getHospitalId());
        bundle.setSupplierId(apply.getSupplierId());
        bundle.setCertBundleJson(json);
        scmSupplierCertApplyBundleMapper.insertBundle(bundle);
    }

    @Override
    public List<ScmHospitalSupplierApply> selectAssociationApplyList(ScmHospitalSupplierApply query)
    {
        return hospitalSupplierApplyMapper.selectApplyList(query);
    }

    @Override
    public List<ScmHospitalSupplierApply> selectSupplierApplyList(Long supplierId, String auditStatus,
        String hospitalKeyword, String supplierKeyword)
    {
        if (supplierId == null)
        {
            return java.util.Collections.emptyList();
        }
        ScmHospitalSupplierApply q = new ScmHospitalSupplierApply();
        q.setSupplierId(String.valueOf(supplierId));
        q.setAuditStatus(auditStatus);
        q.setHospitalKeyword(StringUtils.trimToNull(hospitalKeyword));
        q.setSupplierKeyword(StringUtils.trimToNull(supplierKeyword));
        return hospitalSupplierApplyMapper.selectApplyList(q);
    }

    @Override
    @Transactional
    public int withdrawAssociationApply(String applyId, Long supplierId, String operBy)
    {
        if (StringUtils.isEmpty(applyId) || supplierId == null)
        {
            throw new ServiceException("参数不能为空");
        }
        ScmHospitalSupplierApply before = hospitalSupplierApplyMapper.selectByApplyId(applyId);
        if (before == null)
        {
            throw new ServiceException("申请不存在");
        }
        if (!String.valueOf(supplierId).equals(before.getSupplierId()))
        {
            throw new ServiceException("仅允许撤回本供应商申请");
        }
        if (!"0".equals(before.getAuditStatus()))
        {
            throw new ServiceException("仅待审核申请可撤回");
        }
        ScmHospitalSupplierApply update = new ScmHospitalSupplierApply();
        update.setApplyId(applyId);
        update.setDelFlag("2");
        update.setDelBy(operBy);
        update.setDelTime(DateUtils.getNowDate());
        update.setUpdateBy(operBy);
        int rows = hospitalSupplierApplyMapper.updateApply(update);
        if (rows > 0)
        {
            ScmHospitalSupplierApply after = hospitalSupplierApplyMapper.selectByApplyId(applyId);
            insertApplyLog(after == null ? before : after, "WITHDRAW", operBy, before, after);
        }
        return rows;
    }

    @Override
    @Transactional
    public int auditAssociationApply(String applyId, String approved, String auditRemark, String operBy, Long hospitalCtx)
    {
        if (StringUtils.isEmpty(applyId))
        {
            throw new ServiceException("申请ID不能为空");
        }
        ScmHospitalSupplierApply before = hospitalSupplierApplyMapper.selectByApplyId(applyId);
        if (before == null)
        {
            throw new ServiceException("申请不存在");
        }
        if (hospitalCtx != null && !String.valueOf(hospitalCtx).equals(before.getHospitalId()))
        {
            throw new ServiceException("仅允许审核当前医院发起的关联");
        }
        if (!"0".equals(before.getAuditStatus()))
        {
            throw new ServiceException("该申请已审核，请勿重复操作");
        }
        ScmHospitalSupplierApply update = new ScmHospitalSupplierApply();
        update.setApplyId(applyId);
        update.setAuditStatus("1".equals(approved) ? "1" : "2");
        update.setAuditBy(operBy);
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditRemark(auditRemark);
        update.setUpdateBy(operBy);
        int rows = hospitalSupplierApplyMapper.updateApply(update);
        if (rows > 0)
        {
            ScmHospitalSupplierApply after = hospitalSupplierApplyMapper.selectByApplyId(applyId);
            insertApplyLog(after, "1".equals(approved) ? "AUDIT_PASS" : "AUDIT_REJECT", operBy, before, after);
            if ("1".equals(approved))
            {
                createRelationFromApply(after, operBy);
            }
            publishAuditNotice(after, "1".equals(approved), auditRemark, operBy);
        }
        return rows;
    }

    private void createRelationFromApply(ScmHospitalSupplierApply apply, String operBy)
    {
        if (apply == null)
        {
            return;
        }
        Long hospitalId = Long.valueOf(apply.getHospitalId());
        Long supplierId = Long.valueOf(apply.getSupplierId());
        HospitalSupplier q = new HospitalSupplier();
        q.setHospitalId(hospitalId);
        q.setSupplierId(supplierId);
        HospitalSupplier existing = hospitalSupplierMapper.selectHospitalSupplierByHospitalAndSupplier(q);
        if (existing != null)
        {
            return;
        }
        HospitalSupplier relation = new HospitalSupplier();
        relation.setHospitalId(hospitalId);
        relation.setSupplierId(supplierId);
        relation.setBindStatus("1");
        relation.setBindBy(operBy);
        relation.setBindTime(DateUtils.getNowDate());
        relation.setStatus("0");
        relation.setDisableStatus("0");
        relation.setAuditStatus("1");
        relation.setAuditBy(operBy);
        relation.setAuditTime(DateUtils.getNowDate());
        relation.setSupplyStartDate(apply.getSupplyStartDate());
        relation.setSupplyEndDate(apply.getSupplyEndDate());
        relation.setRemark(apply.getAuditRemark());
        relation.setCreateBy(operBy);
        hospitalSupplierMapper.insertHospitalSupplier(relation);
        insertChangeLog(relation, "CREATE_FROM_APPLY", operBy, null, relation);
        scmScopeBootstrapService.applyDefaultHospitalGrantedSupplierMenus(hospitalId, supplierId, operBy);
    }

    private void validateSupplyDateRange(Date supplyStartDate, Date supplyEndDate)
    {
        if (supplyStartDate == null || supplyEndDate == null)
        {
            return;
        }
        if (supplyStartDate.after(supplyEndDate))
        {
            throw new ServiceException("供货开始时间不能晚于供货结束时间");
        }
    }

    private void assertRelationDeletable(HospitalSupplier relation)
    {
        if (relation == null)
        {
            return;
        }
        boolean hasAudit = relation.getAuditTime() != null
            || StringUtils.isNotEmpty(relation.getAuditBy())
            || (StringUtils.isNotEmpty(relation.getAuditStatus()) && !"0".equals(relation.getAuditStatus()));
        HospitalSupplier q = new HospitalSupplier();
        q.setHospitalId(relation.getHospitalId());
        q.setSupplierId(relation.getSupplierId());
        boolean hasOrder = hospitalSupplierMapper.countOrderByHospitalAndSupplier(q) > 0;
        boolean hasDelivery = hospitalSupplierMapper.countDeliveryByHospitalAndSupplier(q) > 0;
        if (hasAudit || hasOrder || hasDelivery)
        {
            throw new ServiceException("该关联已发生审核/订单/配送业务，不允许删除");
        }
    }

    private void insertChangeLog(HospitalSupplier seed, String changeType, String operBy, HospitalSupplier before,
        HospitalSupplier after)
    {
        if (seed == null)
        {
            return;
        }
        HospitalSupplierChangeLog row = new HospitalSupplierChangeLog();
        row.setLogId(IdUtils.dashedUuid7());
        row.setRelationId(seed.getRelationId() == null ? "" : String.valueOf(seed.getRelationId()));
        row.setHospitalId(seed.getHospitalId() == null ? "" : String.valueOf(seed.getHospitalId()));
        row.setSupplierId(seed.getSupplierId() == null ? "" : String.valueOf(seed.getSupplierId()));
        row.setHospitalName(getHospitalName(seed.getHospitalId()));
        row.setSupplierName(getSupplierName(seed.getSupplierId()));
        row.setChangeType(changeType);
        row.setOperBy(operBy);
        row.setChangeSnapshot(buildSnapshot(before, after));
        hospitalSupplierChangeLogMapper.insertHospitalSupplierChangeLog(row);
    }

    private String buildSnapshot(HospitalSupplier before, HospitalSupplier after)
    {
        JSONObject o = new JSONObject();
        if (before != null)
        {
            o.put("before", before);
        }
        if (after != null)
        {
            o.put("after", after);
        }
        return o.toJSONString();
    }

    private String getHospitalName(Long hospitalId)
    {
        if (hospitalId == null)
        {
            return "";
        }
        Hospital hospital = hospitalService.selectHospitalById(hospitalId);
        return hospital == null ? "" : hospital.getHospitalName();
    }

    private String getSupplierName(Long supplierId)
    {
        if (supplierId == null)
        {
            return "";
        }
        Supplier supplier = supplierService.selectSupplierById(supplierId);
        return supplier == null ? "" : supplier.getCompanyName();
    }

    private String currentOperBy()
    {
        try
        {
            return ShiroUtils.getLoginName();
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private void publishAuditNotice(ScmHospitalSupplierApply relation, boolean approved, String auditRemark, String operBy)
    {
        if (relation == null)
        {
            return;
        }
        String supplierName = relation.getSupplierName();
        String hospitalName = relation.getHospitalName();
        String title = approved ? "医院关联申请审核通过" : "医院关联申请审核拒绝";
        StringBuilder content = new StringBuilder();
        content.append("供应商【").append(supplierName).append("】向医院【").append(hospitalName).append("】发起的关联申请已审核");
        content.append(approved ? "通过" : "拒绝").append("。");
        if (relation.getSupplyStartDate() != null || relation.getSupplyEndDate() != null)
        {
            content.append(" 供货期限：");
            content.append(relation.getSupplyStartDate() == null ? "-" : DateUtils.parseDateToStr("yyyy-MM-dd", relation.getSupplyStartDate()));
            content.append(" 至 ");
            content.append(relation.getSupplyEndDate() == null ? "-" : DateUtils.parseDateToStr("yyyy-MM-dd", relation.getSupplyEndDate()));
            content.append("。");
        }
        if (StringUtils.isNotEmpty(auditRemark))
        {
            content.append(" 审核备注：").append(auditRemark).append("。");
        }
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(title);
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setNoticeContent(content.toString());
        notice.setCreateBy(StringUtils.isEmpty(operBy) ? "system" : operBy);
        noticeService.insertNotice(notice);
        bindNoticeReceivers(notice.getNoticeId(), Long.valueOf(relation.getSupplierId()), operBy);
    }

    private void bindNoticeReceivers(Long noticeId, Long supplierId, String operBy)
    {
        if (noticeId == null || supplierId == null)
        {
            return;
        }
        java.util.List<com.scm.system.domain.SupplierUser> supplierUsers =
            supplierUserService.selectSupplierUserListBySupplierId(supplierId);
        if (supplierUsers == null || supplierUsers.isEmpty())
        {
            return;
        }
        java.util.List<ScmNoticeReceiver> rows = new java.util.ArrayList<>();
        java.util.Set<Long> dedup = new java.util.HashSet<>();
        for (com.scm.system.domain.SupplierUser su : supplierUsers)
        {
            if (su == null || su.getUserId() == null || !dedup.add(su.getUserId()))
            {
                continue;
            }
            if (!"0".equals(su.getStatus()))
            {
                continue;
            }
            ScmNoticeReceiver row = new ScmNoticeReceiver();
            row.setId(IdUtils.dashedUuid7());
            row.setNoticeId(noticeId);
            row.setUserId(su.getUserId());
            row.setReadFlag("0");
            row.setCreateBy(StringUtils.isEmpty(operBy) ? "system" : operBy);
            rows.add(row);
        }
        if (!rows.isEmpty())
        {
            noticeReceiverMapper.batchInsert(rows);
        }
    }

    private void insertApplyLog(ScmHospitalSupplierApply seed, String actionType, String operBy,
        ScmHospitalSupplierApply before, ScmHospitalSupplierApply after)
    {
        if (seed == null)
        {
            return;
        }
        JSONObject s = new JSONObject();
        if (before != null)
        {
            s.put("before", before);
        }
        if (after != null)
        {
            s.put("after", after);
        }
        ScmHospitalSupplierApplyLog log = new ScmHospitalSupplierApplyLog();
        log.setLogId(IdUtils.dashedUuid7());
        log.setApplyId(seed.getApplyId());
        log.setSupplierId(seed.getSupplierId());
        log.setHospitalId(seed.getHospitalId());
        log.setActionType(actionType);
        log.setOperBy(operBy);
        log.setSnapshot(s.toJSONString());
        hospitalSupplierApplyLogMapper.insertLog(log);
    }

    @Override
    public HospitalSupplier assertSupplierApprovedRelationForModify(Long relationId, Long supplierId)
    {
        if (relationId == null || supplierId == null)
        {
            throw new ServiceException("参数不能为空");
        }
        HospitalSupplier rel = hospitalSupplierMapper.selectHospitalSupplierById(relationId);
        if (rel == null)
        {
            throw new ServiceException("关联不存在");
        }
        if (!supplierId.equals(rel.getSupplierId()))
        {
            throw new ServiceException("无权引用该关联");
        }
        if (!"1".equals(StringUtils.trimToEmpty(rel.getBindStatus())))
        {
            throw new ServiceException("仅已绑定关联可申请修改");
        }
        if (!"1".equals(StringUtils.trimToEmpty(rel.getAuditStatus())))
        {
            throw new ServiceException("仅已审核通过的关联可申请修改");
        }
        if (!"0".equals(StringUtils.trimToEmpty(rel.getStatus())))
        {
            throw new ServiceException("关联已停用，不可申请修改");
        }
        return rel;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitAssociationModifyFromSupplier(Long relationId, Long supplierId, Date supplyStartDate,
        Date supplyEndDate, String createBy, String contractNo, String applyReason, String contactPerson,
        String contactPhone)
    {
        HospitalSupplier rel = assertSupplierApprovedRelationForModify(relationId, supplierId);
        validateSupplyDateRange(supplyStartDate, supplyEndDate);
        if (hospitalSupplierModifyApplyMapper.countPendingByRelationId(relationId) > 0)
        {
            throw new ServiceException("该关联已存在待审核的修改申请，请勿重复提交");
        }
        ScmHospitalSupplierModifyApply row = new ScmHospitalSupplierModifyApply();
        row.setModifyApplyId(IdUtils.dashedUuid7());
        row.setRelationId(relationId);
        row.setSupplierId(String.valueOf(supplierId));
        row.setHospitalId(String.valueOf(rel.getHospitalId()));
        row.setSupplierName(getSupplierName(supplierId));
        row.setHospitalName(getHospitalName(rel.getHospitalId()));
        row.setPrevSupplyStartDate(rel.getSupplyStartDate());
        row.setPrevSupplyEndDate(rel.getSupplyEndDate());
        row.setPrevRemark(StringUtils.trimToEmpty(rel.getRemark()));
        row.setSupplyStartDate(supplyStartDate);
        row.setSupplyEndDate(supplyEndDate);
        row.setContractNo(contractNo);
        row.setApplyReason(applyReason);
        row.setContactPerson(contactPerson);
        row.setContactPhone(contactPhone);
        row.setBeforeSnapshot(JSON.toJSONString(rel));
        row.setAuditStatus("0");
        row.setDelFlag("0");
        row.setCreateBy(createBy);
        return hospitalSupplierModifyApplyMapper.insert(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int withdrawAssociationModifyApply(String modifyApplyId, Long supplierId, String operBy)
    {
        if (StringUtils.isEmpty(modifyApplyId) || supplierId == null)
        {
            throw new ServiceException("参数不能为空");
        }
        ScmHospitalSupplierModifyApply before = hospitalSupplierModifyApplyMapper.selectById(modifyApplyId);
        if (before == null)
        {
            throw new ServiceException("申请不存在");
        }
        if (!String.valueOf(supplierId).equals(before.getSupplierId()))
        {
            throw new ServiceException("仅允许撤回本供应商申请");
        }
        if (!"0".equals(before.getAuditStatus()))
        {
            throw new ServiceException("仅待审核申请可撤回");
        }
        ScmHospitalSupplierModifyApply upd = new ScmHospitalSupplierModifyApply();
        upd.setModifyApplyId(modifyApplyId);
        upd.setDelFlag("2");
        upd.setDelBy(operBy);
        upd.setDelTime(DateUtils.getNowDate());
        upd.setUpdateBy(operBy);
        return hospitalSupplierModifyApplyMapper.update(upd);
    }

    @Override
    public List<ScmHospitalSupplierModifyApply> selectAssociationModifyApplyList(ScmHospitalSupplierModifyApply query)
    {
        return hospitalSupplierModifyApplyMapper.selectList(query);
    }

    @Override
    public List<ScmHospitalSupplierModifyApply> selectSupplierModifyApplyList(Long supplierId, String auditStatus,
        String hospitalKeyword, String supplierKeyword)
    {
        if (supplierId == null)
        {
            return java.util.Collections.emptyList();
        }
        ScmHospitalSupplierModifyApply q = new ScmHospitalSupplierModifyApply();
        q.setSupplierId(String.valueOf(supplierId));
        q.setAuditStatus(auditStatus);
        q.setHospitalKeyword(StringUtils.trimToNull(hospitalKeyword));
        q.setSupplierKeyword(StringUtils.trimToNull(supplierKeyword));
        return hospitalSupplierModifyApplyMapper.selectList(q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditAssociationModifyApply(String modifyApplyId, String approved, String auditRemark, String operBy,
        Long hospitalCtx)
    {
        if (StringUtils.isEmpty(modifyApplyId))
        {
            throw new ServiceException("申请ID不能为空");
        }
        ScmHospitalSupplierModifyApply before = hospitalSupplierModifyApplyMapper.selectById(modifyApplyId);
        if (before == null)
        {
            throw new ServiceException("申请不存在");
        }
        if (hospitalCtx != null && !String.valueOf(hospitalCtx).equals(before.getHospitalId()))
        {
            throw new ServiceException("仅允许审核当前医院的关联修改申请");
        }
        if (!"0".equals(before.getAuditStatus()))
        {
            throw new ServiceException("该申请已审核，请勿重复操作");
        }
        ScmHospitalSupplierModifyApply upd = new ScmHospitalSupplierModifyApply();
        upd.setModifyApplyId(modifyApplyId);
        upd.setAuditStatus("1".equals(approved) ? "1" : "2");
        upd.setAuditBy(operBy);
        upd.setAuditTime(DateUtils.getNowDate());
        upd.setAuditRemark(auditRemark);
        upd.setUpdateBy(operBy);
        int rows = hospitalSupplierModifyApplyMapper.update(upd);
        if (rows > 0 && "1".equals(approved))
        {
            applyRelationUpdateFromModifyApply(before, operBy);
        }
        if (rows > 0)
        {
            ScmHospitalSupplierModifyApply after = hospitalSupplierModifyApplyMapper.selectById(modifyApplyId);
            publishModifyAuditNotice(after, "1".equals(approved), auditRemark, operBy);
        }
        return rows;
    }

    private void applyRelationUpdateFromModifyApply(ScmHospitalSupplierModifyApply apply, String operBy)
    {
        if (apply == null || apply.getRelationId() == null)
        {
            return;
        }
        HospitalSupplier rel = hospitalSupplierMapper.selectHospitalSupplierById(apply.getRelationId());
        if (rel == null)
        {
            throw new ServiceException("关联已不存在，无法生效修改");
        }
        if (!String.valueOf(rel.getHospitalId()).equals(apply.getHospitalId())
            || !String.valueOf(rel.getSupplierId()).equals(apply.getSupplierId()))
        {
            throw new ServiceException("关联数据与申请不一致，拒绝生效");
        }
        HospitalSupplier upd = new HospitalSupplier();
        upd.setRelationId(apply.getRelationId());
        upd.setSupplyStartDate(apply.getSupplyStartDate());
        upd.setSupplyEndDate(apply.getSupplyEndDate());
        upd.setUpdateBy(operBy);
        StringBuilder rm = new StringBuilder();
        if (StringUtils.isNotEmpty(apply.getContractNo()))
        {
            rm.append("合同编号:").append(apply.getContractNo()).append(" ");
        }
        if (StringUtils.isNotEmpty(apply.getContactPerson()))
        {
            rm.append("联系人:").append(apply.getContactPerson()).append(" ");
        }
        if (StringUtils.isNotEmpty(apply.getContactPhone()))
        {
            rm.append("电话:").append(apply.getContactPhone());
        }
        if (rm.length() > 0)
        {
            upd.setRemark(rm.toString().trim());
        }
        else if (StringUtils.isNotEmpty(apply.getApplyReason()))
        {
            upd.setRemark(apply.getApplyReason());
        }
        updateHospitalSupplier(upd);
    }

    private void publishModifyAuditNotice(ScmHospitalSupplierModifyApply row, boolean approved, String auditRemark,
        String operBy)
    {
        if (row == null)
        {
            return;
        }
        String supplierName = row.getSupplierName();
        String hospitalName = row.getHospitalName();
        String title = approved ? "医院关联修改申请审核通过" : "医院关联修改申请审核拒绝";
        StringBuilder content = new StringBuilder();
        content.append("供应商【").append(supplierName).append("】对医院【").append(hospitalName).append("】的关联信息修改申请已审核");
        content.append(approved ? "通过" : "拒绝").append("。");
        if (row.getSupplyStartDate() != null || row.getSupplyEndDate() != null)
        {
            content.append(" 申请供货期限：");
            content.append(row.getSupplyStartDate() == null ? "-" : DateUtils.parseDateToStr("yyyy-MM-dd", row.getSupplyStartDate()));
            content.append(" 至 ");
            content.append(row.getSupplyEndDate() == null ? "-" : DateUtils.parseDateToStr("yyyy-MM-dd", row.getSupplyEndDate()));
            content.append("。");
        }
        if (StringUtils.isNotEmpty(auditRemark))
        {
            content.append(" 审核备注：").append(auditRemark).append("。");
        }
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(title);
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setNoticeContent(content.toString());
        notice.setCreateBy(StringUtils.isEmpty(operBy) ? "system" : operBy);
        noticeService.insertNotice(notice);
        bindNoticeReceivers(notice.getNoticeId(), Long.valueOf(row.getSupplierId()), operBy);
    }
}

