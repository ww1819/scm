package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.scm.common.annotation.DataScope;
import com.scm.common.constant.ScmAuthConstants;
import com.scm.common.constant.UserConstants;
import com.scm.common.core.domain.entity.SysDept;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.core.text.Convert;
import com.scm.common.exception.ServiceException;
import com.scm.common.utils.ExceptionUtil;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.bean.BeanValidators;
import com.scm.common.utils.html.EscapeUtil;
import com.scm.common.utils.security.Md5Utils;
import com.scm.common.utils.spring.SpringUtils;
import com.scm.system.domain.HospitalUser;
import com.scm.system.domain.SupplierUser;
import com.scm.system.domain.SysPost;
import com.scm.system.domain.SysUserPost;
import com.scm.system.domain.SysUserRole;
import com.scm.system.mapper.HospitalUserMapper;
import com.scm.system.mapper.SupplierUserMapper;
import com.scm.system.mapper.SysPostMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysUserMapper;
import com.scm.system.mapper.SysUserPostMapper;
import com.scm.system.mapper.SysUserRoleMapper;
import com.scm.system.service.ISysConfigService;
import com.scm.system.service.ISysDeptService;
import com.scm.system.service.ISysUserService;

/**
 * 用户 业务层处理
 *
 * @author scm
 */
@Service
public class SysUserServiceImpl implements ISysUserService
{
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);
    private static final String ROLE_KEY_HOSPITAL_ADMIN = "hospital_admin";
    private static final String ROLE_KEY_HOSPITAL_STAFF = "hospital_staff";
    private static final String ROLE_KEY_SUPPLIER_ADMIN = "supplier_admin";
    private static final String ROLE_KEY_SUPPLIER_SALES = "supplier_sales";
    private static final String ROLE_KEY_SUPPLIER_LEGACY = "supplier";

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SupplierUserMapper supplierUserMapper;

    @Autowired
    private HospitalUserMapper hospitalUserMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    protected Validator validator;

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u", tenantAlias = "u")
    public List<SysUser> selectUserList(SysUser user)
    {
        return userMapper.selectUserList(user);
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u", tenantAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user)
    {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u", tenantAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user)
    {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByLoginName(String userName)
    {
        return userMapper.selectUserByLoginName(userName);
    }

    /**
     * 通过手机号码查询用户
     *
     * @param phoneNumber 手机号码
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByPhoneNumber(String phoneNumber)
    {
        return userMapper.selectUserByPhoneNumber(phoneNumber);
    }

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByEmail(String email)
    {
        return userMapper.selectUserByEmail(email);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId)
    {
        return userMapper.selectUserById(userId);
    }

    /**
     * 通过用户ID查询用户和角色关联
     *
     * @param userId 用户ID
     * @return 用户和角色关联列表
     */
    @Override
    public List<SysUserRole> selectUserRoleByUserId(Long userId)
    {
        return userRoleMapper.selectUserRoleByUserId(userId);
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserById(Long userId)
    {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
        userPostMapper.deleteUserPostByUserId(userId);
        supplierUserMapper.deleteSupplierUserByUserId(userId);
        hospitalUserMapper.deleteHospitalUserByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批量删除用户信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserByIds(String ids)
    {
        Long[] userIds = Convert.toLongArray(ids);
        for (Long userId : userIds)
        {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        // 删除用户与角色关联
        userRoleMapper.deleteUserRole(userIds);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPost(userIds);
        for (Long userId : userIds)
        {
            supplierUserMapper.deleteSupplierUserByUserId(userId);
            hospitalUserMapper.deleteHospitalUserByUserId(userId);
        }
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertUser(SysUser user)
    {
        checkSuperAdminRoleDept(user.getDeptId(), user.getRoleIds());
        validateMaintainScopeOnSave(user, user.getRoleIds());
        assertUserRoleTypeMatchesForSave(user, user.getRoleIds());
        // 新增用户信息
        int rows = userMapper.insertUser(user);
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user.getUserId(), user.getRoleIds());
        syncMaintainSupplierHospital(user);
        return rows;
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUser user)
    {
        user.setUserType(UserConstants.REGISTER_USER_TYPE);
        return userMapper.insertUser(user) > 0;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateUser(SysUser user)
    {
        checkSuperAdminRoleDept(user.getDeptId(), user.getRoleIds());
        validateMaintainScopeOnSave(user, user.getRoleIds());
        assertUserRoleTypeMatchesForSave(user, user.getRoleIds());
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user.getUserId(), user.getRoleIds());
        // 删除用户与岗位关联
        userPostMapper.deleteUserPostByUserId(userId);
        // 新增用户与岗位管理
        insertUserPost(user);
        int rows = userMapper.updateUser(user);
        syncMaintainSupplierHospital(user);
        return rows;
    }

    /**
     * 用户维护：同步「维护供应商 / 维护医院」到 scm_supplier_user、scm_hospital_user（各保留一条主关联）
     */
    private void syncMaintainSupplierHospital(SysUser user)
    {
        Long userId = user.getUserId();
        if (userId == null)
        {
            return;
        }
        String oper = StringUtils.isNotEmpty(user.getUpdateBy()) ? user.getUpdateBy() : user.getCreateBy();
        if (StringUtils.isEmpty(oper))
        {
            oper = ShiroUtils.getLoginName();
        }
        Date now = new Date();
        supplierUserMapper.deleteSupplierUserByUserId(userId);
        if (user.getMaintainSupplierId() != null)
        {
            SupplierUser su = new SupplierUser();
            su.setSupplierId(user.getMaintainSupplierId());
            su.setUserId(userId);
            su.setIsMain("1");
            su.setStatus("0");
            su.setCreateBy(oper);
            su.setCreateTime(now);
            supplierUserMapper.insertSupplierUser(su);
        }
        hospitalUserMapper.deleteHospitalUserByUserId(userId);
        if (user.getMaintainHospitalId() != null)
        {
            HospitalUser hu = new HospitalUser();
            hu.setHospitalId(user.getMaintainHospitalId());
            hu.setUserId(userId);
            hu.setIsMain("1");
            hu.setStatus("0");
            hu.setCreateBy(oper);
            hu.setCreateTime(now);
            hospitalUserMapper.insertHospitalUser(hu);
        }
    }

    /**
     * 修改用户个人详细信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserInfo(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * 用户授权角色
     *
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        SysUser exist = userMapper.selectUserById(userId);
        if (exist == null)
        {
            throw new ServiceException("用户不存在");
        }
        checkSuperAdminRoleDept(exist.getDeptId(), roleIds);
        validateMaintainScopeOnAuth(userId, roleIds);
        assertUserRoleTypeMatches(userId, roleIds);
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    private void assertUserRoleTypeMatchesForSave(SysUser user, Long[] roleIds)
    {
        if (user == null || roleIds == null || roleIds.length == 0)
        {
            return;
        }
        String ut = StringUtils.trimToEmpty(user.getUserType());
        if (StringUtils.isEmpty(ut) || !isScmTenantUserType(ut))
        {
            return;
        }
        for (Long roleId : roleIds)
        {
            if (roleId == null)
            {
                continue;
            }
            SysRole role = roleMapper.selectRoleById(roleId);
            if (role == null || StringUtils.isNotEmpty(role.getDelFlag()) && "2".equals(role.getDelFlag()))
            {
                continue;
            }
            String rt = normalizeScmRoleType(role);
            if (!ut.equalsIgnoreCase(rt))
            {
                throw new ServiceException("用户类型与角色类型不一致：用户为「" + ut + "」时不能分配「" + rt + "」类角色");
            }
        }
    }

    private void assertUserRoleTypeMatches(Long userId, Long[] roleIds)
    {
        if (userId == null || roleIds == null || roleIds.length == 0)
        {
            return;
        }
        SysUser u = userMapper.selectUserById(userId);
        if (u == null)
        {
            return;
        }
        assertUserRoleTypeMatchesForSave(u, roleIds);
    }

    private static String normalizeScmRoleType(SysRole role)
    {
        String rt = StringUtils.trimToEmpty(role.getRoleType());
        if (StringUtils.isEmpty(rt))
        {
            return ScmAuthConstants.ROLE_TYPE_PLATFORM;
        }
        return rt;
    }

    /** 仅对 SCM 主体类型用户校验与角色 role_type 一致（兼容历史 user_type=00 等） */
    private static boolean isScmTenantUserType(String userType)
    {
        return ScmAuthConstants.ROLE_TYPE_PLATFORM.equalsIgnoreCase(userType)
            || ScmAuthConstants.ROLE_TYPE_HOSPITAL.equalsIgnoreCase(userType)
            || ScmAuthConstants.ROLE_TYPE_SUPPLIER.equalsIgnoreCase(userType);
    }

    private void validateMaintainScopeOnSave(SysUser user, Long[] roleIds)
    {
        if (roleIds == null || roleIds.length == 0)
        {
            return;
        }
        boolean requireHospital = false;
        boolean requireSupplier = false;
        for (Long roleId : roleIds)
        {
            if (roleId == null)
            {
                continue;
            }
            SysRole role = roleMapper.selectRoleById(roleId);
            if (role == null || StringUtils.isNotEmpty(role.getDelFlag()) && "2".equals(role.getDelFlag()))
            {
                continue;
            }
            String roleKey = StringUtils.trimToEmpty(role.getRoleKey());
            if (ROLE_KEY_HOSPITAL_ADMIN.equals(roleKey) || ROLE_KEY_HOSPITAL_STAFF.equals(roleKey))
            {
                requireHospital = true;
            }
            if (ROLE_KEY_SUPPLIER_ADMIN.equals(roleKey) || ROLE_KEY_SUPPLIER_SALES.equals(roleKey)
                || ROLE_KEY_SUPPLIER_LEGACY.equals(roleKey))
            {
                requireSupplier = true;
            }
        }
        if (requireHospital && user.getMaintainHospitalId() == null)
        {
            throw new ServiceException("当前角色需要维护医院，请先选择“维护医院”");
        }
        if (requireSupplier && user.getMaintainSupplierId() == null)
        {
            throw new ServiceException("当前角色需要维护供应商，请先选择“维护供应商”");
        }
    }

    private void validateMaintainScopeOnAuth(Long userId, Long[] roleIds)
    {
        if (roleIds == null || roleIds.length == 0)
        {
            return;
        }
        boolean requireHospital = false;
        boolean requireSupplier = false;
        for (Long roleId : roleIds)
        {
            if (roleId == null)
            {
                continue;
            }
            SysRole role = roleMapper.selectRoleById(roleId);
            if (role == null || StringUtils.isNotEmpty(role.getDelFlag()) && "2".equals(role.getDelFlag()))
            {
                continue;
            }
            String roleKey = StringUtils.trimToEmpty(role.getRoleKey());
            if (ROLE_KEY_HOSPITAL_ADMIN.equals(roleKey) || ROLE_KEY_HOSPITAL_STAFF.equals(roleKey))
            {
                requireHospital = true;
            }
            if (ROLE_KEY_SUPPLIER_ADMIN.equals(roleKey) || ROLE_KEY_SUPPLIER_SALES.equals(roleKey)
                || ROLE_KEY_SUPPLIER_LEGACY.equals(roleKey))
            {
                requireSupplier = true;
            }
        }
        if (requireHospital && hospitalUserMapper.selectHospitalUserByUserId(userId) == null)
        {
            throw new ServiceException("当前用户未绑定医院，请先在“用户编辑”第三方置维护医院后再授权医院角色");
        }
        if (requireSupplier && supplierUserMapper.selectSupplierUserByUserId(userId) == null)
        {
            throw new ServiceException("当前用户未绑定供应商，请先在“用户编辑”第三方置维护供应商后再授权供应商角色");
        }
    }

    /**
     * 修改用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetUserPwd(SysUser user)
    {
        return updateUserInfo(user);
    }

    /**
     * 新增用户角色信息
     *
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, Long[] roleIds)
    {
        assertUserRoleTypeMatches(userId, roleIds);
        if (StringUtils.isNotNull(roleIds))
        {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>();
            for (Long roleId : roleIds)
            {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (list.size() > 0)
            {
                userRoleMapper.batchUserRole(list);
            }
        }
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    public void insertUserPost(SysUser user)
    {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotNull(posts))
        {
            // 新增用户与岗位管理
            List<SysUserPost> list = new ArrayList<SysUserPost>();
            for (Long postId : posts)
            {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            if (list.size() > 0)
            {
                userPostMapper.batchUserPost(list);
            }
        }
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkLoginNameUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkLoginNameUnique(user.getLoginName());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkEmailUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUser user)
    {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 超级管理员角色仅允许挂在「一级部门」（parent_id = 0），避免选二级目录即具备全量权限的误配。
     */
    @Override
    public void checkSuperAdminRoleDept(Long deptId, Long[] roleIds)
    {
        if (roleIds == null || roleIds.length == 0)
        {
            return;
        }
        boolean assignSuperAdmin = false;
        for (Long roleId : roleIds)
        {
            if (roleId != null && SysRole.isAdmin(roleId))
            {
                assignSuperAdmin = true;
                break;
            }
        }
        if (!assignSuperAdmin)
        {
            return;
        }
        if (deptId == null)
        {
            throw new ServiceException("分配超级管理员角色前请先选择归属部门");
        }
        SysDept dept = deptService.selectDeptById(deptId);
        if (dept == null)
        {
            throw new ServiceException("归属部门不存在，无法分配超级管理员角色");
        }
        Long parentId = dept.getParentId();
        if (parentId == null || parentId.longValue() != 0L)
        {
            throw new ServiceException("超级管理员角色仅可归属一级组织（根部门，如「医承云配」），请更换归属部门或取消该角色");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId)
    {
        if (!SysUser.isAdmin(ShiroUtils.getUserId()))
        {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
            if (StringUtils.isEmpty(users))
            {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(Long userId)
    {
        List<SysRole> list = roleMapper.selectRolesByUserId(userId);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(Long userId)
    {
        List<SysPost> list = postMapper.selectPostsByUserId(userId);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    /**
     * 导入用户数据
     *
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(userList) || userList.size() == 0)
        {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysUser user : userList)
        {
            try
            {
                // 验证是否存在这个用户
                SysUser u = userMapper.selectUserByLoginName(user.getLoginName());
                if (StringUtils.isNull(u))
                {
                    BeanValidators.validateWithException(validator, user);
                    deptService.checkDeptDataScope(user.getDeptId());
                    String password = configService.selectConfigByKey("sys.user.initPassword");
                    user.setPassword(Md5Utils.hash(user.getLoginName() + password));
                    user.setCreateBy(operName);
                    userMapper.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    BeanValidators.validateWithException(validator, user);
                    checkUserAllowed(u);
                    checkUserDataScope(u.getUserId());
                    deptService.checkDeptDataScope(user.getDeptId());
                    user.setUserId(u.getUserId());
                    user.setUpdateBy(operName);
                    userMapper.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getLoginName() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String loginName = user.getLoginName();
                if (ExceptionUtil.isCausedBy(e, ConstraintViolationException.class))
                {
                    loginName = EscapeUtil.clean(loginName);
                }
                String msg = "<br/>" + failureNum + "、账号 " + loginName + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 用户状态修改
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int changeStatus(SysUser user)
    {
        return userMapper.updateUser(user);
    }
}
