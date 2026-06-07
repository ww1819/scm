package com.scm.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.core.domain.BaseEntity;

/**
 * 统一文件存储 scm_file
 */
public class ScmFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private String fileId;

    /** 存储类型 cos/local */
    private String storageType;

    /** 存储桶 */
    private String bucketName;

    /** 地域 */
    private String region;

    /** 对象键 */
    private String objectKey;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名 */
    private String fileName;

    /** 扩展名 */
    private String fileExt;

    /** MIME 类型 */
    private String contentType;

    /** 文件大小 */
    private Long fileSize;

    /** 访问 URL */
    private String fileUrl;

    /** ETag */
    private String etag;

    /** 上传来源模块 */
    private String sourceModule;

    /** 删除标志 */
    private String delFlag;

    public String getFileId()
    {
        return fileId;
    }

    public void setFileId(String fileId)
    {
        this.fileId = fileId;
    }

    public String getStorageType()
    {
        return storageType;
    }

    public void setStorageType(String storageType)
    {
        this.storageType = storageType;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public String getObjectKey()
    {
        return objectKey;
    }

    public void setObjectKey(String objectKey)
    {
        this.objectKey = objectKey;
    }

    public String getOriginalName()
    {
        return originalName;
    }

    public void setOriginalName(String originalName)
    {
        this.originalName = originalName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileExt()
    {
        return fileExt;
    }

    public void setFileExt(String fileExt)
    {
        this.fileExt = fileExt;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getEtag()
    {
        return etag;
    }

    public void setEtag(String etag)
    {
        this.etag = etag;
    }

    public String getSourceModule()
    {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule)
    {
        this.sourceModule = sourceModule;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("fileId", getFileId())
                .append("storageType", getStorageType())
                .append("objectKey", getObjectKey())
                .append("originalName", getOriginalName())
                .append("fileSize", getFileSize())
                .append("sourceModule", getSourceModule())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .toString();
    }
}
