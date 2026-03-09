package com.scm.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.utils.uuid.IdUtils;
import com.scm.system.domain.ScmTenantMenuPause;
import com.scm.system.domain.ScmTenantMenuPauseLog;
import com.scm.system.domain.ScmTenantMenuPauseLogVo;
import com.scm.system.domain.ScmTenantMenuPauseManageVo;
import com.scm.system.mapper.ScmTenantMenuMapper;
import com.scm.system.mapper.ScmTenantMenuPauseLogMapper;
import com.scm.system.mapper.ScmTenantMenuPauseMapper;
import com.scm.system.service.IScmTenantMenuPauseService;

@Service
public class ScmTenantMenuPauseServiceImpl implements IScmTenantMenuPauseService
{
    @Autowired
    private ScmTenantMenuPauseMapper pauseMapper;
    @Autowired
    private ScmTenantMenuPauseLogMapper logMapper;
    @Autowired
    private ScmTenantMenuMapper scmTenantMenuMapper;

    @Override
    public List<ScmTenantMenuPauseManageVo> listMenusWithStatusByTenantId(String tenantId)
    {
        return scmTenantMenuMapper.selectListWithMenuNameAndPauseByTenantId(tenantId);
    }

    @Override
    public List<ScmTenantMenuPause> selectByTenantId(String tenantId)
    {
        return pauseMapper.selectByTenantId(tenantId);
    }

    @Override
    public List<ScmTenantMenuPauseLog> selectPauseLogsByTenantId(String tenantId)
    {
        return logMapper.selectByTenantId(tenantId);
    }

    @Override
    public List<ScmTenantMenuPauseLogVo> listPauseLogsWithMenuNameByTenantId(String tenantId)
    {
        return logMapper.selectByTenantIdWithMenuName(tenantId);
    }

    @Override
    public List<Long> selectPausedMenuIdsByTenantId(String tenantId)
    {
        return pauseMapper.selectPausedMenuIdsByTenantId(tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pauseMenu(String tenantId, Long menuId, String operBy, String remark)
    {
        if (tenantId == null || menuId == null) return 0;
        List<Long> allowed = scmTenantMenuMapper.selectMenuIdsByTenantId(tenantId);
        if (allowed == null || !allowed.contains(menuId)) return 0;
        Date now = new Date();
        ScmTenantMenuPause p = pauseMapper.selectByTenantAndMenu(tenantId, menuId);
        if (p != null)
        {
            if ("1".equals(p.getPauseStatus())) return 1; // 已暂停
            p.setPauseStatus("1");
            p.setPauseTime(now);
            p.setUpdateBy(operBy);
            pauseMapper.update(p);
        }
        else
        {
            p = new ScmTenantMenuPause();
            p.setPauseId(IdUtils.simpleUuid7());
            p.setTenantId(tenantId);
            p.setMenuId(menuId);
            p.setPauseStatus("1");
            p.setPauseTime(now);
            p.setCreateBy(operBy);
            pauseMapper.insert(p);
        }
        ScmTenantMenuPauseLog log = new ScmTenantMenuPauseLog();
        log.setLogId(IdUtils.simpleUuid7());
        log.setPauseId(p.getPauseId());
        log.setTenantId(tenantId);
        log.setMenuId(menuId);
        log.setAction("1");
        log.setOperBy(operBy);
        log.setOperTime(now);
        log.setRemark(remark);
        logMapper.insert(log);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeMenu(String tenantId, Long menuId, String operBy, String remark)
    {
        if (tenantId == null || menuId == null) return 0;
        ScmTenantMenuPause p = pauseMapper.selectByTenantAndMenu(tenantId, menuId);
        if (p == null) return 1;
        if ("0".equals(p.getPauseStatus())) return 1;
        p.setPauseStatus("0");
        p.setPauseTime(null);
        p.setUpdateBy(operBy);
        pauseMapper.update(p);
        Date now = new Date();
        ScmTenantMenuPauseLog log = new ScmTenantMenuPauseLog();
        log.setLogId(IdUtils.simpleUuid7());
        log.setPauseId(p.getPauseId());
        log.setTenantId(tenantId);
        log.setMenuId(menuId);
        log.setAction("0");
        log.setOperBy(operBy);
        log.setOperTime(now);
        log.setRemark(remark);
        logMapper.insert(log);
        return 1;
    }
}
