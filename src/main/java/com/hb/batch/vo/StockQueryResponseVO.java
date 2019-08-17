package com.hb.batch.vo;

import com.hb.remote.model.StockModel;

import java.io.Serializable;
import java.util.List;

/**
 * ========== 股票查询响应VO ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.vo.StockQueryResponseVO.java, v1.0
 * @date 2019年06月27日 21时39分
 */
public class StockQueryResponseVO implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8939156435142530512L;
    /**
     * 股票信息集合
     */
    private List<StockModel> stockList;

    public StockQueryResponseVO(List<StockModel> stockList) {
        this.stockList = stockList;
    }

    public List<StockModel> getStockList() {
        return stockList;
    }

    public void setStockList(List<StockModel> stockList) {
        this.stockList = stockList;
    }
}
