package com.scm.common.utils.bean;

import java.io.IOException;
import java.util.Date;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;

/**
 * 兼容多种日期字符串的反序列化（如 yyyy-MM-dd / yyyyMMdd）。
 */
public class FlexibleDateDeserializer extends JsonDeserializer<Date>
{
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        String text = p.getText();
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        text = text.trim();
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        Date date = DateUtils.parseDate(text);
        if (date != null)
        {
            return date;
        }
        return (Date) ctxt.handleWeirdStringValue(Date.class, text,
                "Unsupported date format, expected yyyy-MM-dd or yyyyMMdd");
    }
}
