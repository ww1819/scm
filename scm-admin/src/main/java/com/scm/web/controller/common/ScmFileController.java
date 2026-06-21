package com.scm.web.controller.common;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.utils.ServletUtils;
import com.scm.system.domain.ScmFile;
import com.scm.system.service.IScmFileService;

/**
 * 统一文件上传/下载（业务功能通过 file_id 关联 scm_file）
 */
@Controller
@RequestMapping("/common/file")
public class ScmFileController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(ScmFileController.class);

    @Autowired
    private IScmFileService scmFileService;

    @RequiresPermissions("common:file:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ScmFile query)
    {
        startPage();
        List<ScmFile> list = scmFileService.selectScmFileList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("common:file:upload")
    @PostMapping("/upload")
    @ResponseBody
    public AjaxResult upload(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "sourceModule", required = false) String sourceModule)
    {
        try
        {
            ScmFile scmFile = scmFileService.uploadToCos(file, sourceModule, getLoginName());
            AjaxResult ajax = AjaxResult.success("上传成功");
            ajax.put("fileId", scmFile.getFileId());
            ajax.put("fileUrl", scmFile.getFileUrl());
            ajax.put("objectKey", scmFile.getObjectKey());
            ajax.put("originalFilename", scmFile.getOriginalName());
            ajax.put("etag", scmFile.getEtag());
            ajax.put("size", scmFile.getFileSize());
            return ajax;
        }
        catch (Exception e)
        {
            log.error("文件上传失败", e);
            return error(e.getMessage());
        }
    }

    @RequiresPermissions(value = { "common:file:download", "interface:cos:view" }, logical = Logical.OR)
    @GetMapping("/downloadUrl/{fileId}")
    @ResponseBody
    public AjaxResult downloadUrl(@PathVariable("fileId") String fileId)
    {
        try
        {
            String url = scmFileService.getPresignedDownloadUrl(fileId);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            return ajax;
        }
        catch (Exception e)
        {
            log.error("获取预签名下载地址失败, fileId={}", fileId, e);
            return error(e.getMessage());
        }
    }

    @RequiresPermissions(value = { "common:file:download", "interface:cos:view" }, logical = Logical.OR)
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable("fileId") String fileId, HttpServletResponse response)
    {
        try
        {
            scmFileService.download(fileId, response);
        }
        catch (Exception e)
        {
            log.error("文件下载失败, fileId={}", fileId, e);
            if (!response.isCommitted())
            {
                response.reset();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain;charset=UTF-8");
                ServletUtils.renderString(response, "文件下载失败：" + e.getMessage());
            }
        }
    }

    @RequiresPermissions("common:file:list")
    @GetMapping("/{fileId}")
    @ResponseBody
    public AjaxResult info(@PathVariable("fileId") String fileId)
    {
        ScmFile scmFile = scmFileService.selectScmFileById(fileId);
        if (scmFile == null)
        {
            return error("文件不存在");
        }
        return AjaxResult.success(scmFile);
    }
}
