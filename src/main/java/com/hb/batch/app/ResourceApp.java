package com.hb.batch.app;

import com.hb.batch.service.IOrderService;
import com.hb.batch.service.IStockListService;
import com.hb.batch.util.LogUtils;
import com.hb.batch.vo.StockIndexQueryResponseVO;
import com.hb.batch.vo.StockQueryResponseVO;
import com.hb.facade.common.AppResponseCodeEnum;
import com.hb.facade.common.AppResultModel;
import com.hb.facade.entity.StockListDO;
import com.hb.facade.vo.appvo.request.StockQueryPageRequestVO;
import com.hb.facade.vo.appvo.request.StockQueryRequestVO;
import com.hb.facade.vo.appvo.response.QueryStockPagesResponseVO;
import com.hb.remote.model.StockIndexModel;
import com.hb.remote.model.StockModel;
import com.hb.remote.service.IStockService;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * ========== 获取资源 ==========
 *
 * @author Mr.huang
 * @version com.hb.web.android.api.noauth.ResourceApp.java, v1.0
 * @date 2019年08月14日 09时22分
 */
@Api(tags = "[APP]资源")
@RestController
@RequestMapping("app/noauth/resource")
public class ResourceApp extends BaseApp {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceApp.class);

    @Autowired
    private IStockService iStockService;

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private IStockListService stockListService;

    @ApiOperation(value = "根据股票代码获取股票信息")
    @PostMapping("/queryStockList")
    public AppResultModel<StockQueryResponseVO> queryStockList(@RequestBody StockQueryRequestVO stockQueryRequestVO) {
        LOGGER.info(LogUtils.appLog("根据股票代码获取股票信息，入参：{}"), stockQueryRequestVO);
        List<StockModel> stockModels = null;
        try {
            List<StockListDO> stockListDOList = stockListService.getStockListBySet(stockQueryRequestVO.getStockCodeSet());
            if (stockListDOList == null) {
                return AppResultModel.generateResponseData(AppResponseCodeEnum.SUCCESS);
            }
            Set<String> fullCodeList = new HashSet<>();
            for (StockListDO stockListDO : stockListDOList) {
                fullCodeList.add(stockListDO.getFull_code());
            }
            stockModels = iStockService.queryStockList(fullCodeList);
        } catch (Exception e) {
            String stackTrace = LogUtils.getStackTrace(e);
            LOGGER.error(LogUtils.appLog("根据股票代码获取股票信息,系统异常：{}"), stackTrace);
            alarmTools.alert("APP", "股票", "根据股票代码获取股票信息", e.getMessage());
            return AppResultModel.generateResponseData(AppResponseCodeEnum.FAIL);
        }
        LOGGER.info(LogUtils.appLog("根据股票代码获取股票信息，出参：{}"), stockModels);
        return AppResultModel.generateResponseData(AppResponseCodeEnum.SUCCESS, new StockQueryResponseVO(stockModels));
    }

    @ApiOperation(value = "根据指数代码获取指数信息")
    @PostMapping("/queryStockIndexList")
    public AppResultModel<StockIndexQueryResponseVO> queryStockIndexList() {
        Set<String> stockCodeSet = new HashSet<>();
        // 上证指数
        stockCodeSet.add("000001");
        // 深圳成指
        stockCodeSet.add("399001");
        // 创业板指
        stockCodeSet.add("399006");
        List<StockIndexModel> stockIndexModels = null;
        try {
            stockIndexModels = iStockService.queryStockIndexList(stockCodeSet);
        } catch (Exception e) {
            String stackTrace = LogUtils.getStackTrace(e);
            LOGGER.error(LogUtils.appLog("根据指数代码获取指数信息,系统异常：{}"), stackTrace);
            alarmTools.alert("APP", "股票", "根据指数代码获取指数信息", e.getMessage());
            return AppResultModel.generateResponseData(AppResponseCodeEnum.FAIL);
        }
        if (CollectionUtils.isNotEmpty(stockIndexModels)) {
            Collections.sort(stockIndexModels, Comparator.comparing(StockIndexModel::getIndexCode));
        }
        LOGGER.info(LogUtils.appLog("根据指数代码获取指数信息，出参：{}"), stockIndexModels);
        return AppResultModel.generateResponseData(AppResponseCodeEnum.SUCCESS, new StockIndexQueryResponseVO(stockIndexModels));
    }

    @ApiOperation(value = "查询热门股票")
    @PostMapping("/getHotStockList")
    public AppResultModel<StockQueryResponseVO> getHotStockList() {
        // 获取订单中最热门的前几个股票代码
        int number = 9;
        List<StockModel> stockModelList = null;
        try {
            Set<String> stockSet = iOrderService.getHotStockSet(number);
            LOGGER.info(LogUtils.appLog("查询热门股票，股票代码：{}"), stockSet);
            List<StockListDO> stockListDOList = stockListService.getStockListBySet(stockSet);
            LOGGER.info(LogUtils.appLog("查询热门股票，查询数据库结果：{}"), stockListDOList);
            if (CollectionUtils.isEmpty(stockListDOList)) {
                return AppResultModel.generateResponseData(AppResponseCodeEnum.SUCCESS);
            }
            Set<String> fullCodeSet = new HashSet<>();
            for (StockListDO stockListDO : stockListDOList) {
                fullCodeSet.add(stockListDO.getFull_code());
            }
            // 根据股票代码查询信息
            stockModelList = iStockService.queryStockList(fullCodeSet);
        } catch (Exception e) {
            String stackTrace = LogUtils.getStackTrace(e);
            LOGGER.error(LogUtils.appLog("查询热门股票,系统异常：{}"), stackTrace);
            alarmTools.alert("APP", "股票", "查询热门股票", e.getMessage());
            return AppResultModel.generateResponseData(AppResponseCodeEnum.FAIL);
        }
        LOGGER.info(LogUtils.appLog("查询热门股票，出参：{}"), stockModelList);
        return AppResultModel.generateResponseData(AppResponseCodeEnum.SUCCESS, new StockQueryResponseVO(stockModelList));
    }

    @ApiOperation(value = "根据股票代码或者名称模糊搜索股票信息")
    @PostMapping("/findStockPagesByCodeOrName")
    public AppResultModel<QueryStockPagesResponseVO> findStockPagesByCodeOrName(@RequestBody StockQueryPageRequestVO requestVO) {
        LOGGER.info(LogUtils.appLog("根据股票代码模糊搜索股票信息，入参：{}"), requestVO);
        QueryStockPagesResponseVO responseVO = new QueryStockPagesResponseVO();
        String queryText = requestVO.getQueryText();
        if (StringUtils.isBlank(queryText)) {
            return AppResultModel.generateResponseData(AppResponseCodeEnum.FAIL);
        }
        Integer pageSize = requestVO.getPageSize();
        if (pageSize == null || pageSize == 0) {
            pageSize = 100;
        }
        Integer startRow = requestVO.getStartRow() == null ? 0 : requestVO.getStartRow();
        List<StockListDO> pageList = stockListService.findPageList(queryText, startRow, pageSize);
        responseVO.setStockSearchList(pageList);
        LOGGER.info(LogUtils.appLog("根据股票代码模糊搜索股票信息，出参：{}"), responseVO);
        return AppResultModel.generateResponseData(AppResponseCodeEnum.SUCCESS, responseVO);
    }

}
