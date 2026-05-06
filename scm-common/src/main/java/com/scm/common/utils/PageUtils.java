package com.scm.common.utils;

import java.util.function.Supplier;
import com.github.pagehelper.PageHelper;
import com.scm.common.core.page.PageDomain;
import com.scm.common.core.page.TableSupport;
import com.scm.common.utils.sql.SqlUtil;

/**
 * 分页工具类
 * 
 * @author scm
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }

    /**
     * 在已存在 PageHelper 分页上下文时，执行一段不应被分页/排序（如 expire_date）污染的 SQL。
     * 典型场景：Controller 已 startPage()，Service 在查主列表前需先查供应商用户、医院关联等辅助表。
     */
    public static <T> T callWithoutPaging(Supplier<T> supplier)
    {
        if (PageHelper.getLocalPage() == null)
        {
            return supplier.get();
        }
        PageHelper.clearPage();
        try
        {
            return supplier.get();
        }
        finally
        {
            startPage();
        }
    }
}
