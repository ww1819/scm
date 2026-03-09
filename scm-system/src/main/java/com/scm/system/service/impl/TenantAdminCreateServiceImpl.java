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
 * 客户新增后自动创建医院管理员、供应商、供应商业务员角色及医院管理员用户
 */
@Service
public class TenantAdminCreateServiceImpl implements ITenantAdminCreateService
{
    private static final String ROLE_KEY_HOSPITAL_ADMIN = "hospital_admin";
    private static final String ROLE_KEY_SUPPLIER = "supplier";
    private static final String ROLE_KEY_SUPPLIER_SALES = "supplier_sales";
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
        ensureTenantRolesAndAdminUser(tenantId, tenantName, pinyinCode, operBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureTenantRolesAndAdminUser(String tenantId, String tenantName, String pinyinCode, String operBy)
    {
        if (StringUtils.isEmpty(tenantId)) return;
        String name = StringUtils.isNotEmpty(tenantName) ? tenantName : "客户";
        String code = StringUtils.isNotEmpty(pinyinCode) ? pinyinCode : "kh";

        // 1) 确保三个角色存在：医院管理员、供应商、供应商业务员
        ensureRole(tenantId, name, ROLE_KEY_HOSPITAL_ADMIN, "医院管理员", "2", operBy);
        ensureRole(tenantId, name, ROLE_KEY_SUPPLIER, "供应商", "3", operBy);
        ensureRole(tenantId, name, ROLE_KEY_SUPPLIER_SALES, "供应商业务员", "4", operBy);

        // 2) 确保医院管理员用户存在（若不存在则创建）
        SysRole adminRole = roleMapper.selectByRoleKeyAndTenantId(ROLE_KEY_HOSPITAL_ADMIN, tenantId);
        if (adminRole == null) return;
        List<SysUser> tenantUsers = userMapper.selectUserListByTenantId(tenantId);
        for (SysUser u : tenantUsers)
        {
            if (u.getRoles() != null)
            {
                for (SysRole r : u.getRoles())
                {
                    if (ROLE_KEY_HOSPITAL_ADMIN.equals(r.getRoleKey()))
                        return; // 已存在拥有医院管理员角色的用户
                }
            }
        }

        // 3) 创建首字母管理员用户
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
        ur.setRoleId(adminRole.getRoleId());
        list.add(ur);
        userRoleMapper.batchUserRole(list);
    }

    private void ensureRole(String tenantId, String tenantName, String roleKey, String roleName, String roleSort, String operBy)
    {
        SysRole role = roleMapper.selectByRoleKeyAndTenantId(roleKey, tenantId);
        if (role != null) return;
        role = new SysRole();
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setRoleSort(roleSort);
        role.setDataScope("1");
        role.setStatus("0");
        role.setRemark("客户[" + tenantName + "]默认角色");
        role.setTenantId(tenantId);
        role.setCreateBy(operBy);
        roleMapper.insertRole(role);
    }
}
