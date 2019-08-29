package com.hb.batch.impl;

import com.hb.batch.mapper.StockListMapper;
import com.hb.batch.service.IStockListService;
import com.hb.facade.entity.StockListDO;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ========== 股票相关service实现类 ==========
 *
 * @author Mr.huang
 * @version com.hb.web.api.impl.StockServiceImpl.java, v1.0
 * @date 2019年05月31日 11时06分
 */
@Service
public class StockListServiceImpl implements IStockListService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockListServiceImpl.class);

    @Autowired
    private StockListMapper stockListMapper;

    /**
     * 股票基本信息集合
     */
    private static Map<String, StockListDO> stockListMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<StockListDO> allStockList = getAllStockList();
        allStockList.forEach(stockListDO -> stockListMap.put(stockListDO.getCode(), stockListDO));
        LOGGER.info("加载所有股票基本信息完成");
    }

    @Override
    public StockListDO getStockByCode(String stockCode) {
        Set<String> querys = new HashSet<>();
        querys.add(stockCode);
        List<StockListDO> list = stockListMapper.getStockListByStockCodeSet(querys);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public List<StockListDO> getStockListBySet(Set<String> stockCodeSet) {
        if (CollectionUtils.isEmpty(stockCodeSet)) {
            return null;
        }
        List<StockListDO> result = new ArrayList<>();
        for (String stockCode : stockCodeSet) {
            StockListDO stockListDO = stockListMap.get(stockCode);
            if (stockListDO != null) {
                result.add(stockListDO);
            }
        }
        return result;
    }

    @Override
    public List<StockListDO> findPageList(String queryText, Integer startRow, Integer pageSize) {
        return stockListMapper.findPageList(queryText, startRow, pageSize);
    }

    public List<StockListDO> getAllStockList() {
        return stockListMapper.getAllStockList();
    }

}
