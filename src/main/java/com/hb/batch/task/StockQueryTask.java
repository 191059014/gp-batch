package com.hb.batch.task;

import com.hb.batch.runable.StockQueryRunnable;
import com.hb.batch.scheduler.ITaskScheduler;
import com.hb.batch.service.IOrderService;
import com.hb.batch.service.IStockListService;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.remote.model.StockModel;
import com.hb.remote.service.IStockService;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * ========== 查询股票任务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.QueryStockTask.java, v1.0
 * @date 2019年08月19日 18时34分
 */
@Component
public class StockQueryTask implements InitializingBean {
    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StockQueryTask.class);

    /**
     * 线程池任务调度
     */
    private ThreadPoolTaskScheduler threadPoolTaskScheduler = null;
    /**
     * 结果
     */
    private Map<String, ScheduledFuture> futureMap = new ConcurrentHashMap<>();
    /**
     * 股票实时信息数据池
     */
    private static volatile Map<String, StockModel> stockMap = new ConcurrentHashMap<>();

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IStockService stockService;

    @Autowired
    private IStockListService stockListService;

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
    }

    /**
     * ########## 开启任务调度 ##########
     *
     * @param cron         表达式
     * @param stockCodeSet 股票代码集合
     * @param userId       用户ID
     */
    public void startTask(String cron, Set<String> stockCodeSet, String userId) {
        Runnable runnable = new StockQueryRunnable(stockCodeSet, stockService, stockListService);
        String taskId = getTaskId(userId);
        iTaskScheduler.start(cron, runnable, taskId, threadPoolTaskScheduler, futureMap);
        LOGGER.info("开启任务调度{}后所有任务：{}", taskId, futureMap.keySet());
    }

    /**
     * ########## 关闭任务调度 ##########
     *
     * @param userId 用户ID
     */
    public void stopTask(String userId) {
        String taskId = getTaskId(userId);
        iTaskScheduler.stop(taskId, futureMap);
        LOGGER.info("关闭任务调度{}后所有任务：{}", taskId, futureMap.keySet());
    }

    /**
     * ########## 添加股票查询任务 ##########
     *
     * @param stockCodeSet 股票代码集合
     */
    public void addStockQueryTask(Set<String> stockCodeSet) {
        if (CollectionUtils.isEmpty(stockCodeSet)) {
            return;
        }
        LOGGER.info("添加股票查询任务：{}", stockCodeSet.size());
        Runnable runnable = new StockQueryRunnable(stockCodeSet, stockService, stockListService);
        runnable.run();
        LOGGER.info("添加股票查询任务完成：{}", stockMap.size());
    }

    /**
     * ########## 获取任务ID ##########
     *
     * @param userId 用户ID
     * @return taskId
     */
    private String getTaskId(String userId) {
        return "stockQueryTask_" + userId;
    }

    /**
     * ########## 更新股票信息集合 ##########
     *
     * @param stockModelList 股票信息集合
     */
    public static void updateStockMap(List<StockModel> stockModelList) {
        if (CollectionUtils.isEmpty(stockModelList)) {
            return;
        }
        stockModelList.forEach(stock -> {
            stock.setLastTime(new Date());
            stockMap.put(stock.getStockCode(), stock);
        });
    }

    public static StockModel getStock(String stockCode) {
        return stockMap.get(stockCode);
    }

}
