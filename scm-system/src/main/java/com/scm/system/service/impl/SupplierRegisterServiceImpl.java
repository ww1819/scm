package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.UserConstants;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierUser;
import com.scm.system.domain.SupplierUserApply;
import com.scm.system.domain.SysRoleMenu;
import com.scm.system.domain.SysUserRole;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.mapper.SupplierUserApplyMapper;
import com.scm.system.mapper.SupplierUserMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysUserMapper;
import com.scm.system.mapper.SysRoleMenuMapper;
import com.scm.system.mapper.SysUserRoleMapper;
import com.scm.common.utils.security.Md5Utils;
import com.scm.system.service.ISupplierRegisterService;

/**
 * 供应商注册与业务员申请
 */
@Service
public class SupplierRegisterServiceImpl implements ISupplierRegisterService {

    private static final String ROLE_KEY_SUPPLIER_ADMIN = "supplier_admin";
    private static final String ROLE_KEY_SUPPLIER_SALES = "supplier_sales";

    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SupplierUserMapper supplierUserMapper;
    @Autowired
    private SupplierUserApplyMapper applyMapper;
    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    /** 供应商管理员默认菜单：业务员审核(2003,20031,20032)、供应商资质登记(2301)、产品证件登记(2302) */
    private static final long[] SUPPLIER_ADMIN_MENU_IDS = { 2003L, 20031L, 20032L, 2301L, 2302L };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerSupplier(Supplier supplier, SysUser adminUser, String operBy) {
        if (supplier == null || StringUtils.isEmpty(supplier.getCompanyName())) {
            throw new IllegalArgumentException("公司名称不能为空");
        }
        if (adminUser == null || StringUtils.isEmpty(adminUser.getLoginName()) || StringUtils.isEmpty(adminUser.getPassword())) {
            throw new IllegalArgumentException("用户信息不完整");
        }
        Supplier exist = supplierMapper.selectSupplierByCompanyName(supplier.getCompanyName().trim());
        if (exist != null) {
            throw new IllegalArgumentException("该公司名称已存在，注册失败");
        }
        if (userMapper.checkLoginNameUnique(adminUser.getLoginName()) != null) {
            throw new IllegalArgumentException("注册账号已存在");
        }

        String supplierCode = "GYS" + System.currentTimeMillis();
        supplier.setSupplierCode(supplierCode);
        supplier.setStatus("0");
        supplier.setAuditStatus("0");
        if (StringUtils.isEmpty(supplier.getCreateBy())) {
            supplier.setCreateBy(operBy != null ? operBy : adminUser.getLoginName());
        }
        supplierMapper.insertSupplier(supplier);
        Long supplierId = supplier.getSupplierId();

        SysRole adminRole = new SysRole();
        adminRole.setRoleName("供应商管理员");
        adminRole.setRoleKey(ROLE_KEY_SUPPLIER_ADMIN);
        adminRole.setRoleSort("1");
        adminRole.setDataScope("1");
        adminRole.setStatus("0");
        adminRole.setRemark("供应商[" + supplier.getCompanyName() + "]管理员");
        adminRole.setSupplierId(supplierId);
        adminRole.setCreateBy(operBy != null ? operBy : adminUser.getLoginName());
        roleMapper.insertRole(adminRole);
        Long adminRoleId = adminRole.getRoleId();
        List<SysRoleMenu> roleMenus = new ArrayList<>();
        for (long menuId : SUPPLIER_ADMIN_MENU_IDS) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(adminRoleId);
            rm.setMenuId(menuId);
            roleMenus.add(rm);
        }
        if (!roleMenus.isEmpty()) {
            roleMenuMapper.batchRoleMenu(roleMenus);
        }

        SysRole salesRole = new SysRole();
        salesRole.setRoleName("供应商业务员");
        salesRole.setRoleKey(ROLE_KEY_SUPPLIER_SALES);
        salesRole.setRoleSort("2");
        salesRole.setDataScope("1");
        salesRole.setStatus("0");
        salesRole.setRemark("供应商[" + supplier.getCompanyName() + "]业务员");
        salesRole.setSupplierId(supplierId);
        salesRole.setCreateBy(operBy != null ? operBy : adminUser.getLoginName());
        roleMapper.insertRole(salesRole);

        SysUser user = new SysUser();
        user.setLoginName(adminUser.getLoginName().trim());
        user.setUserName(StringUtils.isNotEmpty(adminUser.getUserName()) ? adminUser.getUserName() : adminUser.getLoginName());
        user.setUserType(UserConstants.REGISTER_USER_TYPE);
        user.setStatus("0");
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(Md5Utils.hash(user.getLoginName() + adminUser.getPassword() + user.getSalt()));
        if (StringUtils.isNotEmpty(adminUser.getPhonenumber())) user.setPhonenumber(adminUser.getPhonenumber());
        if (StringUtils.isNotEmpty(adminUser.getEmail())) user.setEmail(adminUser.getEmail());
        user.setCreateBy(operBy != null ? operBy : user.getLoginName());
        userMapper.insertUser(user);
        Long userId = user.getUserId();

        List<SysUserRole> urList = new ArrayList<>();
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(adminRoleId);
        urList.add(ur);
        userRoleMapper.batchUserRole(urList);

        SupplierUser su = new SupplierUser();
        su.setSupplierId(supplierId);
        su.setUserId(userId);
        su.setIsMain("1");
        su.setStatus("0");
        su.setCreateBy(operBy != null ? operBy : user.getLoginName());
        su.setCreateTime(new Date());
        supplierUserMapper.insertSupplierUser(su);

        return supplierId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerSalesperson(Long supplierId, SysUser user, String operBy) {
        if (supplierId == null || user == null || StringUtils.isEmpty(user.getLoginName()) || StringUtils.isEmpty(user.getPassword())) {
            throw new IllegalArgumentException("供应商与用户信息不完整");
        }
        if (supplierMapper.selectSupplierById(supplierId) == null) {
            throw new IllegalArgumentException("所选供应商不存在");
        }
        SysUser existUser = userMapper.checkLoginNameUnique(user.getLoginName());
        Long userId;
        if (existUser != null) {
            userId = existUser.getUserId();
        } else {
            SysUser newUser = new SysUser();
            newUser.setLoginName(user.getLoginName().trim());
            newUser.setUserName(StringUtils.isNotEmpty(user.getUserName()) ? user.getUserName() : user.getLoginName());
            newUser.setUserType(UserConstants.REGISTER_USER_TYPE);
            newUser.setStatus("0");
            newUser.setSalt(ShiroUtils.randomSalt());
            newUser.setPassword(Md5Utils.hash(newUser.getLoginName() + user.getPassword() + newUser.getSalt()));
            if (StringUtils.isNotEmpty(user.getPhonenumber())) newUser.setPhonenumber(user.getPhonenumber());
            if (StringUtils.isNotEmpty(user.getEmail())) newUser.setEmail(user.getEmail());
            newUser.setCreateBy(operBy != null ? operBy : newUser.getLoginName());
            userMapper.insertUser(newUser);
            userId = newUser.getUserId();
        }
        SupplierUserApply pending = applyMapper.selectPendingBySupplierAndUser(supplierId, userId);
        if (pending != null) {
            throw new IllegalArgumentException("您已提交过该供应商的关联申请，请等待审核");
        }
        SupplierUserApply apply = new SupplierUserApply();
        apply.setSupplierId(supplierId);
        apply.setUserId(userId);
        apply.setStatus(SupplierUserApply.STATUS_PENDING);
        apply.setApplyTime(new Date());
        applyMapper.insert(apply);
        return apply.getApplyId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitSupplierAssociate(Long supplierId, Long userId, String operBy) {
        if (supplierId == null || userId == null) {
            throw new IllegalArgumentException("供应商与用户不能为空");
        }
        if (supplierMapper.selectSupplierById(supplierId) == null) {
            throw new IllegalArgumentException("所选供应商不存在");
        }
        SupplierUser exist = supplierUserMapper.selectSupplierUserByUserIdAndSupplierId(userId, supplierId);
        if (exist != null) {
            throw new IllegalArgumentException("您已是该供应商业务员，无需重复申请");
        }
        SupplierUserApply pending = applyMapper.selectPendingBySupplierAndUser(supplierId, userId);
        if (pending != null) {
            throw new IllegalArgumentException("您已提交过该供应商的关联申请，请等待审核");
        }
        SupplierUserApply apply = new SupplierUserApply();
        apply.setSupplierId(supplierId);
        apply.setUserId(userId);
        apply.setStatus(SupplierUserApply.STATUS_PENDING);
        apply.setApplyTime(new Date());
        applyMapper.insert(apply);
        return apply.getApplyId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApply(Long applyId, String approved, String auditRemark, String operBy) {
        SupplierUserApply apply = applyMapper.selectByApplyId(applyId);
        if (apply == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (!SupplierUserApply.STATUS_PENDING.equals(apply.getStatus())) {
            throw new IllegalArgumentException("该申请已处理");
        }
        Long supplierId = apply.getSupplierId();
        Long operatorUserId = ShiroUtils.getUserId();
        SupplierUser adminSu = supplierUserMapper.selectSupplierUserByUserIdAndSupplierId(operatorUserId, supplierId);
        if (adminSu == null || !"1".equals(adminSu.getIsMain())) {
            throw new IllegalArgumentException("仅该供应商管理员可审核");
        }
        if ("1".equals(approved)) {
            applyMapper.updateStatus(applyId, SupplierUserApply.STATUS_APPROVED, operBy, auditRemark);
            SysRole salesRole = roleMapper.selectByRoleKeyAndSupplierId(ROLE_KEY_SUPPLIER_SALES, supplierId);
            if (salesRole != null) {
                List<SysUserRole> urList = new ArrayList<>();
                SysUserRole ur = new SysUserRole();
                ur.setUserId(apply.getUserId());
                ur.setRoleId(salesRole.getRoleId());
                urList.add(ur);
                userRoleMapper.batchUserRole(urList);
            }
            SupplierUser su = new SupplierUser();
            su.setSupplierId(supplierId);
            su.setUserId(apply.getUserId());
            su.setIsMain("0");
            su.setStatus("0");
            su.setCreateBy(operBy != null ? operBy : "system");
            su.setCreateTime(new Date());
            supplierUserMapper.insertSupplierUser(su);
        } else {
            applyMapper.updateStatus(applyId, SupplierUserApply.STATUS_REJECTED, operBy, auditRemark);
        }
    }
}
