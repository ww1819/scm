package com.scm.web.controller.interfacepkg;

import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.scm.common.constant.ScmFileConstants;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.page.TableDataInfo;
import com.scm.common.utils.ServletUtils;
import com.scm.common.utils.file.TencentCosService;
import com.scm.system.domain.ScmFile;
import com.scm.system.service.IScmFileService;

/**
 * 腾讯云 COS 文件上传测试
 */
@Controller
@RequestMapping("/interface/cosUpload")
public class CosUploadController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(CosUploadController.class);

    private static final String PREFIX = "interface/cosUpload";

    @Autowired
    private TencentCosService tencentCosService;

    @Autowired
    private IScmFileService scmFileService;

    @RequiresPermissions("interface:cos:view")
    @GetMapping()
    public String index(ModelMap mmap)
    {
        mmap.put("cosStatus", tencentCosService.getConfigStatus());
        return PREFIX;
    }

    @RequiresPermissions(value = { "common:file:upload", "interface:cos:view" }, logical = Logical.OR)
    @PostMapping("/upload")
    @ResponseBody
    public AjaxResult upload(@RequestParam("file") MultipartFile file)
    {
        try
        {
            ScmFile scmFile = scmFileService.uploadToCos(file, ScmFileConstants.MODULE_COS_TEST, getLoginName());
            AjaxResult ajax = AjaxResult.success("上传成功");
            ajax.put("fileId", scmFile.getFileId());
            ajax.put("url", scmFile.getFileUrl());
            ajax.put("fileUrl", scmFile.getFileUrl());
            ajax.put("objectKey", scmFile.getObjectKey());
            ajax.put("originalFilename", scmFile.getOriginalName());
            ajax.put("etag", scmFile.getEtag());
            ajax.put("size", scmFile.getFileSize());
            return ajax;
        }
        catch (Exception e)
        {
            return error(e.getMessage());
        }
    }

    @RequiresPermissions(value = { "common:file:list", "interface:cos:view" }, logical = Logical.OR)
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ScmFile query)
    {
        if (query == null)
        {
            query = new ScmFile();
        }
        query.setSourceModule(ScmFileConstants.MODULE_COS_TEST);
        startPage();
        return getDataTable(scmFileService.selectScmFileList(query));
    }

    /** 返回预签名下载地址（前端新开窗口下载，避免 iframe/fetch 截断） */
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
            log.error("获取 COS 预签名下载地址失败, fileId={}", fileId, e);
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
            log.error("COS 测试页文件下载失败, fileId={}", fileId, e);
            if (!response.isCommitted())
            {
                response.reset();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain;charset=UTF-8");
                ServletUtils.renderString(response, "文件下载失败：" + e.getMessage());
            }
        }
    }
}
