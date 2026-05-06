package com.scm.common.profiler;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/**
 * 单次操作内多阶段耗时统计，结束时输出一条汇总日志并标出最慢阶段，便于定位性能瓶颈。
 */
public final class OperationProfiler
{
    private final Logger log;
    private final String operation;
    private final String subject;
    private final long startNanos = System.nanoTime();
    private long lastNanos = startNanos;
    private final List<Segment> segments = new ArrayList<>();

    private static final class Segment
    {
        final String name;
        final double ms;

        Segment(String name, double ms)
        {
            this.name = name;
            this.ms = ms;
        }
    }

    private OperationProfiler(Logger log, String operation, String subject)
    {
        this.log = log;
        this.operation = operation;
        this.subject = subject == null ? "" : subject;
    }

    public static OperationProfiler start(Logger log, String operation, String subject)
    {
        return new OperationProfiler(log, operation, subject);
    }

    /**
     * 记录自上一标记（或开始）到当前时刻的耗时，阶段命名为 {@code segmentName}。
     */
    public void mark(String segmentName)
    {
        long now = System.nanoTime();
        double ms = (now - lastNanos) / 1_000_000.0;
        segments.add(new Segment(segmentName, ms));
        lastNanos = now;
    }

    /**
     * @param infoThresholdMs 总耗时达到或超过该值时打 INFO（含「SLOW」前缀）；否则仅在 DEBUG 下输出。
     */
    public void finish(long infoThresholdMs)
    {
        double totalMs = (System.nanoTime() - startNanos) / 1_000_000.0;
        Segment slowest = null;
        for (Segment s : segments)
        {
            if (slowest == null || s.ms > slowest.ms)
            {
                slowest = s;
            }
        }
        StringBuilder sb = new StringBuilder(160);
        if (totalMs >= infoThresholdMs)
        {
            sb.append("SLOW ");
        }
        sb.append("[perf] ").append(operation);
        if (!subject.isEmpty())
        {
            sb.append(" subject=").append(subject);
        }
        sb.append(String.format(" total=%.1fms", totalMs));
        if (slowest != null)
        {
            sb.append(String.format(" slowest=%s(%.1fms)", slowest.name, slowest.ms));
        }
        if (!segments.isEmpty())
        {
            sb.append(" stages=");
            for (int i = 0; i < segments.size(); i++)
            {
                if (i > 0)
                {
                    sb.append(',');
                }
                Segment s = segments.get(i);
                sb.append(String.format("%s:%.1fms", s.name, s.ms));
            }
        }
        String line = sb.toString();
        if (totalMs >= infoThresholdMs)
        {
            log.info(line);
        }
        else if (log.isDebugEnabled())
        {
            log.debug(line);
        }
    }
}
