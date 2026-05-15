package com.scm.system.domain.vo;

import java.io.Serializable;

/**
 * 打印预览页用：纸张、方向、字号（已含方向换算后的 @page 宽高）
 */
public class PrintStyleVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** portrait / landscape */
    private String orientation;
    /** 用户录入的纸张宽 mm（竖向时的“宽”） */
    private int paperWidthMm;
    /** 用户录入的纸张高 mm */
    private int paperHeightMm;
    /** CSS @page size 第一维 mm（已按方向换算） */
    private int pageWidthMm;
    /** CSS @page size 第二维 mm */
    private int pageHeightMm;
    private int titleFontPx;
    private int headerFooterFontPx;
    private int contentFontPx;
    /** 横向偏移 mm：负左正右 */
    private int offsetXMm;
    /** 纵向偏移 mm：负上正下 */
    private int offsetYMm;
    /** 每页明细行数 */
    private int rowsPerPage;

    public String getOrientation()
    {
        return orientation;
    }

    public void setOrientation(String orientation)
    {
        this.orientation = orientation;
    }

    public int getPaperWidthMm()
    {
        return paperWidthMm;
    }

    public void setPaperWidthMm(int paperWidthMm)
    {
        this.paperWidthMm = paperWidthMm;
    }

    public int getPaperHeightMm()
    {
        return paperHeightMm;
    }

    public void setPaperHeightMm(int paperHeightMm)
    {
        this.paperHeightMm = paperHeightMm;
    }

    public int getPageWidthMm()
    {
        return pageWidthMm;
    }

    public void setPageWidthMm(int pageWidthMm)
    {
        this.pageWidthMm = pageWidthMm;
    }

    public int getPageHeightMm()
    {
        return pageHeightMm;
    }

    public void setPageHeightMm(int pageHeightMm)
    {
        this.pageHeightMm = pageHeightMm;
    }

    public int getTitleFontPx()
    {
        return titleFontPx;
    }

    public void setTitleFontPx(int titleFontPx)
    {
        this.titleFontPx = titleFontPx;
    }

    public int getHeaderFooterFontPx()
    {
        return headerFooterFontPx;
    }

    public void setHeaderFooterFontPx(int headerFooterFontPx)
    {
        this.headerFooterFontPx = headerFooterFontPx;
    }

    public int getContentFontPx()
    {
        return contentFontPx;
    }

    public void setContentFontPx(int contentFontPx)
    {
        this.contentFontPx = contentFontPx;
    }

    public int getOffsetXMm()
    {
        return offsetXMm;
    }

    public void setOffsetXMm(int offsetXMm)
    {
        this.offsetXMm = offsetXMm;
    }

    public int getOffsetYMm()
    {
        return offsetYMm;
    }

    public void setOffsetYMm(int offsetYMm)
    {
        this.offsetYMm = offsetYMm;
    }

    public int getRowsPerPage()
    {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage)
    {
        this.rowsPerPage = rowsPerPage;
    }
}
