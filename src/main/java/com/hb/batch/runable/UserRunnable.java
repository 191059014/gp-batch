package com.hb.batch.runable;

import com.hb.batch.container.SpringUtil;
import com.hb.batch.task.OrderQueryTask;
import com.hb.batch.task.RiskControlTaskService;
import com.hb.facade.calc.StockTools;
import com.hb.facade.entity.OrderDO;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;
import java.util.Map;

/**
 * ========== 用户下订单风控 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.runable.UserRunnable.java, v1.0
 * @date 2019年08月24日 18时37分
 */
public class UserRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRunnable.class);

    private RiskControlTaskService riskControlTaskService;

    private ThreadPoolTaskScheduler userOrderTaskScheduler;

    public UserRunnable() {
        riskControlTaskService = SpringUtil.getBean(RiskControlTaskService.class);
        userOrderTaskScheduler = (ThreadPoolTaskScheduler) SpringUtil.getBean("userOrderTaskScheduler");
    }

    @Override
    public void run() {
        if (!StockTools.stockOnLine()) {
            return;
        }
        Map<String, List<OrderDO>> userOrderMap = OrderQueryTask.getUserOrderMap();
        userOrderMap.forEach((userId, orderList) -> userOrderTaskScheduler.execute(new Runnable() {
            @Override
            public void run() {
                riskControlTaskService.monitorUser(userId, orderList);
            }
        }));

    }

}
