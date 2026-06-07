package com.scm.system.service;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import com.scm.system.domain.ScmFile;

/**
 * 统一文件存储 服务层
 */
public interface IScmFileService
{
    ScmFile selectScmFileById(String fileId);

    List<ScmFile> selectScmFileList(ScmFile query);

    /**
     * 上传文件到 COS 并写入 scm_file
     */
    ScmFile uploadToCos(MultipartFile file, String sourceModule, String createBy) throws Exception;

    /**
     * 按 fileId 下载文件
     */
    void download(String fileId, HttpServletResponse response) throws Exception;
}
