package com.hb.batch.task;

import com.hb.batch.runable.UserRunnable;
import com.hb.batch.service.*;
import com.hb.facade.calc.StockTools;
import com.hb.facade.common.SystemConfig;
import com.hb.facade.entity.*;
import com.hb.facade.enumutil.FundTypeEnum;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.remote.model.StockModel;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.helper.LogHelper;
import com.hb.unic.util.util.BigDecimalUtils;
import com.hb.unic.util.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RiskControlTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRunnable.class);

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private ICustomerFundService iCustomerFundService;

    @Autowired
    private ICustomerFundDetailService iCustomerFundDetailService;

    @Autowired
    private IAgentService iAgentService;

    @Autowired
    private IUserService iUserService;

    /**
     * 监控用户下的订单
     *
     * @param userId    用户ID
     * @param orderList 订单集合
     */
    public void monitorUser(String userId, List<OrderDO> orderList) {
        LOGGER.info("用户ID：{}，风险控制开始，共需处理的订单个数：{}", userId, orderList.size());
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        Set<String> stockCodeSet = new HashSet<>();
        orderList.forEach(orderDO -> stockCodeSet.add(orderDO.getStockCode()));
        for (OrderDO orderDO : orderList) {
            String userName = orderDO.getUserName();
            String orderId = orderDO.getOrderId();
            String stockCode = orderDO.getStockCode();
            try {
                LOGGER.info("订单号：{}，股票代码：{}，风险控制开始", orderId, stockCode);
                StockModel stockModel = StockQueryTask.getStock(stockCode);
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
                if (new Date().after(orderDO.getDelayEndTime())) {
                    //  递延到期，平仓
                    LOGGER.info("用户姓名：{}，订单号：{}，递延到期，进行平仓操作", userName, orderId);
                    completeOrder(orderDO, stockModel);
                    continue;
                }
                BigDecimal buyPrice = orderDO.getBuyPrice();
                Integer buyNumber = orderDO.getBuyNumber();
                boolean earn = false;
                if (currentPrice.compareTo(buyPrice) >= 0) {
                    earn = true;
                }
                BigDecimal strategyMoney = orderDO.getStrategyMoney();
                if (earn) {
                    // 盈利
                    BigDecimal totalProfit = StockTools.calcOrderProfit(buyPrice, currentPrice, buyNumber);
                    BigDecimal maxProfit = BigDecimalUtils.multiply(strategyMoney, SystemConfig.getAppJson().getStopMaxPercent());
                    if (totalProfit.compareTo(maxProfit) >= 0) {
                        // 盈利达到最大限度，平仓
                        LOGGER.info("用户姓名：{}，订单号：{}，当前价格({})已经达到盈利阀值（{}），进行平仓操作", userName, orderId, currentPrice, totalProfit);
                        completeOrder(orderDO, stockModel);
                        continue;
                    }
                } else {
                    // 亏损
                    BigDecimal totalProfit = StockTools.calcOrderProfit(buyPrice, currentPrice, buyNumber);
                    BigDecimal maxProfit = BigDecimalUtils.multiply(strategyMoney, SystemConfig.getAppJson().getStopMinPercent());
                    if (totalProfit.abs().compareTo(maxProfit) >= 0) {
                        // 亏损达到最大限度，平仓
                        LOGGER.info("用户姓名：{}，订单号：{}，当前价格({})已经达到亏损阀值（{}），进行平仓操作", userName, orderId, currentPrice, totalProfit);
                        completeOrder(orderDO, stockModel);
                        continue;
                    }
                }
                LOGGER.info("用户姓名：{}，订单号：{}，股票代码：{}，风险控制结束", userName, orderId, stockCode);
            } catch (Exception e) {
                LOGGER.error("用户：{}，订单号：{}，风控过程中出现异常：{}", userName, orderId, LogHelper.getStackTrace(e));
            }
        }
    }

    /**
     * 是否是强制卖出时间段
     *
     * @return true为强制卖出时间段
     */
    private boolean isForceSellTimeBetween() {
        Date currentDate = DateUtils.getCurrentDate();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String nowStr = hour + "" + minute;
        int now = Integer.parseInt(nowStr);
        if (now >= 1450 && now <= 1455) {
            return true;
        }
        return false;
    }

    /**
     * ########## 完成订单 ##########
     *
     * @param orderDO    订单信息
     * @param stockModel 股票信息
     */
    private void completeOrder(OrderDO orderDO, StockModel stockModel) {
        String userId = orderDO.getUserId();
        // 当前价格
        BigDecimal currentPrice = stockModel.getCurrentPrice();
        // 买入股数
        Integer buyNumber = orderDO.getBuyNumber();
        // 买入价格
        BigDecimal buyPrice = orderDO.getBuyPrice();
        // 利润，盈利为正，亏损为负
        BigDecimal lossOrEarnMoneyTotal = StockTools.calcOrderProfit(buyPrice, currentPrice, buyNumber);
        // 策略本金
        BigDecimal strategyOwnMoney = orderDO.getStrategyOwnMoney();
        // 策略金额
        BigDecimal strategyMoney = orderDO.getStrategyMoney();
        // 买入总金额
        BigDecimal buyPriceTotal = orderDO.getBuyPriceTotal();

        /**
         * 更新订单信息
         */
        BigDecimal profit = StockTools.calcOrderProfit(orderDO.getBuyPrice(), currentPrice, orderDO.getBuyNumber());
        // 卖出 价格
        orderDO.setSellPrice(currentPrice);
        // 卖出总价格
        orderDO.setSellPriceTotal(BigDecimalUtils.add(strategyMoney, profit));
        // 订单状态
        orderDO.setOrderStatus(OrderStatusEnum.ALREADY_SETTLED.getValue());
        // 利润
        orderDO.setProfit(profit);
        // 盈亏率
        orderDO.setProfitRate(StockTools.calcOrderProfitRate(profit, strategyMoney));

        int backDays = StockTools.calcBackDays(orderDO.getCreateTime(), orderDO.getDelayDays());
        LOGGER.info("卖出，需要退还的递延金的天数：{}", backDays);
        BigDecimal backDelayMoney = BigDecimal.ZERO;
        if (backDays > 0) {
            // 退还递延金
            backDelayMoney = StockTools.calcDelayMoney(strategyMoney, backDays, SystemConfig.getAppJson().getDelayMoneyPercent());
            LOGGER.info("卖出，退还递延金：{}", backDelayMoney);
            // 递延金
            orderDO.setDelayMoney(BigDecimalUtils.subtract(orderDO.getDelayMoney(), backDelayMoney));
        }
        LOGGER.info("卖出-更新订单信息：{}", orderDO);
        iOrderService.updateByPrimaryKeySelective(orderDO);

        /**
         * 更新客户资金信息
         * 1.账户总金额变化
         * 2.可用余额变化
         * 3.冻结资金变化
         * 4.累计盈亏变化
         */
        CustomerFundDO query = new CustomerFundDO(userId);
        CustomerFundDO customerFund = iCustomerFundService.findCustomerFund(query);
        customerFund.setAccountTotalMoney(BigDecimalUtils.add(customerFund.getAccountTotalMoney(), profit));
        customerFund.setUsableMoney(BigDecimalUtils.addAll(BigDecimalUtils.DEFAULT_SCALE, customerFund.getUsableMoney(), strategyOwnMoney, profit, backDelayMoney));
        customerFund.setTradeFreezeMoney(BigDecimalUtils.multiply(customerFund.getTradeFreezeMoney(), strategyMoney));
        customerFund.setTotalProfitAndLossMoney(BigDecimalUtils.add(customerFund.getTotalProfitAndLossMoney(), profit));
        LOGGER.info("卖出-更新客户资金信息：{}", customerFund);
        iCustomerFundService.updateByPrimaryKeySelective(customerFund);

        /**
         * 增加退还递延金流水
         */
        UserDO user = iUserService.findUser(new UserDO(userId));
        AgentDO agent = iAgentService.getAgentByInviterMobile(user.getInviterMobile());
        if (backDelayMoney.compareTo(BigDecimal.ZERO) > 0) {
            CustomerFundDetailDO backDelayDetail = new CustomerFundDetailDO();
            backDelayDetail.setUserId(userId);
            backDelayDetail.setUserName(user.getUserName());
            backDelayDetail.setAgentId(agent.getAgentId());
            backDelayDetail.setAgentName(agent.getAgentName());
            backDelayDetail.setHappenMoney(backDelayMoney);
            backDelayDetail.setAfterHappenMoney(backDelayMoney);
            backDelayDetail.setFundType(FundTypeEnum.DELAY_BACK.getValue());
            backDelayDetail.setRemark(FundTypeEnum.DELAY_BACK.getDesc());
            LOGGER.info("卖出-退还递延金流水：{}", backDelayDetail);
            iCustomerFundDetailService.addOne(backDelayDetail);
        }
    }

}
