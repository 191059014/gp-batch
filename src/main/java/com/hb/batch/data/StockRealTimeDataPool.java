package com.hb.batch.data;

import com.hb.remote.model.StockModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ========== 股票实时数据池 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.data.StockRealTimeDataPool.java, v1.0
 * @date 2019年08月20日 01时38分
 */
@Component
public class StockRealTimeDataPool {

    /**
     * 股票实时信息数据池
     */
    private static volatile Map<String, StockModel> stockMap = new ConcurrentHashMap<>();

    /**
     * ########## 更新股票信息集合 ##########
     *
     * @param stockModelList 股票信息集合
     */
    public void updateStockMap(List<StockModel> stockModelList) {
        if (CollectionUtils.isEmpty(stockModelList)) {
            return;
        }
        stockModelList.forEach(stock -> {
            stock.setLastTime(new Date());
            stockMap.put(stock.getStockCode(), stock);
        });
    }

}
