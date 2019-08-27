package com.hb.batch.service;

import com.hb.facade.entity.OrderDO;

import java.util.Date;
import java.util.List;
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

    /**
     * ########## 通过订单状态集合查询股票代码集合 ##########
     *
     * @param orderStatusSet 订单状态集合
     * @return 股票代码集合
     */
    Set<String> getStockCodeByOrderStatus(Set<Integer> orderStatusSet);

    /**
     * ########## 通过订单状态集合查询订单集合 ##########
     *
     * @param orderStatuSet 订单状态集合
     * @param date          日期
     * @return 订单集合
     */
    List<OrderDO> getOrderListByOrderStatusAndTime(Set<Integer> orderStatuSet, Date date);

    /**
     * ########## 通过用户ID和订单状态集合查询股票代码集合 ##########
     *
     * @param orderStatuSet 订单状态集合
     * @return 股票代码集合
     */
    List<OrderDO> findByUserIdAndOrderStatus(String userId, Set<Integer> orderStatuSet);

    /**
     * ########## 根据主键更新订单 ##########
     *
     * @param orderDO din订单信息
     * @return 更新的行数
     */
    int updateByPrimaryKeySelective(OrderDO orderDO);

}
