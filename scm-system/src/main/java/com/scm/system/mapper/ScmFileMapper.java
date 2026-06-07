package com.scm.system.mapper;

import java.util.List;
import com.scm.system.domain.ScmFile;

/**
 * 统一文件存储 数据层
 */
public interface ScmFileMapper
{
    ScmFile selectScmFileById(String fileId);

    List<ScmFile> selectScmFileList(ScmFile query);

    int insertScmFile(ScmFile scmFile);

    int deleteScmFileById(String fileId);
}
