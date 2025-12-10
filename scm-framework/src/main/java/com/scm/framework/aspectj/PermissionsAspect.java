package com.scm.framework.aspectj;

import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import com.scm.common.core.context.PermissionContextHolder;
import com.scm.common.utils.StringUtils;

/**
 * 自定义权限拦截器，将权限字符串放到当前请求中以便用于多个角色匹配符合要求的权限
 * 
 * @author scm
 */
@Aspect
@Component
public class PermissionsAspect
{
    @Before("@annotation(controllerRequiresPermissions)")
    public void doBefore(JoinPoint point, RequiresPermissions controllerRequiresPermissions) throws Throwable
    {
        handleRequiresPermissions(point, controllerRequiresPermissions);
    }

    protected void handleRequiresPermissions(final JoinPoint joinPoint, RequiresPermissions requiresPermissions)
    {
        String context = StringUtils.join(requiresPermissions.value(), ",");
        PermissionContextHolder.setContext(Objects.requireNonNull(context));
    }
}
