package com.hb.batch.util;

import com.hb.batch.task.UserTask;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;

public class StockUtils {

    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StockUtils.class);

    /**
     * 判断股票信息是否过期
     *
     * @param lastUpdateTime         股票信息最后一次更新时间
     * @param stockValidTimeInterval 股票信息有效时间间隔
     * @return true为过期
     */
    public static boolean isExpire(Long lastUpdateTime, Double stockValidTimeInterval) {
        long nowTime = System.currentTimeMillis();
        Double timeInterval = (Double.parseDouble((nowTime - lastUpdateTime) + "")) / 1000;
        LOGGER.info("时间间隔：{}", timeInterval);
        if (timeInterval.compareTo(stockValidTimeInterval) > 0) {
            return true;
        }
        return false;
    }

}
