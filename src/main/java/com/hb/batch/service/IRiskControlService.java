package com.hb.batch.service;

import com.hb.remote.model.StockModel;

import java.util.List;
import java.util.Set;

/**
 * ========== Description ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.service.IRiskControlService.java, v1.0
 * @date 2019年10月11日 15时07分
 */
public interface IRiskControlService {

    List<StockModel> getNotTradeTimeStockInfo(Set<String> stockCodeSet);

}
