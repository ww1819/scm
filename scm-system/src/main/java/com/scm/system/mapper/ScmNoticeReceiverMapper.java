package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmNoticeReceiver;

/**
 * 通知接收人 Mapper
 */
public interface ScmNoticeReceiverMapper
{
    int batchInsert(List<ScmNoticeReceiver> rows);
}
