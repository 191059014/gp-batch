package com.hb.batch.task;

import com.hb.batch.data.StockRealTimeDataPool;
import com.hb.batch.runable.StockQueryRunnable;
import com.hb.batch.scheduler.ITaskScheduler;
import com.hb.batch.service.IOrderService;
import com.hb.batch.service.IStockListService;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.remote.service.IStockService;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * ========== 查询股票任务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.QueryStockTask.java, v1.0
 * @date 2019年08月19日 18时34分
 */
@Component("stockQueryTask")
public class StockQueryTask implements IStockQueryTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockQueryTask.class);

    /**
     * 线程池任务调度
     */
    private ThreadPoolTaskScheduler threadPoolTaskScheduler = null;
    /**
     * 结果
     */
    private Map<String, ScheduledFuture> futureMap = new ConcurrentHashMap<>();

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IStockService stockService;

    @Autowired
    private IStockListService stockListService;

    @Autowired
    private StockRealTimeDataPool stockRealTimeDataPool;

    @Autowired
    private ITaskScheduler iTaskScheduler;

    @Value("${stockQueryTask.cron.default}")
    private String defaultCron;

    @Value("${stockTaskExecutor.thread.core_pool_size}")
    private int poolSize;

    @Value("${stockTaskExecutor.thread.name.prefix}")
    private String threadNamePrefix;

    @Override
    public void afterPropertiesSet() throws Exception {
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(poolSize);
        threadPoolTaskScheduler.setThreadNamePrefix(threadNamePrefix);
        threadPoolTaskScheduler.initialize();
        // 项目启动默认开启任务调度
        openTaskSchedulerDetault();
    }

    private void openTaskSchedulerDetault() {
        Set<Integer> orderStatuSet = new HashSet<>();
        orderStatuSet.add(OrderStatusEnum.IN_THE_POSITION.getValue());
        Set<String> stockCodeSet = orderService.getStockCodeByOrderStatus(orderStatuSet);
        start(defaultCron, stockCodeSet, "system");
    }

    @Override
    public void start(String cron, Set<String> stockCodeSet, String userId) {
        Runnable runnable = new StockQueryRunnable(stockCodeSet, stockService, stockListService, stockRealTimeDataPool);
        String taskId = getTaskId(userId);
        iTaskScheduler.start(cron,runnable,taskId,threadPoolTaskScheduler,futureMap);
        LOGGER.info("start-所有任务：{}",futureMap.keySet());
    }

    @Override
    public void stop(String userId) {
        String taskId = getTaskId(userId);
        iTaskScheduler.stop(taskId,futureMap);
        LOGGER.info("stop-所有任务：{}",futureMap.keySet());
    }

    /** 
     * ########## 获取任务ID ##########
     *
     * @param userId 用户ID
     * @return taskId
     */
    private String getTaskId(String userId) {
        return "stockQueryTask_"+userId;
    }

}
