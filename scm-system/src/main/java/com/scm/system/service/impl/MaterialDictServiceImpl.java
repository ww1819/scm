package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.MaterialDict;
import com.scm.system.mapper.MaterialDictMapper;
import com.scm.system.service.IMaterialDictService;

/**
 * 物资字典 服务层实现
 * 
 * @author scm
 */
@Service
public class MaterialDictServiceImpl implements IMaterialDictService
{
    @Autowired
    private MaterialDictMapper materialDictMapper;

    /**
     * 查询物资字典信息
     * 
     * @param materialId 物资ID
     * @return 物资信息
     */
    @Override
    public MaterialDict selectMaterialDictById(Long materialId)
    {
        return materialDictMapper.selectMaterialDictById(materialId);
    }

    /**
     * 查询物资字典列表
     * 
     * @param materialDict 物资信息
     * @return 物资集合
     */
    @Override
    public List<MaterialDict> selectMaterialDictList(MaterialDict materialDict)
    {
        return materialDictMapper.selectMaterialDictList(materialDict);
    }

    /**
     * 新增物资字典信息
     * 
     * @param materialDict 物资信息
     * @return 结果
     */
    @Override
    public int insertMaterialDict(MaterialDict materialDict)
    {
        if (StringUtils.isEmpty(materialDict.getStatus()))
        {
            materialDict.setStatus("0");
        }
        // 如果产品编码为空，自动生成唯一编码
        if (StringUtils.isEmpty(materialDict.getMaterialCode()))
        {
            materialDict.setMaterialCode(generateMaterialCode());
        }
        materialDict.setCreateTime(DateUtils.getNowDate());
        return materialDictMapper.insertMaterialDict(materialDict);
    }

    /**
     * 生成唯一的产品编码
     * 
     * @return 产品编码
     */
    private String generateMaterialCode()
    {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do
        {
            // 使用时间戳+随机数生成编码
            code = "MAT" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            if (code.length() > 50)
            {
                code = code.substring(0, 50);
            }
            attempt++;
        }
        while (materialDictMapper.checkMaterialCodeUnique(code) != null && attempt < maxAttempts);
        
        if (attempt >= maxAttempts)
        {
            // 如果10次尝试都失败，使用UUID
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            code = "MAT" + uuid.substring(0, Math.min(20, uuid.length()));
        }
        
        return code;
    }

    /**
     * 修改物资字典信息
     * 
     * @param materialDict 物资信息
     * @return 结果
     */
    @Override
    public int updateMaterialDict(MaterialDict materialDict)
    {
        materialDict.setUpdateTime(DateUtils.getNowDate());
        return materialDictMapper.updateMaterialDict(materialDict);
    }

    /**
     * 批量删除物资字典信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteMaterialDictByIds(String ids)
    {
        return materialDictMapper.deleteMaterialDictByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除物资字典信息
     * 
     * @param materialId 物资ID
     * @return 结果
     */
    @Override
    public int deleteMaterialDictById(Long materialId)
    {
        return materialDictMapper.deleteMaterialDictById(materialId);
    }

    /**
     * 校验产品编码是否唯一
     * 
     * @param materialDict 物资信息
     * @return 结果
     */
    @Override
    public boolean checkMaterialCodeUnique(MaterialDict materialDict)
    {
        Long materialId = StringUtils.isNull(materialDict.getMaterialId()) ? -1L : materialDict.getMaterialId();
        MaterialDict info = materialDictMapper.checkMaterialCodeUnique(materialDict.getMaterialCode());
        if (StringUtils.isNotNull(info) && info.getMaterialId().longValue() != materialId.longValue())
        {
            return false;
        }
        return true;
    }

    /**
     * 根据产品编码查询物资信息
     * 
     * @param materialCode 产品编码
     * @return 物资信息
     */
    @Override
    public MaterialDict selectMaterialDictByCode(String materialCode)
    {
        return materialDictMapper.checkMaterialCodeUnique(materialCode);
    }
}

