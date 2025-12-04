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
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.poi.ExcelUtil;
import com.scm.system.domain.MaterialDict;
import com.scm.system.service.IMaterialDictService;

/**
 * 物资字典信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/material/dict")
public class MaterialDictController extends BaseController
{
    private String prefix = "material/dict";

    @Autowired
    private IMaterialDictService materialDictService;

    @RequiresPermissions("material:dict:view")
    @GetMapping()
    public String materialDict()
    {
        return prefix + "/dict";
    }

    /**
     * 产品选择页面
     */
    @GetMapping("/select")
    public String select()
    {
        return prefix + "/select";
    }

    /**
     * 查询物资字典列表
     */
    @RequiresPermissions("material:dict:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(MaterialDict materialDict)
    {
        startPage();
        List<MaterialDict> list = materialDictService.selectMaterialDictList(materialDict);
        return getDataTable(list);
    }

    /**
     * 导出物资字典列表
     */
    @RequiresPermissions("material:dict:export")
    @Log(title = "物资字典管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(MaterialDict materialDict)
    {
        List<MaterialDict> list = materialDictService.selectMaterialDictList(materialDict);
        ExcelUtil<MaterialDict> util = new ExcelUtil<MaterialDict>(MaterialDict.class);
        return util.exportExcel(list, "物资字典数据");
    }

    /**
     * 新增物资字典
     */
    @RequiresPermissions("material:dict:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存物资字典
     */
    @RequiresPermissions("material:dict:add")
    @Log(title = "物资字典管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated MaterialDict materialDict)
    {
        if (!materialDictService.checkMaterialCodeUnique(materialDict))
        {
            return error("新增物资'" + materialDict.getMaterialName() + "'失败，产品编码已存在");
        }
        materialDict.setCreateBy(getLoginName());
        return toAjax(materialDictService.insertMaterialDict(materialDict));
    }

    /**
     * 获取物资字典详细信息
     */
    @GetMapping("/getInfo/{materialId}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("materialId") Long materialId)
    {
        return success(materialDictService.selectMaterialDictById(materialId));
    }

    /**
     * 根据产品编码获取物资信息
     */
    @GetMapping("/getByCode/{materialCode}")
    @ResponseBody
    public AjaxResult getByCode(@PathVariable("materialCode") String materialCode)
    {
        MaterialDict materialDict = materialDictService.selectMaterialDictByCode(materialCode);
        if (materialDict == null)
        {
            return error("未找到产品编码为'" + materialCode + "'的产品");
        }
        return success(materialDict);
    }

    /**
     * 修改物资字典
     */
    @RequiresPermissions("material:dict:edit")
    @GetMapping("/edit/{materialId}")
    public String edit(@PathVariable("materialId") Long materialId, ModelMap mmap)
    {
        MaterialDict materialDict = materialDictService.selectMaterialDictById(materialId);
        mmap.put("materialDict", materialDict);
        return prefix + "/edit";
    }

    /**
     * 修改保存物资字典
     */
    @RequiresPermissions("material:dict:edit")
    @Log(title = "物资字典管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated MaterialDict materialDict)
    {
        if (!materialDictService.checkMaterialCodeUnique(materialDict))
        {
            return error("修改物资'" + materialDict.getMaterialName() + "'失败，产品编码已存在");
        }
        materialDict.setUpdateBy(getLoginName());
        return toAjax(materialDictService.updateMaterialDict(materialDict));
    }

    /**
     * 删除物资字典
     */
    @RequiresPermissions("material:dict:remove")
    @Log(title = "物资字典管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(materialDictService.deleteMaterialDictByIds(ids));
    }

    /**
     * 启用/停用物资字典
     */
    @RequiresPermissions("material:dict:edit")
    @Log(title = "物资字典管理", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(MaterialDict materialDict)
    {
        materialDict.setUpdateBy(getLoginName());
        return toAjax(materialDictService.updateMaterialDict(materialDict));
    }
}

