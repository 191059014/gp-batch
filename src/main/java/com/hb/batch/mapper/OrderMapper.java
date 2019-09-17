package com.hb.batch.mapper;

import com.hb.facade.entity.OrderDO;
import com.hb.facade.vo.appvo.request.HotStockVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface OrderMapper {

    OrderDO selectByPrimaryKey(String orderId);

    int updateByPrimaryKeySelective(OrderDO record);

    /**
     * ########## 获取热门股票 ##########
     *
     * @return 股票代码集合
     */
    List<HotStockVO> getHotStockList();

    /**
     * ########## 通过用户ID和订单状态集合查询股票代码集合 ##########
     *
     * @param orderStatuSet 订单状态集合
     * @return 股票代码集合
     */
    List<OrderDO> findByUserIdAndOrderStatus(@Param("userId") String userId, @Param("orderStatuSet") Set<Integer> orderStatuSet);

    /**
     * ########## 通过订单状态集合查询股票代码集合 ##########
     *
     * @param orderStatuSet 订单状态集合
     * @return 股票代码集合
     */
    Set<String> getStockCodeByOrderStatus(@Param("orderStatuSet") Set<Integer> orderStatuSet);

    /**
     * ########## 通过订单状态集合查询订单集合 ##########
     *
     * @param orderStatuSet 订单状态集合
     * @return 订单集合
     */
    List<OrderDO> getOrderListByOrderStatusAndTime(@Param("orderStatuSet") Set<Integer> orderStatuSet);
}