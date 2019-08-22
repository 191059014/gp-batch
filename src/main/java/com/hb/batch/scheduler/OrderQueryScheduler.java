package com.hb.batch.scheduler;

import com.hb.batch.data.OrderRealTimeDataPool;
import com.hb.batch.service.IOrderService;
import com.hb.facade.entity.OrderDO;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ========== 订单查询定时任务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.scheduler.OrderQueryScheduler.java, v1.0
 * @date 2019年08月20日 10时12分
 */
@Component
public class OrderQueryScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderQueryScheduler.class);
    /**
     * 最后一次查询时间
     */
    private static volatile Date lastQueryTime = null;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private OrderRealTimeDataPool orderRealTimeDataPool;

    /**
     * ########## 周一到周五每天8点进行预加载一次待处理的订单数据 ##########
     */
    @Scheduled(cron = "0 30 8 ? * MON-FRI")
    public void dailyPreHandler() {
        long start = System.currentTimeMillis();
        LOGGER.info("预加载待处理的订单数据，批次：dailyPreHandler_" + DateUtils.getCurrentDateStr(DateUtils.YYYYMMDDHHMMSS));
        // 清空
        orderRealTimeDataPool.clear();
        loadPendingOrder(null);
        lastQueryTime = new Date();
        LOGGER.info("预加载待处理的订单数据，耗时：{}毫秒，线程：{}", (System.currentTimeMillis() - start), Thread.currentThread());
    }

    /**
     * ########## 周一到周五每天9点至23点开始，5秒钟轮询一次，查询新增的订单 ##########
     */
    @Scheduled(cron = "0/5 * 9-23 ? * MON-FRI")
    public void queryOrderScheduler() {
        long start = System.currentTimeMillis();
        LOGGER.info("轮询查询订单，批次：queryOrderScheduler_" + DateUtils.getCurrentDateStr(DateUtils.YYYYMMDDHHMMSS));
        loadPendingOrder(lastQueryTime);
        lastQueryTime = new Date();
        LOGGER.info("轮询查询订单，耗时：{}毫秒，线程：{}", (System.currentTimeMillis() - start), Thread.currentThread());
    }

    /**
     * ########## 加载待处理的订单 ##########
     */
    private void loadPendingOrder(Date date) {
        Set<Integer> orderStatuSet = new HashSet<>();
        orderStatuSet.add(OrderStatusEnum.IN_THE_POSITION.getValue());
        List<OrderDO> orderList = orderService.getOrderListByOrderStatusAndTime(orderStatuSet, date);
        LOGGER.info("日期：{}，查询订单结果：{}", date == null ? "" : DateUtils.date2str(date, DateUtils.FORMAT_MS), orderList.size());
        // 刷新
        orderRealTimeDataPool.flush(orderList);
    }

}
