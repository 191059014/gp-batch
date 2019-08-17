package com.hb.batch.vo;

import com.hb.remote.model.StockIndexModel;

import java.io.Serializable;
import java.util.List;

/**
 * ========== 股票指数信息响应VO ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.vo.StockIndexQueryResponseVO.java, v1.0
 * @date 2019年06月27日 21时43分
 */
public class StockIndexQueryResponseVO implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 9218996959806418956L;

    /**
     * 股票指数信息集合
     */
    List<StockIndexModel> stockIndexList;

    public StockIndexQueryResponseVO(List<StockIndexModel> stockIndexList) {
        this.stockIndexList = stockIndexList;
    }

    public List<StockIndexModel> getStockIndexList() {
        return stockIndexList;
    }

    public void setStockIndexList(List<StockIndexModel> stockIndexList) {
        this.stockIndexList = stockIndexList;
    }
}
