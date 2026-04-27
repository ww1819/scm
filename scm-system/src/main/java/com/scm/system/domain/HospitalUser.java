package com.scm.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.scm.common.core.domain.BaseEntity;

/**
 * 医院用户表 scm_hospital_user
 *
 * @author scm
 */
public class HospitalUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long hospitalUserId;

    private Long hospitalId;

    private Long userId;

    private String isMain;

    private String status;

    /** 医院名称（关联查询） */
    private String hospitalName;

    public Long getHospitalUserId()
    {
        return hospitalUserId;
    }

    public void setHospitalUserId(Long hospitalUserId)
    {
        this.hospitalUserId = hospitalUserId;
    }

    public Long getHospitalId()
    {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId)
    {
        this.hospitalId = hospitalId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getIsMain()
    {
        return isMain;
    }

    public void setIsMain(String isMain)
    {
        this.isMain = isMain;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getHospitalName()
    {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("hospitalUserId", getHospitalUserId())
            .append("hospitalId", getHospitalId())
            .append("userId", getUserId())
            .append("isMain", getIsMain())
            .append("status", getStatus())
            .append("hospitalName", getHospitalName())
            .toString();
    }
}
