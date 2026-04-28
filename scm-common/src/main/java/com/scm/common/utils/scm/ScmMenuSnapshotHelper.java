package com.scm.common.utils.scm;

import java.util.LinkedHashMap;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.scm.common.core.domain.entity.SysMenu;

/**
 * 菜单审计快照（不落 children，避免循环）
 */
public final class ScmMenuSnapshotHelper
{
    private ScmMenuSnapshotHelper()
    {
    }

    public static Map<String, Object> toSnapshot(SysMenu m)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        if (m == null)
        {
            return map;
        }
        put(map, "menuId", m.getMenuId());
        put(map, "menuName", m.getMenuName());
        put(map, "parentId", m.getParentId());
        put(map, "orderNum", m.getOrderNum());
        put(map, "url", m.getUrl());
        put(map, "target", m.getTarget());
        put(map, "menuType", m.getMenuType());
        put(map, "visible", m.getVisible());
        put(map, "isRefresh", m.getIsRefresh());
        put(map, "perms", m.getPerms());
        put(map, "icon", m.getIcon());
        put(map, "remark", m.getRemark());
        put(map, "authType", m.getAuthType());
        put(map, "defaultOpenScope", m.getDefaultOpenScope());
        put(map, "hospitalGrantSupplierFlag", m.getHospitalGrantSupplierFlag());
        put(map, "menuBizCategory", m.getMenuBizCategory());
        return map;
    }

    private static void put(Map<String, Object> map, String key, Object val)
    {
        map.put(key, val == null ? "" : val);
    }

    public static String snapshotToJson(SysMenu m)
    {
        return JSON.toJSONString(toSnapshot(m));
    }
}
