package com.scm.web.controller.system;

import java.util.ArrayList;
import java.util.List;
import com.github.pagehelper.PageInfo;
import com.scm.common.core.page.TableDataInfo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.scm.common.annotation.Log;
import com.scm.common.constant.UserConstants;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.Ztree;
import com.scm.common.core.domain.entity.SysDept;
import com.scm.common.enums.BusinessType;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.ServletUtils;
import com.scm.common.utils.StringUtils;
import com.scm.framework.aspectj.DataScopeAspect;
import com.scm.system.service.ISysDeptService;

/**
 * 部门信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController
{
    private String prefix = "system/dept";

    @Autowired
    private ISysDeptService deptService;

    @RequiresPermissions("system:dept:view")
    @GetMapping()
    public String dept()
    {
        return prefix + "/dept";
    }

    @RequiresPermissions("system:dept:list")
    @PostMapping("/list")
    @ResponseBody
    public Object list(SysDept dept)
    {
        // 带 pageNum：树表首页分页（虚拟根，每页最多 10 条根）；并合并各根的直属子部门（默认展开到省级，市/区县仍懒加载）
        if (StringUtils.isNotEmpty(ServletUtils.getParameter("pageNum")))
        {
            dept.setParentId(null);
            String dataScope = Convert.toStr(dept.getParams().get(DataScopeAspect.DATA_SCOPE));
            if (StringUtils.isNotEmpty(dataScope))
            {
                dept.getParams().put("dataScopeAliasD3", dataScope.replace("d.", "d3."));
            }
            dept.getParams().put("treeFirstPage", Boolean.TRUE);
            startPage();
            List<SysDept> roots = deptService.selectDeptList(dept);
            long rootTotal = new PageInfo<>(roots).getTotal();
            clearPage();
            dept.getParams().remove("treeFirstPage");
            dept.getParams().remove("dataScopeAliasD3");

            List<SysDept> merged = new ArrayList<>();
            for (SysDept r : roots)
            {
                merged.add(r);
                SysDept childQuery = new SysDept();
                childQuery.setParentId(r.getDeptId());
                if (StringUtils.isNotEmpty(dept.getDeptName()))
                {
                    childQuery.setDeptName(dept.getDeptName());
                }
                if (StringUtils.isNotEmpty(dept.getStatus()))
                {
                    childQuery.setStatus(dept.getStatus());
                }
                List<SysDept> subs = deptService.selectDeptList(childQuery);
                merged.addAll(subs);
            }
            deptService.fillDeptTreeLeafFlag(merged);
            TableDataInfo rsp = new TableDataInfo();
            rsp.setCode(0);
            rsp.setRows(merged);
            rsp.setTotal(rootTotal);
            return rsp;
        }
        // 无 pageNum 且有 parentId：懒加载直属子部门（整批返回，树表展开用）
        if (dept.getParentId() != null)
        {
            List<SysDept> children = deptService.selectDeptList(dept);
            deptService.fillDeptTreeLeafFlag(children);
            return children;
        }
        // 兼容旧调用：一次查全表
        return deptService.selectDeptList(dept);
    }

    /**
     * 新增部门
     */
    @RequiresPermissions("system:dept:add")
    @GetMapping("/add/{parentId}")
    public String add(@PathVariable("parentId") Long parentId, ModelMap mmap)
    {
        if (!getSysUser().isAdmin())
        {
            parentId = getSysUser().getDeptId();
        }
        mmap.put("dept", deptService.selectDeptById(parentId));
        return prefix + "/add";
    }

    /**
     * 新增保存部门
     */
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("system:dept:add")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SysDept dept)
    {
        if (!deptService.checkDeptNameUnique(dept))
        {
            return error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(getLoginName());
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @RequiresPermissions("system:dept:edit")
    @GetMapping("/edit/{deptId}")
    public String edit(@PathVariable("deptId") Long deptId, ModelMap mmap)
    {
        deptService.checkDeptDataScope(deptId);
        SysDept dept = deptService.selectDeptById(deptId);
        if (StringUtils.isNotNull(dept) && 100L == deptId)
        {
            dept.setParentName("无");
        }
        mmap.put("dept", dept);
        return prefix + "/edit";
    }

    /**
     * 修改保存部门
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:dept:edit")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SysDept dept)
    {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept))
        {
            return error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        else if (dept.getParentId().equals(deptId))
        {
            return error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0)
        {
            return AjaxResult.error("该部门包含未停用的子部门！");
        }
        dept.setUpdateBy(getLoginName());
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除
     */
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("system:dept:remove")
    @GetMapping("/remove/{deptId}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("deptId") Long deptId)
    {
        if (deptService.selectDeptCount(deptId) > 0)
        {
            return AjaxResult.warn("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId))
        {
            return AjaxResult.warn("部门存在用户,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return toAjax(deptService.deleteDeptById(deptId));
    }

    /**
     * 校验部门名称
     */
    @PostMapping("/checkDeptNameUnique")
    @ResponseBody
    public boolean checkDeptNameUnique(SysDept dept)
    {
        return deptService.checkDeptNameUnique(dept);
    }

    /**
     * 选择部门树
     * 
     * @param deptId 部门ID
     * @param excludeId 排除ID
     */
    @RequiresPermissions("system:dept:list")
    @GetMapping(value = { "/selectDeptTree/{deptId}", "/selectDeptTree/{deptId}/{excludeId}" })
    public String selectDeptTree(@PathVariable("deptId") Long deptId, @PathVariable(value = "excludeId", required = false) Long excludeId, ModelMap mmap)
    {
        mmap.put("dept", deptService.selectDeptById(deptId));
        mmap.put("excludeId", excludeId);
        return prefix + "/tree";
    }

    /**
     * 加载部门列表树（排除下级）
     */
    @RequiresPermissions("system:dept:list")
    @GetMapping("/treeData/{excludeId}")
    @ResponseBody
    public List<Ztree> treeDataExcludeChild(@PathVariable(value = "excludeId", required = false) Long excludeId)
    {
        SysDept dept = new SysDept();
        dept.setExcludeId(excludeId);
        List<Ztree> ztrees = deptService.selectDeptTreeExcludeChild(dept);
        return ztrees;
    }
}
