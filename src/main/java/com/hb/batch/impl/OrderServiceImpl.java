package com.hb.batch.impl;

import com.hb.batch.vo.RiskControlRequestVO;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ========== 订单服务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.impl.OrderServiceImpl.java, v1.0
 * @date 2019年08月15日 22时12分
 */
@RestController
@RequestMapping("batch/order")
public class OrderServiceImpl {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @PostMapping("/riskControl")
    public void riskControl(@RequestBody RiskControlRequestVO requestVO) {
        String userId = requestVO == null ? "" : requestVO.getUserId();
        if (StringUtils.isBlank(userId)) {
            LOGGER.info("用户ID为空，直接跳出...");
            return;
        }
        LOGGER.info("用户:{}，开始进行批处理任务...");
        /**
         * 1.查询该用户下所有 持仓中或者委托中的订单
         */

        /**
         * 2.根据订单的股票代码查询各股票的实时行情
         */



    }

}
