package com.weiit.web.admin.task.mapper;
 
import com.weiit.core.entity.FormMap;
import com.weiit.core.mapper.BaseMapper;
 

/**
 * Created by johnluo on 2018/7/7.
 */
public interface MerchantMapper extends BaseMapper {

	void editMerchantService(FormMap formMap);
}
