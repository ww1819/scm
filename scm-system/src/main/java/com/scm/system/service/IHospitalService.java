package com.scm.system.service;

import java.util.List;
import com.scm.system.domain.Hospital;

/**
 * 医院信息 服务层
 * 
 * @author scm
 */
public interface IHospitalService
{
    /**
     * 查询医院信息
     * 
     * @param hospitalId 医院ID
     * @return 医院信息
     */
    public Hospital selectHospitalById(Long hospitalId);

    /**
     * 查询医院信息列表
     * 
     * @param hospital 医院信息
     * @return 医院集合
     */
    public List<Hospital> selectHospitalList(Hospital hospital);

    /**
     * 新增医院信息
     * 
     * @param hospital 医院信息
     * @return 结果
     */
    public int insertHospital(Hospital hospital);

    /**
     * 修改医院信息
     * 
     * @param hospital 医院信息
     * @return 结果
     */
    public int updateHospital(Hospital hospital);

    /**
     * 批量删除医院信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteHospitalByIds(String ids);

    /**
     * 删除医院信息
     * 
     * @param hospitalId 医院ID
     * @return 结果
     */
    public int deleteHospitalById(Long hospitalId);

    /**
     * 校验医院编码是否唯一
     * 
     * @param hospital 医院信息
     * @return 结果
     */
    public boolean checkHospitalCodeUnique(Hospital hospital);
}

