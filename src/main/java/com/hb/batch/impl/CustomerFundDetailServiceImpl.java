package com.hb.batch.impl;

import com.hb.batch.mapper.CustomerFundDetailMapper;
import com.hb.batch.service.ICustomerFundDetailService;
import com.hb.facade.entity.CustomerFundDetailDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ========== 客户资金流水service实现类 ==========
 *
 * @author Mr.huang
 * @version CustomerFundDetailServiceImpl.java, v1.0
 * @date 2019年06月17日 14时32分
 */
@Service
public class CustomerFundDetailServiceImpl implements ICustomerFundDetailService {

    @Autowired
    private CustomerFundDetailMapper customerFundDetailMapper;

    @Override
    public int addOne(CustomerFundDetailDO customerFundDetailDO) {
        return customerFundDetailMapper.insertSelective(customerFundDetailDO);
    }

}
