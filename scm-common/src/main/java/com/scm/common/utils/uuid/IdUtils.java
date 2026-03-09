package com.scm.common.utils.uuid;

import java.util.concurrent.ThreadLocalRandom;

/**
 * ID生成器工具类
 * 
 * @author scm
 */
public class IdUtils
{
    /**
     * 生成 UUID7 风格主键（时间有序、32位十六进制字符串）
     */
    public static String simpleUuid7()
    {
        long ts = System.currentTimeMillis();
        long r = ThreadLocalRandom.current().nextLong() & 0x0FFFFFFFFFFFFFFFL;
        return String.format("%012x%020x", ts, r);
    }

    /**
     * 获取随机UUID
     * 
     * @return 随机UUID
     */
    public static String randomUUID()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线
     * 
     * @return 简化的UUID，去掉了横线
     */
    public static String simpleUUID()
    {
        return UUID.randomUUID().toString(true);
    }

    /**
     * 获取随机UUID，使用性能更好的ThreadLocalRandom生成UUID
     * 
     * @return 随机UUID
     */
    public static String fastUUID()
    {
        return UUID.fastUUID().toString();
    }

    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     * 
     * @return 简化的UUID，去掉了横线
     */
    public static String fastSimpleUUID()
    {
        return UUID.fastUUID().toString(true);
    }
}
