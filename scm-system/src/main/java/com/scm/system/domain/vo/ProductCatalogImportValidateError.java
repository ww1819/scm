package com.scm.system.domain.vo;

/**
 * 产品目录导入校验错误项
 */
public class ProductCatalogImportValidateError
{
    /** Excel 行号（从 1 起，含表头） */
    private int rowNum;

    private String materialName;

    private String registerNo;

    private String message;

    public int getRowNum()
    {
        return rowNum;
    }

    public void setRowNum(int rowNum)
    {
        this.rowNum = rowNum;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getRegisterNo()
    {
        return registerNo;
    }

    public void setRegisterNo(String registerNo)
    {
        this.registerNo = registerNo;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
