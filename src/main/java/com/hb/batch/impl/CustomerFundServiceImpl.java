package com.hb.batch.impl;

import com.hb.batch.mapper.CustomerFundMapper;
import com.hb.batch.service.ICustomerFundService;
import com.hb.facade.entity.CustomerFundDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ========== 客户资金信息service实现类 ==========
 *
 * @author Mr.huang
 * @version CustomerFundServiceImpl.java, v1.0
 * @date 2019年06月16日 21时13分
 */
@Service
public class CustomerFundServiceImpl implements ICustomerFundService {

    @Autowired
    private CustomerFundMapper customerFundMapper;

    @Override
    public CustomerFundDO findCustomerFund(CustomerFundDO customerFundDO) {
        return customerFundMapper.findCustomerFund(customerFundDO);
    }

    @Override
    public int updateByPrimaryKeySelective(CustomerFundDO customerFundDO) {
        return customerFundMapper.updateByPrimaryKeySelective(customerFundDO);
    }

}
