package com.scm.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.scm.system.domain.Hospital;

/**
 * 医院信息 数据层
 * 
 * @author scm
 */
public interface HospitalMapper
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
     * 根据医院编码查询医院
     * 
     * @param hospitalCode 医院编码
     * @return 医院信息
     */
    public Hospital checkHospitalCodeUnique(String hospitalCode);

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
     * 删除医院信息
     * 
     * @param hospitalId 医院主键
     * @return 结果
     */
    public int deleteHospitalById(Long hospitalId);

    /**
     * 批量删除医院信息
     * 
     * @param hospitalIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteHospitalByIds(String[] hospitalIds);

    /**
     * 查询启用且未删除的医院ID（用于升级补齐）
     */
    public List<Long> selectActiveHospitalIds();

    /**
     * 仅更新首拼简码（批量回填）
     */
    public int updateHospitalPinyinCode(@Param("hospitalId") Long hospitalId, @Param("pinyinCode") String pinyinCode);
}

