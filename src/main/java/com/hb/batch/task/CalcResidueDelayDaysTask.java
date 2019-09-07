package com.hb.batch.task;

import com.hb.batch.service.IOrderService;
import com.hb.facade.calc.StockTools;
import com.hb.facade.entity.OrderDO;
import com.hb.remote.tool.AlarmTools;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("calcResidueDelayDaysTask")
public class CalcResidueDelayDaysTask {

    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CalcResidueDelayDaysTask.class);

    private static final String LOG_PREFIX = "【CalcResidueDelayDaysTask】";

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private OrderTask orderTask;

    @Autowired
    public AlarmTools alarmTools;

    /**
     * 计算订单剩余递延天数的定时任务
     */
    public void execute() {
        LOGGER.info("{}当前线程：{}", LOG_PREFIX, Thread.currentThread().getName());
        List<OrderDO> orderList = orderTask.getPendingOrderList(null);
        LOGGER.info("{}待处理订单个数：{}", LOG_PREFIX, orderList.size());
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(orderDO -> updateResidueDelayDays(orderDO));
    }

    /**
     * 更新订单剩余递延天数
     *
     * @param orderDO 订单
     */
    private void updateResidueDelayDays(OrderDO orderDO) {
        String orderId = orderDO.getOrderId();
        try {
            int residueDelayDays = StockTools.calcBackDays(orderDO.getBuyTime(), orderDO.getDelayDays());
            residueDelayDays++;
            LOGGER.info("{}订单:{}剩余递延天数：{}", LOG_PREFIX, orderId, residueDelayDays);
            OrderDO update = new OrderDO(orderId);
            update.setResidueDelayDays(residueDelayDays);
            iOrderService.updateByPrimaryKeySelective(update);
            alarmTools.alert("风控", "订单", "计算订单剩余递延天数", "订单【" + orderId + "】剩余递延天数：" + residueDelayDays + "天");
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.info("订单【{}】订单剩余递延天数异常，请及时处理！【{}】", orderId, e.getMessage());
            }
            alarmTools.alert("风控", "订单", "计算订单剩余递延天数", "订单【" + orderId + "】订单剩余递延天数异常，请及时处理！【" + e.getMessage() + "】");
        }

    }


}