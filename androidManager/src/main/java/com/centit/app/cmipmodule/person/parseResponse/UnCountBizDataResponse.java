package com.centit.app.cmipmodule.person.parseResponse;

import com.centit.core.tools.netUtils.baseReqeust.baseParseResponse.RetParseResponse;

public class UnCountBizDataResponse extends RetParseResponse
{
    
    private UnCountDataResponse bizData;
    
    /**
     * 获取 bizData
     * 
     * @return 返回 bizData
     * @author rqj
     */
    public UnCountDataResponse getBizData()
    {
        return bizData;
    }
    
    /**
     * 设置 bizData
     * 
     * @param bizData 对bizData进行赋值
     * @author rqj
     */
    public void setBizData(UnCountDataResponse bizData)
    {
        this.bizData = bizData;
    }
    
}
