package com.hb.batch.service;

import com.hb.facade.entity.CustomerFundDO;

/**
 * ========== 客户资金信息service接口 ==========
 *
 * @author Mr.huang
 * @version ICustomerFundService.java, v1.0
 * @date 2019年06月16日 21时13分
 */
public interface ICustomerFundService {

    /**
     * ########## 查找客户资金信息 ##########
     *
     * @param customerFundDO 客户资金信息
     * @return CustomerFundDO
     */
    CustomerFundDO findCustomerFund(CustomerFundDO customerFundDO);

    /**
     * ########## 更新客户资金信息 ##########
     *
     * @param customerFundDO 客户资金信息
     * @return int
     */
    int updateByPrimaryKeySelective(CustomerFundDO customerFundDO);

}
