package com.hb.batch.data;

import com.hb.batch.scheduler.OrderQueryScheduler;
import com.hb.facade.entity.OrderDO;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ========== 订单实时待处理数据池 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.data.StockRealTimeDataPool.java, v1.0
 * @date 2019年08月20日 01时38分
 */
@Component
public class OrderRealTimeDataPool {

    /**
     * 订单实时待处理数据集合
     */
    private static volatile Map<String, OrderDO> orderMap = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderRealTimeDataPool.class);

    /**
     * ########## 更新订单数据集合 ##########
     *
     * @param orderList 订单数据集合
     */
    public void flush(List<OrderDO> orderList) {
        LOGGER.info("订单数据池flush前长度：{}", orderMap.size());
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            String orderId = order.getOrderId();
            orderMap.put(orderId, order);
            LOGGER.info("订单号：{}，用户名：{}，加入订单数据池", orderId);
        });
        LOGGER.info("订单数据池flush后长度：{}", orderMap.size());
    }

    /**
     * ########## 清空订单待处理数据集合 ##########
     */
    public void clear() {
        orderMap.clear();
        LOGGER.info("订单数据池flush后长度：{}", orderMap.size());
    }

}
