package com.scm.web.controller.settlement;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 结算查询兼容入口（历史菜单 URL：/settlement/query）
 */
@Controller
@RequestMapping("/settlement")
public class SettlementQueryController
{
    @RequiresPermissions("settlement:settlement:view")
    @GetMapping("/query")
    public String query()
    {
        return "settlement/query";
    }
}

