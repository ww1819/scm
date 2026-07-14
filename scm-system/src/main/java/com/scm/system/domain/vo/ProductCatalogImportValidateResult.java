package com.scm.system.domain.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 产品目录导入校验结果
 */
public class ProductCatalogImportValidateResult
{
    /** 是否全部通过，可执行导入 */
    private boolean pass;

    /** 有效数据行数（不含空行） */
    private int totalCount;

    /** 校验通过、可导入行数 */
    private int validCount;

    /** 问题行数 */
    private int errorCount;

    /** 预计新增条数 */
    private int willInsertCount;

    /** 预计更新条数（勾选更新已存在时） */
    private int willUpdateCount;

    /** 预计跳过条数（已存在且未勾选更新） */
    private int willSkipCount;

    private List<ProductCatalogImportValidateError> errors = new ArrayList<>();

    public boolean isPass()
    {
        return pass;
    }

    public void setPass(boolean pass)
    {
        this.pass = pass;
    }

    public int getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(int totalCount)
    {
        this.totalCount = totalCount;
    }

    public int getValidCount()
    {
        return validCount;
    }

    public void setValidCount(int validCount)
    {
        this.validCount = validCount;
    }

    public int getErrorCount()
    {
        return errorCount;
    }

    public void setErrorCount(int errorCount)
    {
        this.errorCount = errorCount;
    }

    public int getWillInsertCount()
    {
        return willInsertCount;
    }

    public void setWillInsertCount(int willInsertCount)
    {
        this.willInsertCount = willInsertCount;
    }

    public int getWillUpdateCount()
    {
        return willUpdateCount;
    }

    public void setWillUpdateCount(int willUpdateCount)
    {
        this.willUpdateCount = willUpdateCount;
    }

    public int getWillSkipCount()
    {
        return willSkipCount;
    }

    public void setWillSkipCount(int willSkipCount)
    {
        this.willSkipCount = willSkipCount;
    }

    public List<ProductCatalogImportValidateError> getErrors()
    {
        return errors;
    }

    public void setErrors(List<ProductCatalogImportValidateError> errors)
    {
        this.errors = errors;
    }

    public void addError(int rowNum, String materialName, String registerNo, String message)
    {
        ProductCatalogImportValidateError item = new ProductCatalogImportValidateError();
        item.setRowNum(rowNum);
        item.setMaterialName(materialName);
        item.setRegisterNo(registerNo);
        item.setMessage(message);
        errors.add(item);
        errorCount = errors.size();
    }
}
