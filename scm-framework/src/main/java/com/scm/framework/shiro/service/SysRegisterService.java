package com.scm.framework.shiro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.scm.common.constant.Constants;
import com.scm.common.constant.ShiroConstants;
import com.scm.common.constant.UserConstants;
import com.scm.common.utils.LoginNameUtils;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.DateUtils;
import com.scm.common.utils.MessageUtils;
import com.scm.common.utils.ServletUtils;
import com.scm.common.utils.ShiroUtils;
import com.scm.common.utils.StringUtils;
import com.scm.framework.manager.AsyncManager;
import com.scm.framework.manager.factory.AsyncFactory;
import com.scm.system.service.ISysUserService;

/**
 * 注册校验方法
 * 
 * @author scm
 */
@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPasswordService passwordService;

    /**
     * 注册
     */
    public String register(SysUser user)
    {
        String msg = "", loginName = user.getLoginName(), password = user.getPassword();

        if (ShiroConstants.CAPTCHA_ERROR.equals(ServletUtils.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA)))
        {
            msg = "验证码错误";
        }
        else if (StringUtils.isEmpty(loginName))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(StringUtils.trim(user.getRealName())))
        {
            msg = "用户姓名不能为空";
        }
        else if (StringUtils.trim(user.getRealName()).length() > 50)
        {
            msg = "用户姓名不能超过50个字符";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        else
        {
            String loginFmt = LoginNameUtils.validateLoginName(loginName, UserConstants.USERNAME_MAX_LENGTH);
            if (loginFmt != null)
            {
                msg = loginFmt;
            }
            else if (!userService.checkLoginNameUnique(user))
            {
                msg = "保存用户'" + loginName + "'失败，注册账号已存在";
            }
            else
            {
                user.setPwdUpdateDate(DateUtils.getNowDate());
                user.setUserName(loginName);
                user.setRealName(StringUtils.trim(user.getRealName()));
                user.setSalt(ShiroUtils.randomSalt());
                user.setPassword(passwordService.encryptPassword(loginName, password, user.getSalt()));
                boolean regFlag = userService.registerUser(user);
                if (!regFlag)
                {
                    msg = "注册失败,请联系系统管理人员";
                }
                else
                {
                    AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginName, Constants.REGISTER, MessageUtils.message("user.register.success")));
                }
            }
        }
        return msg;
    }
}
