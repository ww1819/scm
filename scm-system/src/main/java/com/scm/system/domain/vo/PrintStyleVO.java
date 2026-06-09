package com.scm.system.domain.vo;

import java.io.Serializable;
import com.scm.common.utils.StringUtils;

/**
 * 打印预览页用：用户保存的纸张宽高、方向、字号等。
 * 横向时 {@link #getPrintWidthMm()} / {@link #getPrintHeightMm()} 与用户录入的宽高互换。
 */
public class PrintStyleVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** portrait / landscape */
    private String orientation;
    /** 用户设置的纸张宽 mm */
    private int paperWidthMm;
    /** 用户设置的纸张高 mm */
    private int paperHeightMm;
    private int titleFontPx;
    private int headerFooterFontPx;
    private int contentFontPx;
    /** 横向偏移 mm：负左正右 */
    private int offsetXMm;
    /** 纵向偏移 mm：负上正下 */
    private int offsetYMm;
    /** 每页明细行数 */
    private int rowsPerPage;

    public boolean isLandscape()
    {
        return "landscape".equalsIgnoreCase(StringUtils.trim(orientation));
    }

    /** 实际排版/打印宽度 mm：竖向=纸宽，横向=纸高 */
    public int getPrintWidthMm()
    {
        return isLandscape() ? paperHeightMm : paperWidthMm;
    }

    /** 实际排版/打印高度 mm：竖向=纸高，横向=纸宽 */
    public int getPrintHeightMm()
    {
        return isLandscape() ? paperWidthMm : paperHeightMm;
    }

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
