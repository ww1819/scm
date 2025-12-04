package com.scm.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.scm.common.core.text.Convert;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Hospital;
import com.scm.system.mapper.HospitalMapper;
import com.scm.system.service.IHospitalService;

/**
 * 医院信息 服务层实现
 * 
 * @author scm
 */
@Service
public class HospitalServiceImpl implements IHospitalService
{
    @Autowired
    private HospitalMapper hospitalMapper;

    /**
     * 查询医院信息
     * 
     * @param hospitalId 医院ID
     * @return 医院信息
     */
    @Override
    public Hospital selectHospitalById(Long hospitalId)
    {
        return hospitalMapper.selectHospitalById(hospitalId);
    }

    /**
     * 查询医院信息列表
     * 
     * @param hospital 医院信息
     * @return 医院集合
     */
    @Override
    public List<Hospital> selectHospitalList(Hospital hospital)
    {
        return hospitalMapper.selectHospitalList(hospital);
    }

    /**
     * 新增医院信息
     * 
     * @param hospital 医院信息
     * @return 结果
     */
    @Override
    public int insertHospital(Hospital hospital)
    {
        if (StringUtils.isEmpty(hospital.getStatus()))
        {
            hospital.setStatus("0"); // 默认正常
        }
        // 如果医院编码为空，自动生成唯一编码
        if (StringUtils.isEmpty(hospital.getHospitalCode()))
        {
            hospital.setHospitalCode(generateHospitalCode());
        }
        hospital.setCreateTime(DateUtils.getNowDate());
        return hospitalMapper.insertHospital(hospital);
    }

    /**
     * 生成唯一的医院编码
     * 
     * @return 医院编码
     */
    private String generateHospitalCode()
    {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do
        {
            // 使用时间戳+随机数生成编码
            code = "H" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            if (code.length() > 50)
            {
                code = code.substring(0, 50);
            }
            attempt++;
        }
        while (hospitalMapper.checkHospitalCodeUnique(code) != null && attempt < maxAttempts);
        
        if (attempt >= maxAttempts)
        {
            // 如果10次尝试都失败，使用UUID
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            code = "H" + uuid.substring(0, Math.min(20, uuid.length()));
        }
        
        return code;
    }

    /**
     * 修改医院信息
     * 
     * @param hospital 医院信息
     * @return 结果
     */
    @Override
    public int updateHospital(Hospital hospital)
    {
        hospital.setUpdateTime(DateUtils.getNowDate());
        return hospitalMapper.updateHospital(hospital);
    }

    /**
     * 批量删除医院信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteHospitalByIds(String ids)
    {
        return hospitalMapper.deleteHospitalByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除医院信息
     * 
     * @param hospitalId 医院ID
     * @return 结果
     */
    @Override
    public int deleteHospitalById(Long hospitalId)
    {
        return hospitalMapper.deleteHospitalById(hospitalId);
    }

    /**
     * 校验医院编码是否唯一
     * 
     * @param hospital 医院信息
     * @return 结果
     */
    @Override
    public boolean checkHospitalCodeUnique(Hospital hospital)
    {
        Long hospitalId = StringUtils.isNull(hospital.getHospitalId()) ? -1L : hospital.getHospitalId();
        Hospital info = hospitalMapper.checkHospitalCodeUnique(hospital.getHospitalCode());
        if (StringUtils.isNotNull(info) && info.getHospitalId().longValue() != hospitalId.longValue())
        {
            return false;
        }
        return true;
    }
}

