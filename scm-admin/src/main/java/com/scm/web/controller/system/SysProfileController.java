package com.scm.web.controller.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.scm.common.annotation.Log;
import com.scm.common.config.ScmConfig;
import com.scm.common.constant.UserConstants;
import com.scm.common.core.controller.BaseController;
import com.scm.common.core.domain.AjaxResult;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.enums.BusinessType;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.LoginNameUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.common.utils.UserProfileGateUtils;
import com.scm.common.utils.file.FileUploadUtils;
import com.scm.common.utils.file.MimeTypeUtils;
import com.scm.framework.shiro.service.SysPasswordService;
import com.scm.system.service.ISysUserService;

/**
 * 个人信息 业务处理
 * 
 * @author scm
 */
@Controller
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(SysProfileController.class);

    private String prefix = "system/user/profile";

    @Autowired
    private ISysUserService userService;
    
    @Autowired
    private SysPasswordService passwordService;

    /**
     * 个人信息
     */
    @GetMapping()
    public String profile(ModelMap mmap)
    {
        SysUser user = getSysUser();
        mmap.put("user", user);
        mmap.put("roleGroup", userService.selectUserRoleGroup(user.getUserId()));
        mmap.put("postGroup", userService.selectUserPostGroup(user.getUserId()));
        return prefix + "/profile";
    }

    @GetMapping("/checkPassword")
    @ResponseBody
    public boolean checkPassword(String password)
    {
        SysUser user = userService.selectUserById(getSysUser().getUserId());
        if (user == null)
        {
            return false;
        }
        return passwordService.matches(user, password);
    }

    @GetMapping("/resetPwd")
    public String resetPwd(ModelMap mmap)
    {
        SysUser user = getSysUser();
        mmap.put("user", userService.selectUserById(user.getUserId()));
        return prefix + "/resetPwd";
    }

    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    @ResponseBody
    public AjaxResult resetPwd(String oldPassword, String newPassword)
    {
        SysUser user = getSysUser();
        if (!passwordService.matches(user, oldPassword))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (passwordService.matches(user, newPassword))
        {
            return error("新密码不能与旧密码相同");
        }
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(passwordService.encryptPassword(user.getLoginName(), newPassword, user.getSalt()));
        user.setPwdPlain("");
        user.setPwdUpdateDate(DateUtils.getNowDate());
        if (userService.resetUserPwd(user) > 0)
        {
            setSysUser(userService.selectUserById(user.getUserId()));
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 修改用户
     */
    @GetMapping("/edit")
    public String edit(ModelMap mmap)
    {
        SysUser user = getSysUser();
        mmap.put("user", userService.selectUserById(user.getUserId()));
        return prefix + "/edit";
    }

    /**
     * 修改头像
     */
    @GetMapping("/avatar")
    public String avatar(ModelMap mmap)
    {
        SysUser user = getSysUser();
        mmap.put("user", userService.selectUserById(user.getUserId()));
        return prefix + "/avatar";
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update(SysUser user)
    {
        SysUser currentUser = getSysUser();
        currentUser.setUserName(user.getUserName());
        currentUser.setRealName(user.getRealName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(currentUser))
        {
            return error("修改用户'" + currentUser.getLoginName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(currentUser))
        {
            return error("修改用户'" + currentUser.getLoginName() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserInfo(currentUser) > 0)
        {
            setSysUser(userService.selectUserById(currentUser.getUserId()));
            return success();
        }
        return error();
    }

    /**
     * 保存头像
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateAvatar")
    @ResponseBody
    public AjaxResult updateAvatar(@RequestParam("avatarfile") MultipartFile file)
    {
        SysUser currentUser = getSysUser();
        try
        {
            if (!file.isEmpty())
            {
                String avatar = FileUploadUtils.upload(ScmConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
                currentUser.setAvatar(avatar);
                if (userService.updateUserInfo(currentUser) > 0)
                {
                    setSysUser(userService.selectUserById(currentUser.getUserId()));
                    return success();
                }
            }
            return error();
        }
        catch (Exception e)
        {
            log.error("修改头像失败！", e);
            return error(e.getMessage());
        }
    }

    /**
     * 登录名含汉字或不合规时：修改登录名并重设密码（保存后退出，需用新登录名登录）。
     */
    @GetMapping("/loginMigrate")
    public String loginMigrate(ModelMap mmap)
    {
        SysUser fresh = userService.selectUserById(getSysUser().getUserId());
        if (fresh == null || !UserProfileGateUtils.isLoginNameInvalidForScm(fresh))
        {
            return "redirect:/system/user/profile";
        }
        mmap.put("user", fresh);
        return prefix + "/loginMigrate";
    }

    /**
     * 仅补充姓名（登录名已合规时）。
     */
    @GetMapping("/completeRealName")
    public String completeRealName(ModelMap mmap)
    {
        SysUser fresh = userService.selectUserById(getSysUser().getUserId());
        if (fresh == null)
        {
            return "redirect:/system/user/profile";
        }
        if (UserProfileGateUtils.isLoginNameInvalidForScm(fresh))
        {
            return "redirect:/system/user/profile/loginMigrate";
        }
        if (!UserProfileGateUtils.isRealNameMissing(fresh))
        {
            return "redirect:/system/user/profile";
        }
        mmap.put("user", fresh);
        return prefix + "/completeRealName";
    }

    @Log(title = "完善姓名", businessType = BusinessType.UPDATE)
    @PostMapping("/completeRealName")
    @ResponseBody
    public AjaxResult completeRealNameSave(@RequestParam("realName") String realName)
    {
        SysUser fresh = userService.selectUserById(getSysUser().getUserId());
        if (fresh == null)
        {
            return error("用户不存在");
        }
        if (UserProfileGateUtils.isLoginNameInvalidForScm(fresh))
        {
            return error("请先完成登录名合规设置");
        }
        if (!UserProfileGateUtils.isRealNameMissing(fresh))
        {
            setSysUser(fresh);
            return success();
        }
        if (StringUtils.isEmpty(StringUtils.trimToNull(realName)))
        {
            return error("姓名不能为空");
        }
        fresh.setRealName(realName.trim());
        if (userService.updateUserInfo(fresh) > 0)
        {
            setSysUser(userService.selectUserById(fresh.getUserId()));
            return success();
        }
        return error("保存失败");
    }

    @Log(title = "合规登录名迁移", businessType = BusinessType.UPDATE)
    @PostMapping("/migrateLogin")
    @ResponseBody
    public AjaxResult migrateLogin(@RequestParam("newLoginName") String newLoginName,
        @RequestParam("oldPassword") String oldPassword,
        @RequestParam("newPassword") String newPassword,
        @RequestParam("confirmPassword") String confirmPassword,
        @RequestParam(value = "realName", required = false) String realName)
    {
        SysUser fresh = userService.selectUserById(getSysUser().getUserId());
        if (fresh == null)
        {
            return error("用户不存在");
        }
        if (!UserProfileGateUtils.isLoginNameInvalidForScm(fresh))
        {
            return error("当前账号无需修改登录名");
        }
        if (!passwordService.matches(fresh, oldPassword))
        {
            return error("当前密码错误");
        }
        if (StringUtils.isEmpty(newLoginName) || StringUtils.isEmpty(newPassword))
        {
            return error("新登录名与密码不能为空");
        }
        if (!StringUtils.equals(newPassword, confirmPassword))
        {
            return error("两次输入的新密码不一致");
        }
        if (newPassword.length() < UserConstants.PASSWORD_MIN_LENGTH || newPassword.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            return error("新密码长度须在5到20个字符之间");
        }
        if (passwordService.matches(fresh, newPassword))
        {
            return error("新密码不能与当前密码相同");
        }
        String rn = StringUtils.trimToNull(realName);
        if (rn != null)
        {
            fresh.setRealName(rn);
        }
        if (UserProfileGateUtils.isRealNameMissing(fresh))
        {
            return error("请填写姓名");
        }
        String newLogin = newLoginName.trim();
        String loginErr = LoginNameUtils.validateLoginName(newLogin, UserConstants.USERNAME_MAX_LENGTH);
        if (loginErr != null)
        {
            return error(loginErr);
        }
        SysUser uniqueProbe = new SysUser();
        uniqueProbe.setUserId(fresh.getUserId());
        uniqueProbe.setLoginName(newLogin);
        if (!userService.checkLoginNameUnique(uniqueProbe))
        {
            return error("新登录名已被占用，请更换");
        }
        fresh.setLoginName(newLogin);
        fresh.setSalt(ShiroUtils.randomSalt());
        fresh.setPassword(passwordService.encryptPassword(newLogin, newPassword, fresh.getSalt()));
        fresh.setPwdPlain("");
        fresh.setPwdUpdateDate(DateUtils.getNowDate());
        if (userService.updateUserInfo(fresh) > 0)
        {
            ShiroUtils.logout();
            return success("登录名与密码已更新，请使用新登录名重新登录");
        }
        return error("保存失败，请重试");
    }
}
