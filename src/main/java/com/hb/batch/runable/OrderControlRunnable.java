package com.hb.batch.runable;

import com.hb.batch.data.OrderRealTimeDataPool;
import com.hb.batch.data.StockRealTimeDataPool;
import com.hb.batch.service.ICustomerFundDetailService;
import com.hb.batch.service.ICustomerFundService;
import com.hb.batch.service.IOrderService;
import com.hb.batch.task.IStockQueryTask;
import com.hb.facade.entity.CustomerFundDO;
import com.hb.facade.entity.CustomerFundDetailDO;
import com.hb.facade.entity.OrderDO;
import com.hb.facade.enumutil.FundTypeEnum;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.remote.model.StockModel;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.util.BigDecimalUtils;
import com.hb.unic.util.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ========== Description ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.runable.OrderControlRunnable.java, v1.0
 * @date 2019年08月24日 18时37分
 */
public class OrderControlRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderControlRunnable.class);

    private IOrderService iOrderService;

    private OrderRealTimeDataPool orderRealTimeDataPool;

    private StockRealTimeDataPool stockRealTimeDataPool;

    private ICustomerFundService iCustomerFundService;

    private ICustomerFundDetailService iCustomerFundDetailService;

    private IStockQueryTask iStockQueryTask;

    private String userId;

    private String riskMaxPercent;

    private String per_5s;
    private String per_4s;
    private String per_3s;
    private String per_2s;
    private String per_1s;

    public OrderControlRunnable(IOrderService iOrderService, OrderRealTimeDataPool orderRealTimeDataPool, StockRealTimeDataPool stockRealTimeDataPool, ICustomerFundService iCustomerFundService, ICustomerFundDetailService iCustomerFundDetailService, IStockQueryTask iStockQueryTask, String userId, String riskMaxPercent, String per_5s, String per_4s, String per_3s, String per_2s, String per_1s) {
        this.iOrderService = iOrderService;
        this.orderRealTimeDataPool = orderRealTimeDataPool;
        this.stockRealTimeDataPool = stockRealTimeDataPool;
        this.iCustomerFundService = iCustomerFundService;
        this.iCustomerFundDetailService = iCustomerFundDetailService;
        this.iStockQueryTask = iStockQueryTask;
        this.userId = userId;
        this.riskMaxPercent = riskMaxPercent;
        this.per_5s = per_5s;
        this.per_4s = per_4s;
        this.per_3s = per_3s;
        this.per_2s = per_2s;
        this.per_1s = per_1s;
    }

    @Override
    public void run() {
        List<OrderDO> orderList = orderRealTimeDataPool.getOrderListByUserId(userId);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        Set<String> stockCodeSet = new HashSet<>();
        orderList.forEach(orderDO -> stockCodeSet.add(orderDO.getStockCode()));
        for (OrderDO orderDO : orderList) {
            String userName = orderDO.getUserName();
            String orderId = orderDO.getOrderId();
            StockModel stockModel = stockRealTimeDataPool.get(orderDO.getStockCode());
            // 当前价格
            BigDecimal currentPrice = stockModel.getCurrentPrice();
            // 止盈价格
            BigDecimal stopEarnMoney = orderDO.getStopEarnMoney();
            // 止损价格
            BigDecimal stopLossMoney = orderDO.getStopLossMoney();
            if (BigDecimal.ZERO.compareTo(stopEarnMoney) != 0 && currentPrice.compareTo(stopEarnMoney) >= 0) {
                // 当前价格>=止盈价格，平仓
                LOGGER.info("用户姓名：{}，订单号：{}，当前价格({})>=止盈价格({})，进行平仓操作", userName, orderId, currentPrice, stopEarnMoney);
                completeOrder(orderDO, stockModel);
                continue;
            }
            if (BigDecimal.ZERO.compareTo(stopLossMoney) != 0 && currentPrice.compareTo(stopLossMoney) <= 0) {
                // 当前价格<=止损价格，平仓
                LOGGER.info("用户姓名：{}，订单号：{}，当前价格({})<=止损价格({})，进行平仓操作", userName, orderId, currentPrice, stopLossMoney);
                completeOrder(orderDO, stockModel);
                continue;
            }
            BigDecimal buyPrice = orderDO.getBuyPrice();
            Integer buyNumber = orderDO.getBuyNumber();
            if (currentPrice.compareTo(buyPrice) >= 0) {
                continue;
            }
            BigDecimal lossMoneyUnit = BigDecimalUtils.subtract(currentPrice, buyPrice, 4);
            BigDecimal lossMoney = BigDecimalUtils.multiply(lossMoneyUnit, new BigDecimal(buyNumber));
            BigDecimal strategyOwnMoney = orderDO.getStrategyOwnMoney();
            BigDecimal riskPercent = new BigDecimal(riskMaxPercent);
            BigDecimal maxLossMoney = BigDecimalUtils.multiply(strategyOwnMoney, riskPercent);
            BigDecimal stockQueryStrategy = BigDecimalUtils.divide(BigDecimalUtils.divide(lossMoney, maxLossMoney).abs(), riskPercent);
            changeQueryStockStrategy(userId, stockCodeSet, stockQueryStrategy);
            if (BigDecimal.ZERO.compareTo(lossMoney) < 0 && lossMoney.abs().compareTo(maxLossMoney) >= 0) {
                // 平仓
                LOGGER.info("用户姓名：{}，订单号：{}，当前价格({})已经达到亏损阀值（{}），进行平仓操作", userName, orderId, currentPrice, riskMaxPercent);
                completeOrder(orderDO, stockModel);
                continue;
            }
        }
    }

    /**
     * ########## 改变股票查询轮询间隔 ##########
     */
    private void changeQueryStockStrategy(String userId, Set<String> stockCodeSet, BigDecimal abs) {

        if (abs.compareTo(new BigDecimal("0.2")) > 0) {
            iStockQueryTask.stop(userId);
            iStockQueryTask.start(per_5s, stockCodeSet, userId);
        } else if (abs.compareTo(new BigDecimal("0.4")) > 0) {
            iStockQueryTask.stop(userId);
            iStockQueryTask.start(per_4s, stockCodeSet, userId);
        } else if (abs.compareTo(new BigDecimal("0.6")) > 0) {
            iStockQueryTask.stop(userId);
            iStockQueryTask.start(per_3s, stockCodeSet, userId);
        } else if (abs.compareTo(new BigDecimal("0.8")) > 0) {
            iStockQueryTask.stop(userId);
            iStockQueryTask.start(per_2s, stockCodeSet, userId);
        } else if (abs.compareTo(new BigDecimal("0.9")) > 0) {
            iStockQueryTask.stop(userId);
            iStockQueryTask.start(per_1s, stockCodeSet, userId);
        }

    }

    /**
     * ########## 完成订单 ##########
     *
     * @param orderDO    订单信息
     * @param stockModel 股票信息
     */
    private void completeOrder(OrderDO orderDO, StockModel stockModel) {
        // 当前价格
        BigDecimal currentPrice = stockModel.getCurrentPrice();
        // 买入股数
        BigDecimal buyNumber = new BigDecimal(orderDO.getBuyNumber());
        // 买入价格
        BigDecimal buyPrice = orderDO.getBuyPrice();
        // 盈亏的总金额，盈利为正，亏损为负
        BigDecimal lossOrEarnMoneyTotal = BigDecimalUtils.multiply(BigDecimalUtils.subtract(currentPrice, buyPrice, 4), buyNumber);
        // 策略本金
        BigDecimal strategyOwnMoney = orderDO.getStrategyOwnMoney();
        // 策略金额
        BigDecimal strategyMoney = orderDO.getStrategyMoney();
        // 买入总金额
        BigDecimal buyPriceTotal = orderDO.getBuyPriceTotal();
        /**
         * 更新订单
         */
        String userId = orderDO.getUserId();
        String orderId = orderDO.getOrderId();
        OrderDO update = new OrderDO(orderId, userId);
        // 卖出价格
        update.setSellPrice(currentPrice);
        // 卖出总价格 = 买入总金额+盈亏的总金额
        BigDecimal sellPriceTotal = BigDecimalUtils.add(buyPriceTotal, lossOrEarnMoneyTotal);
        update.setSellPriceTotal(sellPriceTotal);
        // 订单状态
        update.setOrderStatus(OrderStatusEnum.ALREADY_SETTLED.getValue());
        // 利润
        update.setProfit(lossOrEarnMoneyTotal);
        // 盈亏率 = 盈亏总金额/买入总金额
        BigDecimal profitRate = BigDecimalUtils.divide(lossOrEarnMoneyTotal, buyPriceTotal, 4);
        update.setProfitRate(profitRate);
        int updateOrderResult = iOrderService.updateByPrimaryKeySelective(update);
        LOGGER.info("用户姓名：{}，订单号：{}，更新订单：{}", userId, orderId, update);
        if (updateOrderResult <= 0) {
            return;
        }
        // 递延金总金额 = 递延金*天数
        BigDecimal delayMoney = orderDO.getDelayMoney();
        Date createTime = orderDO.getCreateTime();
        int daysBetween = DateUtils.getDaysBetween(new Date(), createTime);
        BigDecimal delayMoneyTotal = BigDecimalUtils.multiply(delayMoney, new BigDecimal(daysBetween));
        /**
         * 更新客户账户信息
         */
        CustomerFundDO query = new CustomerFundDO(userId);
        CustomerFundDO customerFund = iCustomerFundService.findCustomerFund(query);
        CustomerFundDO updateFund = new CustomerFundDO(userId);
        // 总变化量 = 盈亏总金额-信息服务费-递延金
        BigDecimal changeMoneyTotal = BigDecimalUtils.subtract(BigDecimalUtils.subtract(lossOrEarnMoneyTotal, delayMoneyTotal), orderDO.getServiceMoney());
        // 账户总金额 = 原账户总金额+总变化量
        updateFund.setAccountTotalMoney(BigDecimalUtils.add(customerFund.getAccountTotalMoney(), changeMoneyTotal));
        // 冻结金额 = 原冻结金额-订单的策略本金
        updateFund.setFreezeMoney(BigDecimalUtils.subtract(customerFund.getFreezeMoney(), strategyOwnMoney));
        // 可用余额 = 原可用余额+订单的策略本金+总变化量
        BigDecimal newUseableMoney = BigDecimalUtils.add(customerFund.getUsableMoney(), BigDecimalUtils.add(strategyOwnMoney, changeMoneyTotal));
        updateFund.setUsableMoney(newUseableMoney);
        // 累计盈亏 = 原累计盈亏+此次盈亏
        updateFund.setTotalProfitAndLossMoney(BigDecimalUtils.add(customerFund.getTotalProfitAndLossMoney(), lossOrEarnMoneyTotal));
        // 累计信息服务费 = 原累计信息服务费+此次信息服务费
        updateFund.setTotalMessageServiceMoney(BigDecimalUtils.add(customerFund.getTotalMessageServiceMoney(), orderDO.getServiceMoney()));
        int updateCustomerFundResult = iCustomerFundService.updateByPrimaryKeySelective(updateFund);
        LOGGER.info("用户姓名：{}，订单号：{}，更新客户资金信息：{}", userId, orderId, updateFund);
        if (updateCustomerFundResult <= 0) {
            return;
        }
        /**
         * 更新客户资金流水信息
         */
        CustomerFundDetailDO add = new CustomerFundDetailDO();
        add.setUserId(userId);
        add.setUserName(orderDO.getUserName());
        add.setHappenMoney(delayMoneyTotal);
        add.setFundType(FundTypeEnum.DEFERRED_FEE.getValue());
        add.setRemark("递延费扣除");
        iCustomerFundDetailService.addOne(add);
        LOGGER.info("用户姓名：{}，订单号：{}，新增客户资金流水：{}", userId, orderId, add);
        CustomerFundDetailDO add1 = new CustomerFundDetailDO();
        add1.setUserId(userId);
        add1.setUserName(orderDO.getUserName());
        add1.setHappenMoney(orderDO.getServiceMoney());
        add1.setFundType(FundTypeEnum.FREEZE.getValue());
        add1.setRemark("服务费扣除");
        iCustomerFundDetailService.addOne(add1);
        LOGGER.info("用户姓名：{}，订单号：{}，新增客户资金流水：{}", userId, orderId, add1);
    }

}
