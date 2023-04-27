package com.weiit.web.admin.task.mapper;

import com.weiit.core.entity.E;
import com.weiit.core.entity.FormMap;
import com.weiit.core.mapper.BaseMapper;

import java.util.List;

/**
 * Created by johnluo on 2018/7/7.
 */
public interface OrderConfirmMapper extends BaseMapper {

    List<E> selectOrderUnpaidList(FormMap formMap);
 
}
