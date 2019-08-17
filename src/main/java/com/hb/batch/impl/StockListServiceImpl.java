package com.hb.batch.impl;

import com.hb.batch.mapper.StockListMapper;
import com.hb.batch.service.IStockListService;
import com.hb.facade.entity.StockListDO;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ========== 股票相关service实现类 ==========
 *
 * @author Mr.huang
 * @version com.hb.web.api.impl.StockServiceImpl.java, v1.0
 * @date 2019年05月31日 11时06分
 */
@Service
public class StockListServiceImpl implements IStockListService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockListServiceImpl.class);

    @Autowired
    private StockListMapper stockListMapper;

    @Override
    public StockListDO getStockByCode(String stockCode) {
        Set<String> querys = new HashSet<>();
        querys.add(stockCode);
        List<StockListDO> list = stockListMapper.getStockListByStockCodeSet(querys);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public List<StockListDO> getStockListBySet(Set<String> stockCodeSet) {
        return stockListMapper.getStockListByStockCodeSet(stockCodeSet);
    }

    @Override
    public List<StockListDO> findPageList(String stockCode, Integer startRow, Integer pageSize) {
        return stockListMapper.findPageList(stockCode, startRow, pageSize);
    }

}
