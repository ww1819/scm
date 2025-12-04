package com.scm.web.controller.material;

import java.util.List;
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
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.Ztree;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.MaterialCategory;
import com.scm.system.service.IMaterialCategoryService;

/**
 * 耗材分类信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/material/category")
public class MaterialCategoryController extends BaseController
{
    private String prefix = "material/category";

    @Autowired
    private IMaterialCategoryService materialCategoryService;

    @RequiresPermissions("material:category:view")
    @GetMapping()
    public String category()
    {
        return prefix + "/category";
    }

    /**
     * 查询耗材分类列表
     */
    @RequiresPermissions("material:category:list")
    @PostMapping("/list")
    @ResponseBody
    public List<MaterialCategory> list(MaterialCategory materialCategory)
    {
        List<MaterialCategory> list = materialCategoryService.selectMaterialCategoryList(materialCategory);
        return list;
    }

    /**
     * 加载耗材分类树列表
     */
    @RequiresPermissions("material:category:list")
    @GetMapping("/treeData")
    @ResponseBody
    public List<Ztree> treeData()
    {
        List<Ztree> ztrees = materialCategoryService.selectMaterialCategoryTree();
        return ztrees;
    }

    /**
     * 新增耗材分类
     */
    @RequiresPermissions("material:category:add")
    @GetMapping("/add/{parentId}")
    public String add(@PathVariable("parentId") Long parentId, ModelMap mmap)
    {
        MaterialCategory category = new MaterialCategory();
        if (0L != parentId)
        {
            category.setParentId(parentId);
            MaterialCategory parentCategory = materialCategoryService.selectMaterialCategoryById(parentId);
            category.setParentName(parentCategory.getCategoryName());
        }
        else
        {
            category.setParentId(0L);
            category.setParentName("主分类");
        }
        mmap.put("category", category);
        return prefix + "/add";
    }

    /**
     * 新增保存耗材分类
     */
    @RequiresPermissions("material:category:add")
    @Log(title = "耗材分类管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated MaterialCategory materialCategory)
    {
        if (StringUtils.isNull(materialCategory.getParentId()))
        {
            materialCategory.setParentId(0L);
        }
        materialCategory.setCreateBy(getLoginName());
        return toAjax(materialCategoryService.insertMaterialCategory(materialCategory));
    }

    /**
     * 修改耗材分类
     */
    @RequiresPermissions("material:category:edit")
    @GetMapping("/edit/{categoryId}")
    public String edit(@PathVariable("categoryId") Long categoryId, ModelMap mmap)
    {
        MaterialCategory materialCategory = materialCategoryService.selectMaterialCategoryById(categoryId);
        if (StringUtils.isNotNull(materialCategory) && 0L == categoryId)
        {
            materialCategory.setParentName("主分类");
        }
        mmap.put("category", materialCategory);
        return prefix + "/edit";
    }

    /**
     * 修改保存耗材分类
     */
    @RequiresPermissions("material:category:edit")
    @Log(title = "耗材分类管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated MaterialCategory materialCategory)
    {
        if (StringUtils.isNull(materialCategory.getParentId()))
        {
            materialCategory.setParentId(0L);
        }
        materialCategory.setUpdateBy(getLoginName());
        return toAjax(materialCategoryService.updateMaterialCategory(materialCategory));
    }

    /**
     * 删除耗材分类
     */
    @RequiresPermissions("material:category:remove")
    @Log(title = "耗材分类管理", businessType = BusinessType.DELETE)
    @GetMapping("/remove/{categoryId}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("categoryId") Long categoryId)
    {
        if (materialCategoryService.selectMaterialCategoryList(new MaterialCategory()).size() > 0)
        {
            return AjaxResult.warn("存在下级分类，不允许删除");
        }
        return toAjax(materialCategoryService.deleteMaterialCategoryById(categoryId));
    }

    /**
     * 校验分类名称
     */
    @PostMapping("/checkCategoryNameUnique")
    @ResponseBody
    public String checkCategoryNameUnique(MaterialCategory materialCategory)
    {
        return materialCategoryService.selectMaterialCategoryList(materialCategory).size() > 0 ? "1" : "0";
    }
}

