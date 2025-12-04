package com.scm.web.controller.hospital;

import java.util.ArrayList;
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
import com.scm.system.domain.Hospital;
import com.scm.system.domain.HospitalSupplier;
import com.scm.system.service.IHospitalService;
import com.scm.system.service.IHospitalSupplierService;

/**
 * 医院信息
 * 
 * @author scm
 */
@Controller
@RequestMapping("/hospital/hospital")
public class HospitalController extends BaseController
{
    private String prefix = "hospital";

    @Autowired
    private IHospitalService hospitalService;
    
    @Autowired
    private IHospitalSupplierService hospitalSupplierService;

    @RequiresPermissions("hospital:hospital:view")
    @GetMapping()
    public String hospital()
    {
        return prefix + "/hospital";
    }

    /**
     * 查询医院信息列表
     */
    @RequiresPermissions("hospital:hospital:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Hospital hospital)
    {
        startPage();
        List<Hospital> list = hospitalService.selectHospitalList(hospital);
        return getDataTable(list);
    }

    /**
     * 导出医院信息列表
     */
    @RequiresPermissions("hospital:hospital:export")
    @Log(title = "医院管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Hospital hospital)
    {
        List<Hospital> list = hospitalService.selectHospitalList(hospital);
        ExcelUtil<Hospital> util = new ExcelUtil<Hospital>(Hospital.class);
        return util.exportExcel(list, "医院数据");
    }

    /**
     * 新增医院信息
     */
    @RequiresPermissions("hospital:hospital:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存医院信息
     */
    @RequiresPermissions("hospital:hospital:add")
    @Log(title = "医院管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated Hospital hospital)
    {
        if (!hospitalService.checkHospitalCodeUnique(hospital))
        {
            return error("新增医院'" + hospital.getHospitalName() + "'失败，医院编码已存在");
        }
        hospital.setCreateBy(getLoginName());
        return toAjax(hospitalService.insertHospital(hospital));
    }

    /**
     * 查看医院详情
     */
    @RequiresPermissions("hospital:hospital:view")
    @GetMapping("/view/{hospitalId}")
    public String view(@PathVariable("hospitalId") Long hospitalId, ModelMap mmap)
    {
        Hospital hospital = hospitalService.selectHospitalById(hospitalId);
        mmap.put("hospital", hospital);
        return prefix + "/view";
    }

    /**
     * 修改医院信息
     */
    @RequiresPermissions("hospital:hospital:edit")
    @GetMapping("/edit/{hospitalId}")
    public String edit(@PathVariable("hospitalId") Long hospitalId, ModelMap mmap)
    {
        Hospital hospital = hospitalService.selectHospitalById(hospitalId);
        mmap.put("hospital", hospital);
        return prefix + "/edit";
    }

    /**
     * 修改保存医院信息
     */
    @RequiresPermissions("hospital:hospital:edit")
    @Log(title = "医院管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated Hospital hospital)
    {
        if (!hospitalService.checkHospitalCodeUnique(hospital))
        {
            return error("修改医院'" + hospital.getHospitalName() + "'失败，医院编码已存在");
        }
        hospital.setUpdateBy(getLoginName());
        return toAjax(hospitalService.updateHospital(hospital));
    }

    /**
     * 删除医院信息
     */
    @RequiresPermissions("hospital:hospital:remove")
    @Log(title = "医院管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(hospitalService.deleteHospitalByIds(ids));
    }

    /**
     * 启用/停用医院
     */
    @RequiresPermissions("hospital:hospital:edit")
    @Log(title = "医院管理", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(Hospital hospital)
    {
        hospital.setUpdateBy(getLoginName());
        return toAjax(hospitalService.updateHospital(hospital));
    }

    /**
     * 获取所有医院列表（不分页，用于下拉选择等场景）
     * 包含每个医院绑定的供应商数量
     */
    @GetMapping("/all")
    @ResponseBody
    public AjaxResult getAllHospitals()
    {
        try
        {
            Hospital hospital = new Hospital();
            // 不限制状态，获取所有医院（包括正常和停用的）
            // 注意：这里不使用startPage()，直接获取所有数据
            List<Hospital> list = hospitalService.selectHospitalList(hospital);
            
            // 为每个医院添加绑定的供应商数量
            if (list != null && !list.isEmpty())
            {
                for (Hospital h : list)
                {
                    try
                    {
                        HospitalSupplier hospitalSupplier = new HospitalSupplier();
                        hospitalSupplier.setHospitalId(h.getHospitalId());
                        hospitalSupplier.setStatus("0"); // 只统计正常状态的关联
                        List<HospitalSupplier> relations = hospitalSupplierService.selectHospitalSupplierList(hospitalSupplier);
                        // 将供应商数量存储到remark字段（临时使用，或者可以扩展Hospital实体）
                        h.setRemark(relations != null ? String.valueOf(relations.size()) : "0");
                    }
                    catch (Exception e)
                    {
                        // 如果查询关联失败，设置为0
                        h.setRemark("0");
                    }
                }
            }
            
            // 确保返回非null列表
            List<Hospital> resultList = list != null ? list : new ArrayList<>();
            return success(resultList);
        }
        catch (Exception e)
        {
            logger.error("获取所有医院列表失败", e);
            return error("获取医院列表失败：" + e.getMessage());
        }
    }

    /**
     * 选择医院页面
     */
    @RequiresPermissions("hospital:hospital:view")
    @GetMapping("/select")
    public String select()
    {
        return prefix + "/select";
    }

    /**
     * 根据ID列表获取医院信息
     */
    @PostMapping("/getByIds")
    @ResponseBody
    public AjaxResult getHospitalsByIds(String ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return success();
        }
        String[] idArray = ids.split(",");
        List<Hospital> hospitals = new ArrayList<>();
        for (String id : idArray)
        {
            if (!id.isEmpty())
            {
                Hospital hospital = hospitalService.selectHospitalById(Long.parseLong(id));
                if (hospital != null)
                {
                    hospitals.add(hospital);
                }
            }
        }
        return success(hospitals);
    }

    /**
     * 根据供应商ID获取绑定的医院列表
     */
    @GetMapping("/bySupplier/{supplierId}")
    @ResponseBody
    public AjaxResult getHospitalsBySupplierId(@PathVariable("supplierId") Long supplierId)
    {
        // 查询供应商绑定的医院关联关系
        List<HospitalSupplier> relations = hospitalSupplierService.selectHospitalSupplierBySupplierId(supplierId);
        
        // 提取医院ID列表
        List<Long> hospitalIds = new ArrayList<>();
        for (HospitalSupplier relation : relations)
        {
            if (relation.getHospitalId() != null)
            {
                hospitalIds.add(relation.getHospitalId());
            }
        }
        
        // 如果没有绑定的医院，返回空列表
        if (hospitalIds.isEmpty())
        {
            return success(new ArrayList<>());
        }
        
        // 根据医院ID列表查询医院信息
        List<Hospital> hospitals = new ArrayList<>();
        for (Long hospitalId : hospitalIds)
        {
            Hospital hospital = hospitalService.selectHospitalById(hospitalId);
            if (hospital != null && "0".equals(hospital.getStatus()))
            {
                hospitals.add(hospital);
            }
        }
        
        return success(hospitals);
    }

    /**
     * 根据医院ID获取绑定的供应商ID列表
     */
    @GetMapping("/suppliers/{hospitalId}")
    @ResponseBody
    public AjaxResult getSuppliersByHospitalId(@PathVariable("hospitalId") Long hospitalId)
    {
        // 查询医院绑定的供应商关联关系
        HospitalSupplier hospitalSupplier = new HospitalSupplier();
        hospitalSupplier.setHospitalId(hospitalId);
        hospitalSupplier.setStatus("0"); // 只查询正常状态的关联
        List<HospitalSupplier> relations = hospitalSupplierService.selectHospitalSupplierList(hospitalSupplier);
        
        // 提取供应商ID列表
        List<Long> supplierIds = new ArrayList<>();
        for (HospitalSupplier relation : relations)
        {
            if (relation.getSupplierId() != null)
            {
                supplierIds.add(relation.getSupplierId());
            }
        }
        
        return success(supplierIds);
    }
}

