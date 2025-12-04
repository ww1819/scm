package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.MaterialDict;

/**
 * 物资字典 服务层
 * 
 * @author scm
 */
public interface IMaterialDictService
{
    /**
     * 查询物资字典信息
     * 
     * @param materialId 物资ID
     * @return 物资信息
     */
    public MaterialDict selectMaterialDictById(Long materialId);

    /**
     * 查询物资字典列表
     * 
     * @param materialDict 物资信息
     * @return 物资集合
     */
    public List<MaterialDict> selectMaterialDictList(MaterialDict materialDict);

    /**
     * 新增物资字典信息
     * 
     * @param materialDict 物资信息
     * @return 结果
     */
    public int insertMaterialDict(MaterialDict materialDict);

    /**
     * 修改物资字典信息
     * 
     * @param materialDict 物资信息
     * @return 结果
     */
    public int updateMaterialDict(MaterialDict materialDict);

    /**
     * 批量删除物资字典信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMaterialDictByIds(String ids);

    /**
     * 删除物资字典信息
     * 
     * @param materialId 物资ID
     * @return 结果
     */
    public int deleteMaterialDictById(Long materialId);

    /**
     * 校验产品编码是否唯一
     * 
     * @param materialDict 物资信息
     * @return 结果
     */
    public boolean checkMaterialCodeUnique(MaterialDict materialDict);

    /**
     * 根据产品编码查询物资信息
     * 
     * @param materialCode 产品编码
     * @return 物资信息
     */
    public MaterialDict selectMaterialDictByCode(String materialCode);
}

