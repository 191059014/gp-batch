package com.hb.batch.task;

import com.hb.batch.service.IOrderService;
import com.hb.facade.entity.OrderDO;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ========== 订单查询任务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.OrderQueryTask.java, v1.0
 * @date 2019年08月28日 11时36分
 */
@Component
public class OrderQueryTask implements InitializingBean {
    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderQueryTask.class);
    /**
     * 最后一次查询时间
     */
    private static volatile Long lastQueryTime = null;
    /**
     * 订单实时待处理数据集合
     */
    private static volatile Map<String, OrderDO> orderMap = new ConcurrentHashMap<>();
    /**
     * 股票代码集合
     */
    private static volatile Set<String> stockCodeSet = new HashSet<>();
    /**
     * 用户ID集合
     */
    private static volatile Set<String> userIdSet = new HashSet<>();

    @Autowired
    private IOrderService orderService;

    @Autowired
    private UserOrderTask userOrderTask;

    @Autowired
    private StockQueryTask stockQueryTask;

    @Override
    public void afterPropertiesSet() throws Exception {
        loadPendingOrders(null);
    }

    @Scheduled(cron = "0/5 * 9-23 ? * MON-FRI")
    public void loadPendingOrdersScheduler() {
        loadPendingOrders(lastQueryTime);
    }

    /**
     * ########## 加载待处理的订单 ##########
     *
     * @param queryAfterTime 如果不为null，则查询queryAfterTime之后的订单
     */
    private void loadPendingOrders(Long queryAfterTime) {
        Date afterTime = null;
        if (queryAfterTime != null) {
            afterTime = new Date(queryAfterTime);
        }
        // 更新最后一次查询时间
        lastQueryTime = System.currentTimeMillis();
        Set<Integer> orderStatuSet = new HashSet<>();
        orderStatuSet.add(OrderStatusEnum.IN_THE_POSITION.getValue());
        List<OrderDO> orderList = orderService.getOrderListByOrderStatusAndTime(orderStatuSet, afterTime);
        LOGGER.info("日期：{}，查询待处理订单结果：{}", afterTime == null ? "" : DateUtils.date2str(afterTime, DateUtils.FORMAT_MS), orderList.size());
        if (CollectionUtils.isNotEmpty(orderList)) {
            LOGGER.info("刷新前，订单集合orderMap长度：{}，用户ID集合userIdSet长度：{}", orderMap.size(), userIdSet.size());
            flushOrderMap(orderList);
            flushUserIdSet(orderList);
            LOGGER.info("刷新后，订单集合orderMap长度：{}，用户ID集合userIdSet长度：{}", orderMap.size(), userIdSet.size());
        }
    }

    /**
     * ########## 更新订单数据集合 ##########
     *
     * @param orderList 订单信息集合
     */
    private void flushOrderMap(List<OrderDO> orderList) {
        Set<String> needQueryStockCodeSet = new HashSet<>();
        orderList.forEach(order -> {
            String stockCode = order.getStockCode();
            if (!stockCodeSet.contains(stockCode)) {
                needQueryStockCodeSet.add(stockCode);
            }
            String orderId = order.getOrderId();
            orderMap.put(orderId, order);
            stockCodeSet.add(stockCode);
        });
        if (CollectionUtils.isNotEmpty(needQueryStockCodeSet)) {
            stockQueryTask.addStockQueryTask(needQueryStockCodeSet);
        }
        LOGGER.info("新增订单到数据池：{}", orderList.size());
    }

    /**
     * ########## 更新用户ID集合 ##########
     *
     * @param orderList 订单信息集合
     */
    private void flushUserIdSet(List<OrderDO> orderList) {
        orderList.forEach(order -> {
            String userId = order.getUserId();
            if (!userIdSet.contains(userId)) {
                userIdSet.add(userId);
                userOrderTask.addUserTask(userId);
                LOGGER.info("用户ID：{}，用户名：{}，加入用户ID数据池", userId, order.getUserName());
            }
        });
    }

    /**
     * ########## 根据用户ID查询待处理订单 ##########
     *
     * @param userId 用户ID
     * @return 待处理订单集合
     */
    public static List<OrderDO> getOrderListByUserId(String userId) {
        List<OrderDO> doList = orderMap.values()
                .stream()
                .filter(orderDO -> StringUtils.equals(orderDO.getUserId(), userId))
                .collect(Collectors.toList());
        LOGGER.info("用户ID：{}，待处理订单：{}", userId, doList.size());
        return doList;
    }

}
