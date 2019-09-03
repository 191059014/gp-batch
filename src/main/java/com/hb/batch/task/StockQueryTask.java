package com.hb.batch.task;

import com.hb.batch.runable.StockQueryRunnable;
import com.hb.batch.scheduler.ITaskScheduler;
import com.hb.remote.model.StockModel;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
     * 股票实时信息数据池
     */
    private static volatile Map<String, StockModel> stockMap = new ConcurrentHashMap<>();

    @Autowired
    private ITaskScheduler iTaskScheduler;

    @Autowired
    @Qualifier("stockQueryTaskScheduler")
    private ThreadPoolTaskScheduler stockQueryTaskScheduler;

    @Value("${stockQueryTask.cron.default}")
    private String defaultCron;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * 更新股票信息
     *
     * @param stockCode 股票代码
     */
    public void updateStockInfo(String stockCode) {
        if (StringUtils.isBlank(stockCode)) {
            return;
        }
        Set<String> stockCodeSet = new HashSet<>();
        stockCodeSet.add(stockCode);
        updateStockInfo(stockCodeSet);
    }

    /**
     * 更新股票信息
     *
     * @param stockCodeSet 股票代码集合
     */
    public void updateStockInfo(Set<String> stockCodeSet) {
        if (CollectionUtils.isEmpty(stockCodeSet)) {
            return;
        }
        LOGGER.info("更新股票信息：{}", stockCodeSet);
        Runnable runnable = new StockQueryRunnable(stockCodeSet);
        stockQueryTaskScheduler.execute(runnable);
    }

    /**
     * ########## 开启任务调度 ##########
     */
    public void startTask() {
        Set<String> stockCodeSet = stockMap.keySet();
        if (CollectionUtils.isEmpty(stockCodeSet)) {
            return;
        }
        Runnable runnable = new StockQueryRunnable(stockCodeSet);
        String taskId = getTaskId();
        iTaskScheduler.start(defaultCron, runnable, taskId, stockQueryTaskScheduler);
    }

    /**
     * ########## 获取任务ID ##########
     *
     * @return taskId
     */
    private String getTaskId() {
        return "stockQueryTask_" + System.currentTimeMillis();
    }

    /**
     * ########## 更新股票信息集合 ##########
     *
     * @param stockModelList 股票信息集合
     */
    public static void updateStockMap(List<StockModel> stockModelList) {
        LOGGER.info("更新股票实时数据池：{}", stockModelList.size());
        if (CollectionUtils.isEmpty(stockModelList)) {
            return;
        }
        stockModelList.forEach(stock -> {
            stock.setLastUpdateTime(new Date());
            stockMap.put(stock.getStockCode(), stock);
        });
    }

    /**
     * 获取股票信息
     *
     * @param stockCode 股票带妈妈
     * @return 股票信息
     */
    public static StockModel getStock(String stockCode) {
        LOGGER.info("股票实时数据池：{}", stockMap.keySet());
        return stockMap.get(stockCode);
    }

}
