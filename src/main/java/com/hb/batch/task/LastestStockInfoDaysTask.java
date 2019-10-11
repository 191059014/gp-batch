package com.hb.batch.task;

import com.hb.batch.service.IOrderService;
import com.hb.batch.service.IStockListService;
import com.hb.facade.calc.StockTools;
import com.hb.facade.constant.GeneralConst;
import com.hb.facade.entity.OrderDO;
import com.hb.facade.entity.StockListDO;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.facade.tool.RedisCacheManage;
import com.hb.remote.model.StockModel;
import com.hb.remote.service.IStockService;
import com.hb.remote.tool.AlarmTools;
import com.hb.unic.cache.service.ICacheService;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("lastestStockInfoDaysTask")
public class LastestStockInfoDaysTask {

    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LastestStockInfoDaysTask.class);

    private static final String LOG_PREFIX = "【LastestStockInfoDaysTask】";

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    public AlarmTools alarmTools;

    /**
     * 股票查询每页条数
     */
    public static final int BATCH_COUNT = 20;

    @Autowired
    private IStockService stockService;

    @Autowired
    private IStockListService stockListService;

    @Autowired
    private ICacheService redisCacheService;

    /**
     * 获取非交易时间股票行情
     */
    public void execute() {
        LOGGER.info("{}当前线程：{}", LOG_PREFIX, Thread.currentThread().getName());
        Set<String> allFullStockCode = stockListService.getAllFullStockCode();
        List<String> stockCodeList = new ArrayList<>(allFullStockCode);
        int total = stockCodeList.size();
        int i = 1;
        List<StockModel> resultList = new ArrayList<>();
        while (true) {
            if (total > i * BATCH_COUNT) {
                List<String> subList = stockCodeList.subList((i - 1) * BATCH_COUNT, i * BATCH_COUNT);
                Set<String> subSet = new HashSet<>(subList);
                List<StockModel> stockModelList = stockService.queryStockList(subSet);
                resultList.addAll(stockModelList);
            } else {
                List<String> subList = stockCodeList.subList((i - 1) * BATCH_COUNT, stockCodeList.size());
                Set<String> subSet = new HashSet<>(subList);
                List<StockModel> stockModelList = stockService.queryStockList(subSet);
                resultList.addAll(stockModelList);
                break;
            }
            i++;
        }
        LOGGER.info("{}刷新非交易时间行情信息：{}", LOG_PREFIX, resultList);
        if (resultList.size() > 0) {
            for (StockModel stockModel : resultList) {
                // 添加最新的
                redisCacheService.set(GeneralConst.NOT_TRADE_STOCK_INFO_CACHE_KEY + stockModel.getStockCode(), stockModel);
            }
        }

    }

}
