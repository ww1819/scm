package com.scm.web.controller.certificate;

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
import com.scm.system.domain.CertificateType;
import com.scm.system.domain.CertificateConfig;
import com.scm.system.service.ICertificateTypeService;
import com.scm.system.service.ICertificateConfigService;

/**
 * 证件类型信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/certificate/type")
public class CertificateTypeController extends BaseController
{
    private String prefix = "certificate/type";

    @Autowired
    private ICertificateTypeService certificateTypeService;

    @Autowired
    private ICertificateConfigService certificateConfigService;

    @RequiresPermissions("certificate:type:view")
    @GetMapping()
    public String certificateType()
    {
        return prefix + "/type";
    }

    /**
     * 查询证件类型列表
     */
    @RequiresPermissions("certificate:type:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(CertificateType certificateType)
    {
        startPage();
        List<CertificateType> list = certificateTypeService.selectCertificateTypeList(certificateType);
        return getDataTable(list);
    }

    /**
     * 导出证件类型列表
     */
    @RequiresPermissions("certificate:type:export")
    @Log(title = "证件类型管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(CertificateType certificateType)
    {
        List<CertificateType> list = certificateTypeService.selectCertificateTypeList(certificateType);
        ExcelUtil<CertificateType> util = new ExcelUtil<CertificateType>(CertificateType.class);
        return util.exportExcel(list, "证件类型数据");
    }

    /**
     * 新增证件类型
     */
    @RequiresPermissions("certificate:type:add")
    @GetMapping("/add")
    public String add(String typeCategory, ModelMap mmap)
    {
        mmap.put("typeCategory", typeCategory);
        return prefix + "/add";
    }

    /**
     * 新增保存证件类型
     */
    @RequiresPermissions("certificate:type:add")
    @Log(title = "证件类型管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated CertificateType certificateType)
    {
        if (!certificateTypeService.checkTypeCodeUnique(certificateType))
        {
            return error("新增证件类型'" + certificateType.getTypeName() + "'失败，类型编码已存在");
        }
        certificateType.setCreateBy(getLoginName());
        return toAjax(certificateTypeService.insertCertificateType(certificateType));
    }

    /**
     * 修改证件类型
     */
    @RequiresPermissions("certificate:type:edit")
    @GetMapping("/edit/{typeId}")
    public String edit(@PathVariable("typeId") Long typeId, ModelMap mmap)
    {
        CertificateType certificateType = certificateTypeService.selectCertificateTypeById(typeId);
        mmap.put("certificateType", certificateType);
        return prefix + "/edit";
    }

    /**
     * 修改保存证件类型
     */
    @RequiresPermissions("certificate:type:edit")
    @Log(title = "证件类型管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated CertificateType certificateType)
    {
        if (!certificateTypeService.checkTypeCodeUnique(certificateType))
        {
            return error("修改证件类型'" + certificateType.getTypeName() + "'失败，类型编码已存在");
        }
        certificateType.setUpdateBy(getLoginName());
        return toAjax(certificateTypeService.updateCertificateType(certificateType));
    }

    /**
     * 删除证件类型
     */
    @RequiresPermissions("certificate:type:remove")
    @Log(title = "证件类型管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(certificateTypeService.deleteCertificateTypeByIds(ids));
    }

    /**
     * 证件过期预警配置页面
     */
    @RequiresPermissions("certificate:type:view")
    @GetMapping("/config")
    public String config(ModelMap mmap)
    {
        // 查询所有配置
        CertificateConfig queryConfig = new CertificateConfig();
        List<CertificateConfig> configList = certificateConfigService.selectCertificateConfigList(queryConfig);
        mmap.put("configList", configList);
        return prefix + "/config";
    }

    /**
     * 保存证件过期预警配置
     */
    @RequiresPermissions("certificate:type:edit")
    @Log(title = "证件过期预警配置", businessType = BusinessType.UPDATE)
    @PostMapping("/config/save")
    @ResponseBody
    public AjaxResult saveConfig(CertificateConfig certificateConfig)
    {
        if (certificateConfig.getConfigId() == null)
        {
            // 新增配置
            certificateConfig.setCreateBy(getLoginName());
            return toAjax(certificateConfigService.insertCertificateConfig(certificateConfig));
        }
        else
        {
            // 更新配置
            certificateConfig.setUpdateBy(getLoginName());
            return toAjax(certificateConfigService.updateCertificateConfig(certificateConfig));
        }
    }

    /**
     * 查询证件配置
     */
    @RequiresPermissions("certificate:type:view")
    @PostMapping("/config/get")
    @ResponseBody
    public AjaxResult getConfig(String configType, String certificateType)
    {
        CertificateConfig config = certificateConfigService.selectCertificateConfigByType(configType, certificateType);
        if (config == null)
        {
            // 如果配置不存在，返回默认值
            config = new CertificateConfig();
            config.setWarningDays(30);
            config.setRecentDays(7);
        }
        return success(config);
    }
}

