package com.hb.batch.impl;

import com.hb.batch.mapper.OrderMapper;
import com.hb.batch.service.IOrderService;
import com.hb.facade.vo.appvo.request.HotStockVO;
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
        return set;
    }

}
