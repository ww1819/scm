package com.scm.common.exception.file;

import com.scm.common.exception.base.BaseException;

/**
 * 文件信息异常类
 * 
 * @author scm
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
