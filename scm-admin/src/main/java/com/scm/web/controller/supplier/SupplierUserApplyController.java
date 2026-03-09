package com.scm.web.controller.supplier;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.utils.ShiroUtils;
import com.scm.system.domain.SupplierUserApply;
import com.scm.system.mapper.SupplierUserApplyMapper;
import com.scm.system.service.ISupplierRegisterService;
import com.scm.system.service.ISupplierUserService;

/**
 * 供应商管理员 - 业务员关联申请列表与审核
 */
@Controller
@RequestMapping("/supplier/apply")
public class SupplierUserApplyController extends BaseController {

    @Autowired
    private ISupplierUserService supplierUserService;
    @Autowired
    private SupplierUserApplyMapper applyMapper;
    @Autowired
    private ISupplierRegisterService supplierRegisterService;

    @RequiresPermissions("supplier:apply:list")
    @GetMapping()
    public String index() {
        return "supplier/apply";
    }

    /**
     * 待审核申请列表（当前登录用户为供应商管理员时，仅能看到本供应商的申请）
     */
    @RequiresPermissions("supplier:apply:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(@RequestParam(value = "status", required = false) String status) {
        startPage();
        Long userId = ShiroUtils.getUserId();
        Long supplierId = supplierUserService.getManagedSupplierId(userId);
        if (supplierId == null) {
            return getDataTable(new java.util.ArrayList<>());
        }
        List<SupplierUserApply> list = applyMapper.selectBySupplierId(supplierId, status);
        return getDataTable(list);
    }

    /**
     * 审核：通过或拒绝
     * @param applyId 申请ID
     * @param approved 1通过 其他拒绝
     * @param auditRemark 审核备注
     */
    @RequiresPermissions("supplier:apply:audit")
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult audit(@RequestParam Long applyId,
                            @RequestParam String approved,
                            @RequestParam(required = false) String auditRemark) {
        try {
            String operBy = ShiroUtils.getLoginName();
            supplierRegisterService.approveApply(applyId, approved, auditRemark, operBy);
            return success("1".equals(approved) ? "审核通过" : "已拒绝");
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            logger.error("审核失败", e);
            return error("操作失败：" + e.getMessage());
        }
    }
}
