package com.hb.batch.runable;

import com.hb.batch.container.SpringUtil;
import com.hb.batch.service.IOrderService;
import com.hb.batch.task.OrderQueryTask;
import com.hb.facade.entity.OrderDO;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderQueryRunnable implements Runnable {

    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderQueryTask.class);

    // 最后一次查询时间
    private Date lastQueryDate;

    private OrderQueryTask orderQueryTask;

    private IOrderService orderService;

    public OrderQueryRunnable() {
        if (OrderQueryTask.lastQueryTime != null) {
            this.lastQueryDate = new Date(OrderQueryTask.lastQueryTime);
        }
        OrderQueryTask.lastQueryTime = System.currentTimeMillis();
        orderService = SpringUtil.getBean(IOrderService.class);
        orderQueryTask = SpringUtil.getBean(OrderQueryTask.class);
    }

    @Override
    public void run() {
        Set<Integer> orderStatuSet = new HashSet<>();
        orderStatuSet.add(OrderStatusEnum.IN_THE_POSITION.getValue());
        List<OrderDO> orderList = orderService.getOrderListByOrderStatusAndTime(orderStatuSet, lastQueryDate);
        LOGGER.info("日期：{}，查询待处理订单结果：{}", lastQueryDate == null ? "" : DateUtils.date2str(lastQueryDate, DateUtils.FORMAT_MS), orderList.size());
        if (CollectionUtils.isNotEmpty(orderList)) {
            orderQueryTask.flushOrderMap(orderList);
        }
    }

}
