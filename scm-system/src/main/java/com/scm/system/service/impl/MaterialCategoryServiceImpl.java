package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.domain.Ztree;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.MaterialCategory;
import com.scm.system.mapper.MaterialCategoryMapper;
import com.scm.system.service.IMaterialCategoryService;

/**
 * 耗材分类 服务层实现
 * 
 * @author scm
 */
@Service
public class MaterialCategoryServiceImpl implements IMaterialCategoryService
{
    @Autowired
    private MaterialCategoryMapper materialCategoryMapper;

    /**
     * 查询耗材分类信息
     * 
     * @param categoryId 分类ID
     * @return 分类信息
     */
    @Override
    public MaterialCategory selectMaterialCategoryById(Long categoryId)
    {
        return materialCategoryMapper.selectMaterialCategoryById(categoryId);
    }

    /**
     * 查询耗材分类列表
     * 
     * @param materialCategory 分类信息
     * @return 分类集合
     */
    @Override
    public List<MaterialCategory> selectMaterialCategoryList(MaterialCategory materialCategory)
    {
        return materialCategoryMapper.selectMaterialCategoryList(materialCategory);
    }

    /**
     * 查询耗材分类树列表
     * 
     * @return 分类集合
     */
    @Override
    public List<Ztree> selectMaterialCategoryTree()
    {
        List<MaterialCategory> categoryList = materialCategoryMapper.selectMaterialCategoryTree();
        List<Ztree> ztrees = initZtree(categoryList);
        return ztrees;
    }

    /**
     * 对象转分类树
     *
     * @param categoryList 分类列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<MaterialCategory> categoryList)
    {
        List<Ztree> ztrees = new ArrayList<Ztree>();
        for (MaterialCategory category : categoryList)
        {
            if ("0".equals(category.getStatus()))
            {
                Ztree ztree = new Ztree();
                ztree.setId(category.getCategoryId());
                ztree.setpId(category.getParentId());
                ztree.setName(category.getCategoryName());
                ztree.setTitle(category.getCategoryName());
                ztrees.add(ztree);
            }
        }
        return ztrees;
    }

    /**
     * 新增耗材分类信息
     * 
     * @param materialCategory 分类信息
     * @return 结果
     */
    @Override
    public int insertMaterialCategory(MaterialCategory materialCategory)
    {
        MaterialCategory info = materialCategoryMapper.selectMaterialCategoryById(materialCategory.getParentId());
        if (StringUtils.isNotNull(info))
        {
            materialCategory.setAncestors(info.getAncestors() + "," + materialCategory.getParentId());
        }
        else
        {
            materialCategory.setAncestors("0");
        }
        if (StringUtils.isEmpty(materialCategory.getStatus()))
        {
            materialCategory.setStatus("0");
        }
        materialCategory.setCreateTime(DateUtils.getNowDate());
        return materialCategoryMapper.insertMaterialCategory(materialCategory);
    }

    /**
     * 修改耗材分类信息
     * 
     * @param materialCategory 分类信息
     * @return 结果
     */
    @Override
    public int updateMaterialCategory(MaterialCategory materialCategory)
    {
        MaterialCategory newParentCategory = materialCategoryMapper.selectMaterialCategoryById(materialCategory.getParentId());
        MaterialCategory oldCategory = materialCategoryMapper.selectMaterialCategoryById(materialCategory.getCategoryId());
        if (StringUtils.isNotNull(newParentCategory) && StringUtils.isNotNull(oldCategory))
        {
            String newAncestors = newParentCategory.getAncestors() + "," + newParentCategory.getCategoryId();
            String oldAncestors = oldCategory.getAncestors();
            materialCategory.setAncestors(newAncestors);
            updateCategoryChildren(materialCategory.getCategoryId(), newAncestors, oldAncestors);
        }
        materialCategory.setUpdateTime(DateUtils.getNowDate());
        return materialCategoryMapper.updateMaterialCategory(materialCategory);
    }

    /**
     * 修改子元素关系
     * 
     * @param categoryId 被修改的分类ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateCategoryChildren(Long categoryId, String newAncestors, String oldAncestors)
    {
        List<MaterialCategory> children = materialCategoryMapper.selectMaterialCategoryByParentId(categoryId);
        for (MaterialCategory child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0)
        {
            for (MaterialCategory child : children)
            {
                updateCategoryChildren(child.getCategoryId(), child.getAncestors(), oldAncestors);
            }
        }
    }

    /**
     * 批量删除耗材分类信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteMaterialCategoryByIds(String ids)
    {
        return materialCategoryMapper.deleteMaterialCategoryByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除耗材分类信息
     * 
     * @param categoryId 分类ID
     * @return 结果
     */
    @Override
    public int deleteMaterialCategoryById(Long categoryId)
    {
        return materialCategoryMapper.deleteMaterialCategoryById(categoryId);
    }
}

