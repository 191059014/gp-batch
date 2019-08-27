package com.hb.batch.mapper;

import com.hb.facade.entity.CustomerFundDO;
import org.apache.ibatis.annotations.Param;

public interface CustomerFundMapper {

    int updateByPrimaryKeySelective(CustomerFundDO record);

    CustomerFundDO findCustomerFund(@Param("customerFundDO") CustomerFundDO customerFundDO);

}