package com.hb.batch.controller;

import com.hb.batch.app.BaseApp;
import com.hb.batch.service.IStockListService;
import com.hb.facade.calc.StockTools;
import com.hb.facade.common.ResponseData;
import com.hb.facade.common.ResponseEnum;
import com.hb.facade.entity.OrderDO;
import com.hb.facade.entity.StockListDO;
import com.hb.facade.vo.webvo.response.BackRiskControlResponseVO;
import com.hb.remote.model.StockModel;
import com.hb.remote.service.IStockService;
import com.hb.unic.util.util.BigDecimalUtils;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("controller/riskController")
public class RiskController {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApp.class);

    @Autowired
    private IStockService iStockService;

    @Autowired
    private IStockListService stockListService;

    @ApiOperation(value = "计算当前时间的利润")
    @RequestMapping("/calcProfitCurrentTime")
    public ResponseData<List<BackRiskControlResponseVO>> calcProfitCurrentTime(@RequestBody List<OrderDO> orderList) {
        if (orderList == null || orderList.size() < 1 || orderList.size() > 20) {
            LOGGER.info("计算当前时间的利润，参数不通过：{}", orderList);
            return ResponseData.generateResponseData(ResponseEnum.ERROR_PARAM_VERIFY);
        }
        Set<String> stockCodeSet = orderList.stream().map(OrderDO::getStockCode).collect(Collectors.toSet());
        List<StockListDO> stockListDOList = stockListService.getStockListBySet(stockCodeSet);
        if (stockListDOList == null) {
            return ResponseData.generateResponseData(ResponseEnum.SUCCESS);
        }
        Set<String> fullCodeList = new HashSet<>();
        for (StockListDO stockListDO : stockListDOList) {
            fullCodeList.add(stockListDO.getFull_code());
        }
        List<StockModel> stockModelList = iStockService.queryStockList(fullCodeList);
        Map<String, StockModel> stockModelMap = stockModelList.stream().collect(Collectors.toMap(StockModel::getStockCode, s -> s, (k1, k2) -> k2));
        List<BackRiskControlResponseVO> resultList = new ArrayList<>();
        BackRiskControlResponseVO responseVO = null;
        for (OrderDO orderDO : orderList) {
            responseVO = new BackRiskControlResponseVO(orderDO.getOrderId());
            BigDecimal currentPrice = stockModelMap.get(orderDO.getStockCode()).getCurrentPrice();
            int i = new Random().nextInt(10);
            currentPrice = BigDecimalUtils.add(currentPrice,new BigDecimal(i));
            BigDecimal profit = StockTools.calcOrderProfit(orderDO.getBuyPrice(), currentPrice, orderDO.getBuyNumber());
            responseVO.setCurrentPrice(currentPrice);
            responseVO.setProfit(profit);
            resultList.add(responseVO);
        }
        LOGGER.info("计算当前时间的利润，结果：{}", resultList);
        return ResponseData.generateResponseData(ResponseEnum.SUCCESS, resultList);
    }

}
