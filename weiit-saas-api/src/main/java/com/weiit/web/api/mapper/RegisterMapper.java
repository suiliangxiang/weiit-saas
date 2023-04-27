package com.weiit.web.api.mapper;

import com.weiit.core.entity.E;
import com.weiit.core.entity.FormMap;
import com.weiit.core.mapper.BaseMapper;

/**
 * Created by johnluo on 2018/8/15.
 */
public interface RegisterMapper extends BaseMapper{

    E selectMerchantByAccount(FormMap formMap);

    void insetMerch(FormMap formMap);

    /**
     * 查询公司运营部的通知电话号码
     * @param formMap
     * @return
     */
    E selectNotifyPhoneByBusinessType(FormMap formMap);
}
