/*
 * File name:  ZXLTSelectUserListInfo.java
 * Copyright:  Copyright (c) 2006-2016 hoperun Inc,  All rights reserved
 * Description:  <描述>
 * Author:  wl
 * Last modified date:  2016年11月2日
 * Version:  <版本编号>
 * Edit history:  <修改内容><修改人>
 */
package com.centit.app.cmipmodule.zxlt.response;

import java.io.Serializable;

/**
 * <一句话功能简述>
 * 
 * @Description<功能详细描述>
 * 
 * @author wl
 * @Version [版本号, 2016年11月2日]
 */
public class ZXLTSelectUserListInfo implements Serializable
{
    
    /**
     * ZXLTSelectUserListInfo.java
     */
    private static final long serialVersionUID = 1L;
    
    // "id": "1000004",
    // "icon": "/Asset/resource/upload/photo/photo_lhs@3x.png",
    // "name": "梁总"
    
    private String            id;
    
    private String            icon;
    
    private String            name;
    
    private boolean           isSelect;
    
    /**
     * 获取 isSelect
     * 
     * @return 返回 isSelect
     * @author wl
     */
    public boolean isSelect()
    {
        return isSelect;
    }
    
    /**
     * 设置 isSelect
     * 
     * @param isSelect 对isSelect进行赋值
     * @author wl
     */
    public void setSelect(boolean isSelect)
    {
        this.isSelect = isSelect;
    }
    
    /**
     * 获取 id
     * 
     * @return 返回 id
     * @author wl
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * 设置 id
     * 
     * @param id 对id进行赋值
     * @author wl
     */
    public void setId(String id)
    {
        this.id = id;
    }
    
    /**
     * 获取 icon
     * 
     * @return 返回 icon
     * @author wl
     */
    public String getIcon()
    {
        return icon;
    }
    
    /**
     * 设置 icon
     * 
     * @param icon 对icon进行赋值
     * @author wl
     */
    public void setIcon(String icon)
    {
        this.icon = icon;
    }
    
    /**
     * 获取 name
     * 
     * @return 返回 name
     * @author wl
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * 设置 name
     * 
     * @param name 对name进行赋值
     * @author wl
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
}
