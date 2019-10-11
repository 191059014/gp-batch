package com.hb.batch.impl;

import com.alibaba.fastjson.JSON;
import com.hb.batch.service.IRiskControlService;
import com.hb.facade.constant.GeneralConst;
import com.hb.remote.model.StockModel;
import com.hb.unic.cache.service.ICacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ========== Description ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.impl.RiskControlServiceImpl.java, v1.0
 * @date 2019年10月11日 15时07分
 */
@Service
public class RiskControlServiceImpl implements IRiskControlService {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RiskControlServiceImpl.class);

    @Autowired
    private ICacheService redisCacheService;

    /**
     * 从缓存里获取非交易时间的行情数据
     *
     * @param stockCodeSet 股票代码
     * @return 股票信息
     */
    @Override
    public List<StockModel> getNotTradeTimeStockInfo(Set<String> stockCodeSet) {
        LOGGER.info("非交易时间获取股票行情，入参：{}", stockCodeSet);
        List<StockModel> resultList = new ArrayList<>();
        for (String stockCode : stockCodeSet) {
            String stockInfo = redisCacheService.get(GeneralConst.NOT_TRADE_STOCK_INFO_CACHE_KEY + stockCode);
            if (stockInfo == null) {
                continue;
            }
            StockModel stockModel = JSON.parseObject(stockInfo, StockModel.class);
            resultList.add(stockModel);
        }
        LOGGER.info("非交易时间获取股票行情，结果：{}", resultList);
        return resultList;
    }

}
