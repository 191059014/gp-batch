package com.hb.batch.task;

import com.hb.batch.runable.OrderQueryRunnable;
import com.hb.batch.scheduler.ITaskScheduler;
import com.hb.facade.entity.OrderDO;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
@Component
public class OrderQueryTask implements InitializingBean {
    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderQueryTask.class);
    /**
     * 最后一次查询时间
     */
    public static volatile Long lastQueryTime = null;
    /**
     * 订单实时待处理数据集合
     */
    private static volatile Map<String, OrderDO> orderMap = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("orderQueryTaskScheduler")
    private ThreadPoolTaskScheduler orderQueryTaskScheduler;

    @Value("${orderQueryTask.cron.default}")
    private String defaultCron;

    @Autowired
    private ITaskScheduler iTaskScheduler;

    @Autowired
    private StockQueryTask stockQueryTask;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * ########## 加载待处理的订单 ##########
     */
    public void loadPendingOrders() {
        Runnable runnable = new OrderQueryRunnable();
        orderQueryTaskScheduler.execute(runnable);
    }

    /**
     * 开始任务
     */
    public void startTask() {
        Runnable runnable = new OrderQueryRunnable();
        iTaskScheduler.start(defaultCron, runnable, getTaskId(), orderQueryTaskScheduler);
    }

    /**
     * 获取任务ID
     *
     * @return 任务ID
     */
    private String getTaskId() {
        return "orderQueryTask_" + System.currentTimeMillis();
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
        Set<String> stockCodeSet = new HashSet<>();
        orderList.forEach(order -> {
            orderMap.put(order.getOrderId(), order);
            stockCodeSet.add(order.getStockCode());
        });
        if (CollectionUtils.isNotEmpty(stockCodeSet)) {
            stockQueryTask.updateStockInfo(stockCodeSet);
        }
        LOGGER.info("新增订单到数据池：{}", orderList.size());
    }

    /**
     * 获取用户和用户下的待处理订单信息集合
     *
     * @return 订单信息集合
     */
    public static Map<String, List<OrderDO>> getUserOrderMap() {
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

}
