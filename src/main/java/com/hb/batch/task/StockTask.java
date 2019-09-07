package com.hb.batch.task;

import com.hb.batch.service.IStockListService;
import com.hb.batch.util.StockUtils;
import com.hb.facade.entity.StockListDO;
import com.hb.remote.model.StockModel;
import com.hb.remote.service.IStockService;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Component("stockTask")
public class StockTask {
    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StockTask.class);
    /**
     * 股票查询每页条数
     */
    public static final int BATCH_COUNT = 20;

    @Autowired
    private IStockService stockService;

    @Autowired
    private IStockListService stockListService;

    @Autowired
    private OrderTask orderTask;

    @Value("${stock.valid.timeInterval}")
    private Double stockValidTimeInterval;

    /**
     * 股票实时信息数据池
     */
    private static volatile Map<String, StockModel> stockMap = new ConcurrentHashMap<>();

    private static final String LOG_PREFIX = "【StockTask】";

    /**
     * 刷新行情数据
     *
     * @param stockCode 股票代码
     */
    public StockModel flushOne(String stockCode) {
        if (StringUtils.isBlank(stockCode)) {
            return null;
        }
        Set<String> querySet = new HashSet<>();
        querySet.add(stockCode);
        List<StockModel> stockModelList = flush(querySet);
        return CollectionUtils.isEmpty(stockModelList) ? null : stockModelList.get(0);
    }

    /**
     * 刷新行情数据
     *
     * @param querySet 股票代码集合
     */
    public List<StockModel> flush(Set<String> querySet) {
        List<StockListDO> stockListDOList = stockListService.getStockListBySet(querySet);
        List<String> stockCodeList = new ArrayList<>();
        for (StockListDO stockListDO : stockListDOList) {
            stockCodeList.add(stockListDO.getFull_code());
        }
        int total = stockCodeList.size();
        int i = 1;
        List<StockModel> resultList = new ArrayList<>();
        while (true) {
            if (total > i * BATCH_COUNT) {
                List<String> subList = stockCodeList.subList((i - 1) * BATCH_COUNT, i * BATCH_COUNT);
                Set<String> subSet = new HashSet<>(subList);
                List<StockModel> stockModelList = stockService.queryStockList(subSet);
                updateStockMap(stockModelList);
                resultList.addAll(stockModelList);
            } else {
                List<String> subList = stockCodeList.subList((i - 1) * BATCH_COUNT, stockCodeList.size());
                Set<String> subSet = new HashSet<>(subList);
                List<StockModel> stockModelList = stockService.queryStockList(subSet);
                updateStockMap(stockModelList);
                resultList.addAll(stockModelList);
                break;
            }
            i++;
        }
        return resultList;
    }

    /**
     * 定时刷新行情数据
     */
    public void execute() {
        LOGGER.info("{}当前线程：{}", LOG_PREFIX, Thread.currentThread().getName());
        Set<String> stockCodeSet = orderTask.getStockCodeSet();
        if (CollectionUtils.isEmpty(stockCodeSet)) {
            return;
        }
        Set<String> querySet = new HashSet<>();
        stockCodeSet.forEach(stockCode -> {
            StockModel stockModel = stockMap.get(stockCode);
            if (stockModel != null) {
                if (StockUtils.isExpire(stockModel.getLastUpdateTime(), stockValidTimeInterval)) {
                    querySet.add(stockCode);
                }
            } else {
                querySet.add(stockCode);
            }
        });
        LOGGER.info("{}定时刷新行情数据-需要查询的股票：{}", LOG_PREFIX, querySet);
        if (CollectionUtils.isNotEmpty(querySet)) {
            flush(querySet);
        }
    }

    /**
     * ########## 更新股票信息集合 ##########
     *
     * @param stockModelList 股票信息集合
     */
    private void updateStockMap(List<StockModel> stockModelList) {
        LOGGER.info("{}更新股票实时数据池：{}", LOG_PREFIX, stockModelList.size());
        if (CollectionUtils.isEmpty(stockModelList)) {
            return;
        }
        stockModelList.forEach(stock -> {
            stock.setLastUpdateTime(System.currentTimeMillis());
            stockMap.put(stock.getStockCode(), stock);
        });
    }

    /**
     * 获取股票信息
     *
     * @param stockCode 股票信息
     * @return 股票信息
     */
    public StockModel getStock(String stockCode) {
        LOGGER.info("{}获取股票实时信息，股票数据池：{}", LOG_PREFIX, stockMap.keySet());
        StockModel stockModel = stockMap.get(stockCode);
        LOGGER.info("{}获取股票实时信息：{}", LOG_PREFIX, stockModel);
        return stockModel;
    }

}
