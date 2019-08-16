package com.hb.batch.vo;

import java.io.Serializable;

/**
 * ========== 风控请求vo ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.vo.RiskControlRequestVO.java, v1.0
 * @date 2019年08月16日 18时38分
 */
public class RiskControlRequestVO implements Serializable {

    private static final long serialVersionUID = 1935416507070946034L;

    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "RiskControlRequestVO{" +
                "userId='" + userId + '\'' +
                '}';
    }
}
