package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.UserConstants;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.security.Md5Utils;
import com.scm.system.domain.SysUserRole;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysUserMapper;
import com.scm.system.mapper.SysUserRoleMapper;
import com.scm.system.service.ITenantAdminCreateService;

/**
 * 客户新增后自动创建医院管理员角色与首字母用户
 */
@Service
public class TenantAdminCreateServiceImpl implements ITenantAdminCreateService
{
    private static final String ROLE_KEY_HOSPITAL_ADMIN = "hospital_admin";
    private static final String DEFAULT_PWD = "123456";

    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureHospitalAdminRoleAndUser(String tenantId, String tenantName, String pinyinCode, String operBy)
    {
        if (StringUtils.isEmpty(tenantId)) return;
        String name = StringUtils.isNotEmpty(tenantName) ? tenantName : "客户";
        String code = StringUtils.isNotEmpty(pinyinCode) ? pinyinCode : "kh";

        // 1) 查询该租户下医院管理员角色
        SysRole role = roleMapper.selectByRoleKeyAndTenantId(ROLE_KEY_HOSPITAL_ADMIN, tenantId);
        if (role == null)
        {
            role = new SysRole();
            role.setRoleName("医院管理员");
            role.setRoleKey(ROLE_KEY_HOSPITAL_ADMIN);
            role.setRoleSort("2");
            role.setDataScope("1");
            role.setStatus("0");
            role.setRemark("客户[" + name + "]默认角色");
            role.setTenantId(tenantId);
            role.setCreateBy(operBy);
            roleMapper.insertRole(role);
        }
        Long roleId = role.getRoleId();

        // 2) 该租户下是否已有拥有此角色的用户
        List<SysUser> tenantUsers = userMapper.selectUserListByTenantId(tenantId);
        for (SysUser u : tenantUsers)
        {
            if (u.getRoles() != null)
            {
                for (SysRole r : u.getRoles())
                {
                    if (ROLE_KEY_HOSPITAL_ADMIN.equals(r.getRoleKey()))
                        return; // 已存在，无需创建用户
                }
            }
        }

        // 3) 创建首字母用户，登录名全局唯一：pinyinCode_tenantId
        String loginName = code + "_" + tenantId;
        if (loginName.length() > 30)
            loginName = code + "_" + tenantId.substring(0, 20);
        SysUser user = new SysUser();
        user.setLoginName(loginName);
        user.setUserName(code);
        user.setUserType(UserConstants.REGISTER_USER_TYPE);
        user.setTenantId(tenantId);
        user.setStatus("0");
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(Md5Utils.hash(loginName + DEFAULT_PWD + user.getSalt()));
        user.setCreateBy(operBy);
        userMapper.insertUser(user);
        Long userId = user.getUserId();

        List<SysUserRole> list = new ArrayList<>();
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        list.add(ur);
        userRoleMapper.batchUserRole(list);
    }
}
