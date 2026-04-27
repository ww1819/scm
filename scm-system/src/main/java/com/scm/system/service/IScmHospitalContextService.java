package com.scm.system.service;

/**
 * 解析当前登录用户关联的医院上下文
 */
public interface IScmHospitalContextService
{
    /**
     * 从 scm_hospital_user 解析（主账号优先），无关联返回 null
     */
    Long resolveHospitalIdForUser(Long userId);
}
