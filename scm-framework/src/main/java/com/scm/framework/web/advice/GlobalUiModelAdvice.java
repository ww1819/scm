package com.scm.framework.web.advice;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.scm.common.config.ScmConfig;

/**
 * 全局页面模型：ICP 备案等站点信息。
 */
@ControllerAdvice
public class GlobalUiModelAdvice
{
    @ModelAttribute
    public void addGlobalUiAttributes(Model model)
    {
        model.addAttribute("icpNo", ScmConfig.getIcpNo());
        model.addAttribute("icpLink", ScmConfig.getIcpLink());
    }
}
