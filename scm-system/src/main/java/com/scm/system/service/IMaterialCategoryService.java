package com.scm.system.service;

import java.util.List;
import com.scm.common.core.domain.Ztree;
import com.scm.system.domain.MaterialCategory;

/**
 * 耗材分类 服务层
 * 
 * @author scm
 */
public interface IMaterialCategoryService
{
    /**
     * 查询耗材分类信息
     * 
     * @param categoryId 分类ID
     * @return 分类信息
     */
    public MaterialCategory selectMaterialCategoryById(Long categoryId);

    /**
     * 查询耗材分类列表
     * 
     * @param materialCategory 分类信息
     * @return 分类集合
     */
    public List<MaterialCategory> selectMaterialCategoryList(MaterialCategory materialCategory);

    /**
     * 查询耗材分类树列表
     * 
     * @return 分类集合
     */
    public List<Ztree> selectMaterialCategoryTree();

    /**
     * 新增耗材分类信息
     * 
     * @param materialCategory 分类信息
     * @return 结果
     */
    public int insertMaterialCategory(MaterialCategory materialCategory);

    /**
     * 修改耗材分类信息
     * 
     * @param materialCategory 分类信息
     * @return 结果
     */
    public int updateMaterialCategory(MaterialCategory materialCategory);

    /**
     * 批量删除耗材分类信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMaterialCategoryByIds(String ids);

    /**
     * 删除耗材分类信息
     * 
     * @param categoryId 分类ID
     * @return 结果
     */
    public int deleteMaterialCategoryById(Long categoryId);
}

