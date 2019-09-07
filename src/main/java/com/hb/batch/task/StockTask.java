package com.hb.batch.task;

import com.hb.batch.service.IStockListService;
import com.hb.facade.entity.StockListDO;
import com.hb.remote.model.StockModel;
import com.hb.remote.service.IStockService;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 股票实时信息数据池
     */
    private static volatile Map<String, StockModel> stockMap = new ConcurrentHashMap<>();

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
        LOGGER.info("当前线程：{}", Thread.currentThread().getName());
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
     * ########## 更新股票信息集合 ##########
     *
     * @param stockModelList 股票信息集合
     */
    private void updateStockMap(List<StockModel> stockModelList) {
        LOGGER.info("更新股票实时数据池：{}", stockModelList.size());
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
        LOGGER.info("股票实时数据池：{}", stockMap.keySet());
        return stockMap.get(stockCode);
    }

}
