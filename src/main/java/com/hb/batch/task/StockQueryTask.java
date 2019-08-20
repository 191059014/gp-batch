package com.hb.batch.task;

import com.hb.batch.data.StockRealTimeDataPool;
import com.hb.batch.runable.StockQueryRunable;
import com.hb.batch.service.IOrderService;
import com.hb.batch.service.IStockListService;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.remote.service.IStockService;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * ========== 查询股票任务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.QueryStockTask.java, v1.0
 * @date 2019年08月19日 18时34分
 */
@Component
public class StockQueryTask implements ITask {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockQueryTask.class);

    /**
     * 线程池任务调度
     */
    private ThreadPoolTaskScheduler threadPoolTaskScheduler = null;

    /**
     * 结果
     */
    private ScheduledFuture<?> future = null;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IStockService stockService;

    @Autowired
    private IStockListService stockListService;

    @Autowired
    private StockRealTimeDataPool stockRealTimeDataPool;

    @Value("${stockQueryTask.cron.default}")
    private String defaultCron;

    @Override
    public void afterPropertiesSet() throws Exception {
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        // 项目启动默认调用一次
        Set<Integer> orderStatuSet = new HashSet<>();
        orderStatuSet.add(OrderStatusEnum.IN_THE_POSITION.getValue());
        Set<String> stockCodeSet = orderService.getStockCodeByOrderStatus(orderStatuSet);
        start(defaultCron, stockCodeSet);
    }

    @Async("asyncStockTaskExecutor")
    @Override
    public void start(String cron, Set<String> stockCodeSet) {
        Runnable runnable = new StockQueryRunable(stockCodeSet, stockService, stockListService, stockRealTimeDataPool);
        future = threadPoolTaskScheduler.schedule(runnable, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                return new CronTrigger(cron).nextExecutionTime(triggerContext);
            }
        });
    }

    @Override
    public void stop() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

}
