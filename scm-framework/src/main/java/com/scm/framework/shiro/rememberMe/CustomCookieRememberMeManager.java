package com.scm.framework.shiro.rememberMe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.shiro.crypto.CryptoException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.scm.common.core.domain.entity.SysRole;
import com.scm.common.core.domain.entity.SysUser;
import com.scm.common.utils.spring.SpringUtils;
import com.scm.framework.shiro.service.SysLoginService;

/**
 * 自定义CookieRememberMeManager
 *
 * @author scm
 */
public class CustomCookieRememberMeManager extends CookieRememberMeManager
{
    private static final Logger log = LoggerFactory.getLogger(CustomCookieRememberMeManager.class);

    /**
     * 记住我时去掉角色的permissions权限字符串，防止http请求头过大。
     */
    @Override
    protected void rememberIdentity(Subject subject, PrincipalCollection principalCollection)
    {
        Map<SysRole, Set<String>> rolePermissions = new HashMap<>();
        // 清除角色的permissions权限字符串
        for (Object principal : principalCollection)
        {
            if (principal instanceof SysUser)
            {
                List<SysRole> roles = ((SysUser) principal).getRoles();
                for (SysRole role : roles)
                {
                    rolePermissions.put(role, role.getPermissions());
                    role.setPermissions(null);
                }
            }
        }
        byte[] bytes = convertPrincipalsToBytes(principalCollection);
        // 恢复角色的permissions权限字符串
        for (Object principal : principalCollection)
        {
            if (principal instanceof SysUser)
            {
                List<SysRole> roles = ((SysUser) principal).getRoles();
                for (SysRole role : roles)
                {
                    role.setPermissions(rolePermissions.get(role));
                }
            }
        }
        rememberSerializedIdentity(subject, bytes);
    }

    /**
     * 取记住我身份时恢复角色permissions权限字符串。
     */
    @Override
    public PrincipalCollection getRememberedPrincipals(SubjectContext subjectContext)
    {
        PrincipalCollection principals;
        try
        {
            principals = super.getRememberedPrincipals(subjectContext);
        }
        catch (CryptoException ex)
        {
            // 密钥变更、Cookie 损坏或与旧版本不兼容时解密失败；清除无效 Cookie，避免每个请求都打 WARN 堆栈
            log.debug("RememberMe Cookie 解密失败，已丢弃: {}", ex.getMessage());
            forgetIdentity(subjectContext);
            return null;
        }
        if (principals == null || principals.isEmpty())
        {
            return principals;
        }
        for (Object principal : principals)
        {
            if (principal instanceof SysUser)
            {
                SpringUtils.getBean(SysLoginService.class).setRolePermission((SysUser) principal);
            }
        }
        return principals;
    }
}
