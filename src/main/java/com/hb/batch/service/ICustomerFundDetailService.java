package com.hb.batch.service;

import com.hb.facade.entity.CustomerFundDetailDO;

/**
 * ========== 客户资金流水service接口 ==========
 *
 * @author Mr.huang
 * @version ICustomerFundDetailService.java, v1.0
 * @date 2019年06月17日 14时31分
 */
public interface ICustomerFundDetailService {

    /**
     * ########## 新增一条客户资金流水 ##########
     *
     * @param customerFundDetailDO 客户资金流水信息
     * @return int
     */
    int addOne(CustomerFundDetailDO customerFundDetailDO);

}
