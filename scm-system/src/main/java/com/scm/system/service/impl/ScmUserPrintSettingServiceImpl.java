package com.scm.system.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.utils.StringUtils;
import com.scm.system.constants.ScmPrintPageType;
import com.scm.system.domain.ScmUserPrintSetting;
import com.scm.system.domain.vo.PrintStyleVO;
import com.scm.system.mapper.ScmUserPrintSettingMapper;
import com.scm.system.service.IScmUserPrintSettingService;

@Service
public class ScmUserPrintSettingServiceImpl implements IScmUserPrintSettingService
{
    @Autowired
    private ScmUserPrintSettingMapper scmUserPrintSettingMapper;

    private static ScmUserPrintSetting defaultsFor(String printType)
    {
        ScmUserPrintSetting d = new ScmUserPrintSetting();
        d.setPrintType(printType);
        d.setOrientation("portrait");
        if (ScmPrintPageType.DELIVERY.equals(printType))
        {
            d.setPaperWidthMm(200);
            d.setPaperHeightMm(130);
            d.setTitleFontPx(15);
            d.setHeaderFooterFontPx(10);
            d.setContentFontPx(8);
            d.setOffsetXMm(0);
            d.setOffsetYMm(0);
        }
        else
        {
            d.setPaperWidthMm(210);
            d.setPaperHeightMm(297);
            d.setTitleFontPx(22);
            d.setHeaderFooterFontPx(12);
            d.setContentFontPx(9);
            d.setOffsetXMm(0);
            d.setOffsetYMm(0);
        }
        d.setRowsPerPage(10);
        return d;
    }

    private static PrintStyleVO toView(ScmUserPrintSetting s)
    {
        PrintStyleVO vo = new PrintStyleVO();
        vo.setOrientation(s.getOrientation() != null ? s.getOrientation() : "portrait");
        vo.setPaperWidthMm(s.getPaperWidthMm() != null ? s.getPaperWidthMm() : 210);
        vo.setPaperHeightMm(s.getPaperHeightMm() != null ? s.getPaperHeightMm() : 297);
        vo.setTitleFontPx(s.getTitleFontPx() != null ? s.getTitleFontPx() : 22);
        vo.setHeaderFooterFontPx(s.getHeaderFooterFontPx() != null ? s.getHeaderFooterFontPx() : 12);
        vo.setContentFontPx(s.getContentFontPx() != null ? s.getContentFontPx() : 9);
        vo.setOffsetXMm(s.getOffsetXMm() != null ? s.getOffsetXMm() : 0);
        vo.setOffsetYMm(s.getOffsetYMm() != null ? s.getOffsetYMm() : 0);
        vo.setRowsPerPage(s.getRowsPerPage() != null ? s.getRowsPerPage() : 10);
        return vo;
    }

    private static void clampAndNormalize(ScmUserPrintSetting s)
    {
        if (!"landscape".equalsIgnoreCase(StringUtils.trim(s.getOrientation())))
        {
            s.setOrientation("portrait");
        }
        else
        {
            s.setOrientation("landscape");
        }
        int pw = s.getPaperWidthMm() != null ? s.getPaperWidthMm() : 210;
        int ph = s.getPaperHeightMm() != null ? s.getPaperHeightMm() : 297;
        s.setPaperWidthMm(clamp(pw, 50, 500));
        s.setPaperHeightMm(clamp(ph, 50, 500));
        s.setTitleFontPx(clamp(s.getTitleFontPx() != null ? s.getTitleFontPx() : 22, 6, 72));
        s.setHeaderFooterFontPx(clamp(s.getHeaderFooterFontPx() != null ? s.getHeaderFooterFontPx() : 12, 6, 48));
        s.setContentFontPx(clamp(s.getContentFontPx() != null ? s.getContentFontPx() : 9, 5, 36));
        s.setOffsetXMm(clamp(s.getOffsetXMm() != null ? s.getOffsetXMm() : 0, -100, 100));
        s.setOffsetYMm(clamp(s.getOffsetYMm() != null ? s.getOffsetYMm() : 0, -100, 100));
        s.setRowsPerPage(clamp(s.getRowsPerPage() != null ? s.getRowsPerPage() : 10, 1, 200));
    }

    private static int clamp(int v, int min, int max)
    {
        return Math.min(max, Math.max(min, v));
    }

    @Override
    public PrintStyleVO resolvePrintStyle(Long userId, String printType)
    {
        ScmUserPrintSetting merged = defaultsFor(printType);
        if (userId != null)
        {
            ScmUserPrintSetting row = scmUserPrintSettingMapper.selectByUserIdAndPrintType(userId, printType);
            if (row != null)
            {
                if (StringUtils.isNotEmpty(row.getOrientation()))
                {
                    merged.setOrientation(row.getOrientation());
                }
                if (row.getPaperWidthMm() != null)
                {
                    merged.setPaperWidthMm(row.getPaperWidthMm());
                }
                if (row.getPaperHeightMm() != null)
                {
                    merged.setPaperHeightMm(row.getPaperHeightMm());
                }
                if (row.getTitleFontPx() != null)
                {
                    merged.setTitleFontPx(row.getTitleFontPx());
                }
                if (row.getHeaderFooterFontPx() != null)
                {
                    merged.setHeaderFooterFontPx(row.getHeaderFooterFontPx());
                }
                if (row.getContentFontPx() != null)
                {
                    merged.setContentFontPx(row.getContentFontPx());
                }
                if (row.getOffsetXMm() != null)
                {
                    merged.setOffsetXMm(row.getOffsetXMm());
                }
                if (row.getOffsetYMm() != null)
                {
                    merged.setOffsetYMm(row.getOffsetYMm());
                }
                if (row.getRowsPerPage() != null)
                {
                    merged.setRowsPerPage(row.getRowsPerPage());
                }
            }
        }
        clampAndNormalize(merged);
        return toView(merged);
    }

    @Override
    public void saveOrUpdate(Long userId, String loginName, String printType, String orientation,
        int paperWidthMm, int paperHeightMm, int titleFontPx, int headerFooterFontPx, int contentFontPx,
        int offsetXMm, int offsetYMm, int rowsPerPage)
    {
        ScmUserPrintSetting s = new ScmUserPrintSetting();
        s.setUserId(userId);
        s.setPrintType(printType);
        s.setOrientation(orientation);
        s.setPaperWidthMm(paperWidthMm);
        s.setPaperHeightMm(paperHeightMm);
        s.setTitleFontPx(titleFontPx);
        s.setHeaderFooterFontPx(headerFooterFontPx);
        s.setContentFontPx(contentFontPx);
        s.setOffsetXMm(offsetXMm);
        s.setOffsetYMm(offsetYMm);
        s.setRowsPerPage(rowsPerPage);
        clampAndNormalize(s);
        Date now = new Date();
        ScmUserPrintSetting existing = scmUserPrintSettingMapper.selectByUserIdAndPrintType(userId, printType);
        if (existing == null)
        {
            s.setCreateBy(loginName);
            s.setCreateTime(now);
            s.setUpdateBy(loginName);
            s.setUpdateTime(now);
            scmUserPrintSettingMapper.insertScmUserPrintSetting(s);
        }
        else
        {
            s.setUpdateBy(loginName);
            s.setUpdateTime(now);
            scmUserPrintSettingMapper.updateScmUserPrintSetting(s);
        }
    }
}
