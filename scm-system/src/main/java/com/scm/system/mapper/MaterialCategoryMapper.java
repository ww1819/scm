package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.MaterialCategory;

/**
 * 耗材分类 数据层
 * 
 * @author scm
 */
public interface MaterialCategoryMapper
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
    public List<MaterialCategory> selectMaterialCategoryTree();

    /**
     * 根据父ID查询分类
     * 
     * @param parentId 父分类ID
     * @return 分类集合
     */
    public List<MaterialCategory> selectMaterialCategoryByParentId(Long parentId);

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
     * 删除耗材分类信息
     * 
     * @param categoryId 分类主键
     * @return 结果
     */
    public int deleteMaterialCategoryById(Long categoryId);

    /**
     * 批量删除耗材分类信息
     * 
     * @param categoryIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteMaterialCategoryByIds(String[] categoryIds);
}

