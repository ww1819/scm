package com.scm.web.controller.interfacepkg;



import org.apache.shiro.authz.annotation.RequiresPermissions;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.ModelMap;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.multipart.MultipartFile;

import com.scm.common.constant.ScmFileConstants;

import com.scm.common.core.controller.BaseController;

import com.scm.common.core.domain.AjaxResult;

import com.scm.common.core.page.TableDataInfo;

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



    @RequiresPermissions("common:file:upload")

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



    @RequiresPermissions("common:file:list")

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

}

