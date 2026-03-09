package com.scm.common.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具（客户名拼音简码等）
 */
public final class PinyinUtils
{
    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static
    {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    private PinyinUtils() { }

    /**
     * 获取中文名称的拼音简码（首字母）
     */
    public static String getShortCode(String name)
    {
        if (name == null || name.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray())
        {
            if (Character.toString(c).matches("[\\u4e00-\\u9fa5]"))
            {
                try
                {
                    String[] py = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                    if (py != null && py.length > 0) sb.append(py[0].charAt(0));
                }
                catch (BadHanyuPinyinOutputFormatCombination e) { }
            }
            else if (Character.isLetterOrDigit(c))
                sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}
