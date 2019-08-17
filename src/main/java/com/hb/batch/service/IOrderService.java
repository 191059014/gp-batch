package com.hb.batch.service;

import java.util.Set;

/**
 * ========== 订单服务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.service.IOrderService.java, v1.0
 * @date 2019年08月15日 22时11分
 */
public interface IOrderService {

    /**
     * ########## 获取热门股票 ##########
     *
     * @param number 多少个热门股票
     * @return 股票代码集合
     */
    Set<String> getHotStockSet(int number);

}
