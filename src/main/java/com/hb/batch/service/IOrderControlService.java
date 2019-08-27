package com.hb.batch.service;

/**
 * ========== 订单风控 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.service.IOrderControlService.java, v1.0
 * @date 2019年08月24日 08时01分
 */
public interface IOrderControlService {

    void riskControl(String userId);

}
