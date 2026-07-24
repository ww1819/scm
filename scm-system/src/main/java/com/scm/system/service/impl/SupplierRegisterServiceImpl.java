package com.scm.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scm.common.constant.ScmAuthConstants;
import com.scm.common.constant.UserConstants;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.PinyinUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.system.domain.Supplier;
import com.scm.system.domain.SupplierUser;
import com.scm.system.domain.SupplierUserApply;
import com.scm.system.domain.SysUserRole;
import com.scm.system.mapper.HospitalSupplierMapper;
import com.scm.system.mapper.SupplierMapper;
import com.scm.system.mapper.SupplierUserApplyMapper;
import com.scm.system.mapper.SupplierUserMapper;
import com.scm.system.mapper.SysRoleMapper;
import com.scm.system.mapper.SysUserMapper;
import com.scm.system.mapper.SysUserRoleMapper;
import com.scm.common.utils.security.Md5Utils;
import com.scm.system.service.IScmScopeBootstrapService;
import com.scm.system.service.ISupplierCertificateService;
import com.scm.system.service.ISupplierRegisterService;

/**
 * 供应商注册与业务员申请
 */
@Service
public class SupplierRegisterServiceImpl implements ISupplierRegisterService {

    private static final String ROLE_KEY_SUPPLIER_SALES = ScmAuthConstants.ROLE_KEY_SUPPLIER_SALES;
    private static final String ROLE_KEY_TP_SUPPLIER_SALES = ScmAuthConstants.ROLE_KEY_TP_SUPPLIER_SALES;

    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private HospitalSupplierMapper hospitalSupplierMapper;
    @Autowired
    private IScmScopeBootstrapService scmScopeBootstrapService;
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
    @Lazy
    private ISupplierCertificateService supplierCertificateService;

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
        String rawPy = PinyinUtils.getShortCode(supplier.getCompanyName().trim());
        String py = rawPy != null ? rawPy : "";
        supplier.setPinyinCode(py);
        if (StringUtils.isEmpty(supplier.getCompanyShortName()) && StringUtils.isNotEmpty(py))
        {
            supplier.setCompanyShortName(py.toUpperCase());
        }
        supplierMapper.insertSupplier(supplier);
        Long supplierId = supplier.getSupplierId();
        if (supplierId != null)
        {
            try
            {
                supplierCertificateService.ensureMissingCertificatesForSupplier(supplierId,
                    supplier.getCreateBy() != null ? supplier.getCreateBy() : "");
            }
            catch (Exception ignored)
            {
            }
        }

        String oper = operBy != null ? operBy : adminUser.getLoginName();
        Long adminRoleId = scmScopeBootstrapService.bootstrapAfterSupplierRegister(supplierId, oper);

        SysUser user = new SysUser();
        user.setLoginName(adminUser.getLoginName().trim());
        String rn = StringUtils.trim(adminUser.getRealName());
        user.setRealName(rn);
        user.setUserName(StringUtils.isNotEmpty(adminUser.getUserName()) ? adminUser.getUserName().trim()
            : (StringUtils.isNotEmpty(rn) ? rn : adminUser.getLoginName()));
        user.setUserType(UserConstants.REGISTER_USER_TYPE);
        user.setStatus("0");
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(Md5Utils.hash(user.getLoginName() + adminUser.getPassword() + user.getSalt()));
        user.setPwdPlain(adminUser.getPassword());
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
            String rn = StringUtils.trim(user.getRealName());
            newUser.setRealName(rn);
            newUser.setUserName(StringUtils.isNotEmpty(user.getUserName()) ? user.getUserName().trim()
                : (StringUtils.isNotEmpty(rn) ? rn : user.getLoginName()));
            newUser.setUserType(UserConstants.REGISTER_USER_TYPE);
            newUser.setStatus("0");
            newUser.setSalt(ShiroUtils.randomSalt());
            newUser.setPassword(Md5Utils.hash(newUser.getLoginName() + user.getPassword() + newUser.getSalt()));
            newUser.setPwdPlain(user.getPassword());
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
            String salesRoleKey = hospitalSupplierMapper.isThirdPartyOrderSupplier(supplierId,
                ScmAuthConstants.HOSPITAL_ID_XINHUA_THIRD_PARTY)
                ? ROLE_KEY_TP_SUPPLIER_SALES : ROLE_KEY_SUPPLIER_SALES;
            SysRole salesRole = roleMapper.selectGlobalScmRoleByKey(salesRoleKey);
            if (salesRole == null)
            {
                throw new IllegalArgumentException("全局供应商业务员角色未初始化，请先执行 migrate_global_template_roles_v2.sql");
            }
            List<SysUserRole> urList = new ArrayList<>();
            SysUserRole ur = new SysUserRole();
            ur.setUserId(apply.getUserId());
            ur.setRoleId(salesRole.getRoleId());
            urList.add(ur);
            userRoleMapper.batchUserRole(urList);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordBySupplierVerify(String taxNumber, String adminPhone, String loginName, String newPassword) {
        if (StringUtils.isEmpty(taxNumber) || StringUtils.isEmpty(adminPhone)
            || StringUtils.isEmpty(loginName) || StringUtils.isEmpty(newPassword)) {
            throw new IllegalArgumentException("请填写完整信息");
        }
        String login = loginName.trim();
        String tax = taxNumber.trim();
        String phone = adminPhone.trim();
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new IllegalArgumentException("新密码长度须为 6～20 位");
        }
        boolean hasDigit = false, hasLetter = false;
        for (char c : newPassword.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
        }
        if (!hasDigit || !hasLetter) {
            throw new IllegalArgumentException("新密码必须包含数字和字母");
        }

        SysUser user = userMapper.selectUserByLoginName(login);
        if (user == null || !"0".equals(user.getDelFlag())) {
            throw new IllegalArgumentException("用户名、信用代码或手机号不正确");
        }
        if (!"0".equals(user.getStatus())) {
            throw new IllegalArgumentException("该账号已停用，无法重置密码");
        }
        SupplierUser su = supplierUserMapper.selectSupplierUserByUserId(user.getUserId());
        if (su == null || su.getSupplierId() == null) {
            throw new IllegalArgumentException("该账号不是供应商账号，无法通过此方式重置");
        }
        Supplier supplier = supplierMapper.selectSupplierById(su.getSupplierId());
        if (supplier == null) {
            throw new IllegalArgumentException("关联供应商不存在");
        }
        if (StringUtils.isEmpty(supplier.getTaxNumber())
            || !tax.equalsIgnoreCase(supplier.getTaxNumber().trim())) {
            throw new IllegalArgumentException("用户名、信用代码或手机号不正确");
        }
        String userPhone = StringUtils.trim(user.getPhonenumber());
        String contactPhone = StringUtils.trim(supplier.getContactPhone());
        boolean phoneOk = phone.equals(userPhone) || phone.equals(contactPhone);
        if (!phoneOk) {
            throw new IllegalArgumentException("用户名、信用代码或手机号不正确");
        }

        String salt = user.getSalt();
        if (StringUtils.isEmpty(salt)) {
            salt = ShiroUtils.randomSalt();
            user.setSalt(salt);
        }
        user.setPassword(Md5Utils.hash(user.getLoginName() + newPassword + salt));
        user.setPwdPlain("");
        user.setPwdUpdateDate(new Date());
        user.setUpdateBy(login);
        userMapper.updateUser(user);
    }
}
