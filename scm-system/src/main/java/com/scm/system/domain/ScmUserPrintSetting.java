package com.scm.system.domain;

import java.util.Date;

/**
 * 用户打印版式设置 scm_user_print_setting（按用户 + 打印类型唯一）
 */
public class ScmUserPrintSetting
{
    private Long settingId;
    private Long userId;
    /** {@link com.scm.system.constants.ScmPrintPageType} */
    private String printType;
    /** portrait / landscape */
    private String orientation;
    private Integer paperWidthMm;
    private Integer paperHeightMm;
    private Integer titleFontPx;
    private Integer headerFooterFontPx;
    private Integer contentFontPx;
    /** 横向偏移 mm：负左正右 */
    private Integer offsetXMm;
    /** 纵向偏移 mm：负上正下 */
    private Integer offsetYMm;
    /** 每页打印明细行数 */
    private Integer rowsPerPage;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;

    public Long getSettingId()
    {
        return settingId;
    }

    public void setSettingId(Long settingId)
    {
        this.settingId = settingId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getPrintType()
    {
        return printType;
    }

    public void setPrintType(String printType)
    {
        this.printType = printType;
    }

    public String getOrientation()
    {
        return orientation;
    }

    public void setOrientation(String orientation)
    {
        this.orientation = orientation;
    }

    public Integer getPaperWidthMm()
    {
        return paperWidthMm;
    }

    public void setPaperWidthMm(Integer paperWidthMm)
    {
        this.paperWidthMm = paperWidthMm;
    }

    public Integer getPaperHeightMm()
    {
        return paperHeightMm;
    }

    public void setPaperHeightMm(Integer paperHeightMm)
    {
        this.paperHeightMm = paperHeightMm;
    }

    public Integer getTitleFontPx()
    {
        return titleFontPx;
    }

    public void setTitleFontPx(Integer titleFontPx)
    {
        this.titleFontPx = titleFontPx;
    }

    public Integer getHeaderFooterFontPx()
    {
        return headerFooterFontPx;
    }

    public void setHeaderFooterFontPx(Integer headerFooterFontPx)
    {
        this.headerFooterFontPx = headerFooterFontPx;
    }

    public Integer getContentFontPx()
    {
        return contentFontPx;
    }

    public void setContentFontPx(Integer contentFontPx)
    {
        this.contentFontPx = contentFontPx;
    }

    public Integer getOffsetXMm()
    {
        return offsetXMm;
    }

    public void setOffsetXMm(Integer offsetXMm)
    {
        this.offsetXMm = offsetXMm;
    }

    public Integer getOffsetYMm()
    {
        return offsetYMm;
    }

    public void setOffsetYMm(Integer offsetYMm)
    {
        this.offsetYMm = offsetYMm;
    }

    public Integer getRowsPerPage()
    {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage)
    {
        this.rowsPerPage = rowsPerPage;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public String getUpdateBy()
    {
        return updateBy;
    }

    public void setUpdateBy(String updateBy)
    {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
}
