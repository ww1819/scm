package com.scm.system.service;

import com.scm.system.domain.vo.PrintStyleVO;

/**
 * 用户打印版式设置
 */
public interface IScmUserPrintSettingService
{
    /**
     * 合并默认值与用户保存值，并计算 @page 用宽高
     */
    PrintStyleVO resolvePrintStyle(Long userId, String printType);

    /**
     * 保存或更新（按 user_id + print_type）
     */
    void saveOrUpdate(Long userId, String loginName, String printType, String orientation,
        int paperWidthMm, int paperHeightMm, int titleFontPx, int headerFooterFontPx, int contentFontPx,
        int offsetXMm, int offsetYMm, int rowsPerPage);
}
