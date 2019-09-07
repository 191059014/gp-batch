package com.hb.batch.task;

import com.hb.batch.service.IOrderService;
import com.hb.facade.entity.OrderDO;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.util.DateUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ========== 订单查询任务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.OrderQueryTask.java, v1.0
 * @date 2019年08月28日 11时36分
 */
@Component("orderTask")
public class OrderTask {
    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTask.class);
    /**
     * 最后一次查询时间
     */
    public static volatile Long lastQueryTime = null;
    /**
     * 订单实时待处理数据集合
     */
    private static volatile Map<String, OrderDO> orderMap = new ConcurrentHashMap<>();

    private static final String LOG_PREFIX = "【OrderTask】";

    @Autowired
    private IOrderService orderService;

    public void execute() {
        LOGGER.info("{}当前线程：{}", LOG_PREFIX, Thread.currentThread().getName());
        Date lastQueryDate = null;
        if (lastQueryTime != null) {
            lastQueryDate = new Date(lastQueryTime);
        }
        lastQueryTime = System.currentTimeMillis();
        List<OrderDO> orderList = getPendingOrderList(lastQueryDate);
        LOGGER.info("{}最后一次查询时间：{}，查询待处理订单结果：{}", LOG_PREFIX, lastQueryDate == null ? "" : DateUtils.date2str(lastQueryDate, DateUtils.FORMAT_MS), orderList.size());
        if (CollectionUtils.isNotEmpty(orderList)) {
            flushOrderMap(orderList);
        }
    }

    /**
     * 查询待处理（持仓中）的订单
     *
     * @param lastQueryDate 查询时间
     * @return 待处理订单
     */
    public List<OrderDO> getPendingOrderList(Date lastQueryDate) {
        Set<Integer> orderStatuSet = new HashSet<>();
        orderStatuSet.add(OrderStatusEnum.IN_THE_POSITION.getValue());
        return orderService.getOrderListByOrderStatusAndTime(orderStatuSet, lastQueryDate);
    }

    /**
     * ########## 更新订单数据集合 ##########
     *
     * @param orderList 订单信息集合
     */
    public void flushOrderMap(List<OrderDO> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            orderMap.put(order.getOrderId(), order);
        });
        LOGGER.info("{}新增订单到数据池：{}", LOG_PREFIX, orderList.size());
    }

    /**
     * 获取用户和用户下的待处理订单信息集合
     *
     * @return 订单信息集合
     */
    public Map<String, List<OrderDO>> getUserOrderMap() {
        Map<String, List<OrderDO>> userOrderMap = new HashedMap();
        orderMap.values().forEach(orderDO -> {
            String userId = orderDO.getUserId();
            if (userOrderMap.containsKey(userId)) {
                List<OrderDO> orderList = userOrderMap.get(userId);
                orderList.add(orderDO);
            } else {
                List<OrderDO> orderList = new ArrayList<>();
                orderList.add(orderDO);
                userOrderMap.put(userId, orderList);
            }
        });
        return userOrderMap;
    }

    /**
     * 删除待处理订单
     *
     * @param orderId 订单ID
     */
    public void removeOrder(String orderId) {
        LOGGER.info("{}删除订单：{}", LOG_PREFIX, orderId);
        orderMap.remove(orderId);
    }

    /**
     * 获取股票代码集合
     *
     * @return 股票代码集合
     */
    public Set<String> getStockCodeSet() {
        Set<String> stockCodeSet = new HashSet<>();
        orderMap.values().forEach(orderDO -> stockCodeSet.add(orderDO.getStockCode()));
        LOGGER.info("{}获取股票代码集合：{}", LOG_PREFIX, stockCodeSet);
        return stockCodeSet;
    }

}
