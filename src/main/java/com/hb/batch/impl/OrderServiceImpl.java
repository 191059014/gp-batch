package com.hb.batch.impl;

import com.hb.batch.mapper.OrderMapper;
import com.hb.batch.service.IOrderService;
import com.hb.facade.entity.OrderDO;
import com.hb.facade.vo.appvo.request.HotStockVO;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ========== 订单操作service实现类 ==========
 *
 * @author Mr.huang
 * @version com.hb.web.impl.OrderServiceImpl.java, v1.0
 * @date 2019年06月25日 23时26分
 */
@Service
public class OrderServiceImpl implements IOrderService {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);


    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Set<String> getHotStockSet(int number) {
        List<HotStockVO> hotStockList = orderMapper.getHotStockList();
        Collections.sort(hotStockList, new Comparator<HotStockVO>() {
            @Override
            public int compare(HotStockVO o1, HotStockVO o2) {
                return o1.getTotalNum().compareTo(o2.getTotalNum());
            }
        });
        Set<String> set = new HashSet<>();
        for (int i = hotStockList.size() - 1; i > -1; i--) {
            if (i == hotStockList.size() - number - 1) {
                break;
            }
            set.add(hotStockList.get(i).getStockCode());
        }
        LOGGER.info("查询热门股票结果：{}", set);
        return set;
    }

    @Override
    public Set<String> getStockCodeByOrderStatus(Set<Integer> orderStatusSet) {
        if (orderStatusSet == null || orderStatusSet.size() < 1) {
            return null;
        }
        return orderMapper.getStockCodeByOrderStatus(orderStatusSet);
    }

    @Override
    public List<OrderDO> getOrderListByOrderStatusAndTime(Set<Integer> orderStatuSet) {
        if (orderStatuSet == null || orderStatuSet.size() < 1) {
            return null;
        }
        return orderMapper.getOrderListByOrderStatusAndTime(orderStatuSet);
    }

    @Override
    public List<OrderDO> findByUserIdAndOrderStatus(String userId, Set<Integer> orderStatuSet) {
        return orderMapper.findByUserIdAndOrderStatus(userId, orderStatuSet);
    }

    @Override
    public int updateByPrimaryKeySelective(OrderDO orderDO) {
        if (StringUtils.isBlank(orderDO.getOrderId())) {
            return 0;
        }
        return orderMapper.updateByPrimaryKeySelective(orderDO);
    }

}
