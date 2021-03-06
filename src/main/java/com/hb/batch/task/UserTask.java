package com.hb.batch.task;

import com.hb.batch.service.*;
import com.hb.batch.util.LogUtils;
import com.hb.batch.util.StockUtils;
import com.hb.facade.calc.StockTools;
import com.hb.facade.common.SystemConfig;
import com.hb.facade.entity.*;
import com.hb.facade.enumutil.FundTypeEnum;
import com.hb.facade.enumutil.OrderStatusEnum;
import com.hb.facade.tool.RedisCacheManage;
import com.hb.remote.model.StockModel;
import com.hb.remote.tool.AlarmTools;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.helper.LogHelper;
import com.hb.unic.util.util.BigDecimalUtils;
import com.hb.unic.util.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * ========== 用户任务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.UserTask.java, v1.0
 * @date 2019年08月24日 18时09分
 */
@Component("userTask")
public class UserTask {
    /**
     * the common log
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserTask.class);

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

    @Autowired
    private RedisCacheManage redisCacheManage;

    @Autowired
    private OrderTask orderTask;

    @Autowired
    private StockTask stockTask;

    @Autowired
    public AlarmTools alarmTools;

    @Value("${stock.valid.timeInterval}")
    private Double stockValidTimeInterval;

    private static final String LOG_PREFIX = "【UserTask】";

    /**
     * 用户定时任务
     */
    public void execute() {
        LOGGER.info("{}当前线程：{}", LOG_PREFIX, Thread.currentThread().getName());
        Map<String, List<OrderDO>> userOrderMap = orderTask.getUserOrderMap();
        userOrderMap.forEach((userId, orderList) -> {
            UserDO userDO = redisCacheManage.getUserCache(userId);
            if (userDO == null) {
                userDO = iUserService.findUser(new UserDO(userId));
                if (userDO != null) {
                    redisCacheManage.setUserCache(userDO);
                }
            }
            AgentDO agentDO = redisCacheManage.getAgentCache(userDO.getInviterMobile());
            if (agentDO == null) {
                agentDO = iAgentService.findAgent(new AgentDO(null, userDO.getInviterMobile()));
                if (agentDO != null) {
                    redisCacheManage.setAgentCache(agentDO);
                }
            }
            monitorUser(userDO, agentDO, orderList);
        });
    }

    /**
     * 监控用户下的订单
     *
     * @param userDO    用户ID
     * @param agentDO   代理商信息
     * @param orderList 订单集合
     */
    public void monitorUser(UserDO userDO, AgentDO agentDO, List<OrderDO> orderList) {
        LOGGER.info("{}用户：{}，风险控制开始，共需处理的订单个数：{}", LOG_PREFIX, userDO.getUserName(), orderList.size());
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        Set<String> stockCodeSet = new HashSet<>();
        orderList.forEach(orderDO -> stockCodeSet.add(orderDO.getStockCode()));
        for (OrderDO orderDO : orderList) {
            String userName = userDO.getUserName();
            String orderId = orderDO.getOrderId();
            String stockCode = orderDO.getStockCode();
            if (StockTools.todayIsBuyDate(orderDO.getBuyTime())) {
                // 买入当天不走风控
                continue;
            }
            try {
                LOGGER.info("{}订单号：{}，股票代码：{}，风险控制开始", LOG_PREFIX, orderId, stockCode);
                StockModel stockModel = stockTask.getStock(stockCode);
                if (stockModel == null) {
                    stockModel = stockTask.flushOne(stockCode);
                    if (stockModel == null) {
                        LOGGER.error("{}查询不到股票：{}的行情信息！", LOG_PREFIX, stockCode);
                    }
                }
                if (StockUtils.isExpire(stockModel.getLastUpdateTime(), stockValidTimeInterval)) {
                    LOGGER.info("{}股票{}行情刷新，间隔大于有效期", LOG_PREFIX, stockCode);
                    stockModel = stockTask.flushOne(stockCode);
                }
                // 当前价格
                BigDecimal currentPrice = stockModel.getCurrentPrice();
                if (BigDecimal.ZERO.compareTo(currentPrice) == 0) {
                    // 防止网络波动，查询出来的行情不正确
                    continue;
                }
                // 止盈价格
                BigDecimal stopEarnMoney = orderDO.getStopEarnMoney();
                // 止损价格
                BigDecimal stopLossMoney = orderDO.getStopLossMoney();
                if (BigDecimal.ZERO.compareTo(stopEarnMoney) != 0 && currentPrice.compareTo(stopEarnMoney) >= 0) {
                    // 当前价格>=止盈价格，平仓
                    String message = LOG_PREFIX + "用户【" + userName + "】，订单号【" + orderId + "】，当前价格【" + currentPrice + "】>=止盈价格【" + stopEarnMoney + "】，进行强制平仓，请及时处理！";
                    LOGGER.info(message);
                    alarmTools.alert("风控", "订单", "用户订单", message);
                    completeOrder(orderDO, stockModel, userDO, agentDO);
                    continue;
                }
                if (BigDecimal.ZERO.compareTo(stopLossMoney) != 0 && currentPrice.compareTo(stopLossMoney) <= 0) {
                    // 当前价格<=止损价格，平仓
                    String message = LOG_PREFIX + "用户【" + userName + "】，订单号【" + orderId + "】，当前价格【" + currentPrice + "】<=止损价格【" + stopLossMoney + "】，进行强制平仓，请及时处理！";
                    LOGGER.info(message);
                    alarmTools.alert("风控", "订单", "用户订单", message);
                    completeOrder(orderDO, stockModel, userDO, agentDO);
                    continue;
                }
                if (new Date().after(orderDO.getDelayEndTime())) {
                    //  递延到期，平仓
                    String delayEndTime = DateUtils.date2str(orderDO.getDelayEndTime(), DateUtils.DEFAULT_FORMAT);
                    String message = LOG_PREFIX + "用户【" + userName + "】，订单号【" + orderId + "】，递延到期【截止时间：" + delayEndTime + "】，进行强制平仓，请及时处理！";
                    LOGGER.info(message);
                    alarmTools.alert("风控", "订单", "用户订单", message);
                    completeOrder(orderDO, stockModel, userDO, agentDO);
                    continue;
                }
                BigDecimal buyPrice = orderDO.getBuyPrice();
                Integer buyNumber = orderDO.getBuyNumber();
                boolean earn = false;
                if (currentPrice.compareTo(buyPrice) >= 0) {
                    earn = true;
                }
                BigDecimal strategyOwnMoney = orderDO.getStrategyOwnMoney();
                BigDecimal strategyMoney = orderDO.getStrategyMoney();
                if (earn) {
                    // 盈利
                    BigDecimal totalProfit = StockTools.calcOrderProfit(buyPrice, currentPrice, buyNumber);
                    BigDecimal maxProfit = BigDecimalUtils.multiply(strategyMoney, SystemConfig.getAppJson().getStopMaxPercent());
                    if (totalProfit.compareTo(maxProfit) >= 0) {
                        // 盈利达到最大限度，平仓
                        String message = LOG_PREFIX + "用户【" + userName + "】，订单号【" + orderId + "】，当前价格【" + currentPrice + "】，已经达到盈利阀值【" + totalProfit + "】，进行强制平仓，请及时处理！";
                        LOGGER.info(message);
                        alarmTools.alert("风控", "订单", "用户订单", message);
                        completeOrder(orderDO, stockModel, userDO, agentDO);
                        continue;
                    }
                } else {
                    // 亏损
                    BigDecimal totalProfit = StockTools.calcOrderProfit(buyPrice, currentPrice, buyNumber);
                    BigDecimal maxProfit = BigDecimalUtils.multiply(strategyOwnMoney, SystemConfig.getAppJson().getStopMinPercent());
                    if (totalProfit.abs().compareTo(maxProfit) >= 0) {
                        // 亏损达到最大限度，平仓
                        String message = LOG_PREFIX + "用户【" + userName + "】，订单号【" + orderId + "】，当前价格【" + currentPrice + "】，已经达到亏损阀值【" + totalProfit + "】，进行强制平仓，请及时处理！";
                        LOGGER.info(message);
                        alarmTools.alert("风控", "订单", "用户订单", message);
                        completeOrder(orderDO, stockModel, userDO, agentDO);
                        continue;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("{}用户：{}，订单号：{}，风控过程中出现异常：{}", LOG_PREFIX, userName, orderId, LogHelper.getStackTrace(e));
            }
        }
    }

    /**
     * 平仓
     *
     * @param orderDO    订单信息
     * @param stockModel 股票信息
     * @param user       用户信息
     * @param agent      代理商信息
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public synchronized void completeOrder(OrderDO orderDO, StockModel stockModel, UserDO user, AgentDO agent) {
        String userId = user.getUserId();
        String userName = user.getUserName();
        BigDecimal strategyMoney = orderDO.getStrategyMoney();
        BigDecimal strategyOwnMoney = orderDO.getStrategyOwnMoney();
        BigDecimal currentPrice = stockModel.getCurrentPrice();
        /**
         * 更新订单信息
         */
        BigDecimal profit = StockTools.calcOrderProfit(orderDO.getBuyPrice(), currentPrice, orderDO.getBuyNumber());
        // 卖出 价格
        orderDO.setSellPrice(currentPrice);
        // 卖出股数
        orderDO.setSellNumber(orderDO.getBuyNumber());
        // 卖出总价格
        orderDO.setSellPriceTotal(BigDecimalUtils.add(strategyMoney, profit));
        // 卖出时间
        orderDO.setSellTime(new Date());
        // 订单状态
        orderDO.setOrderStatus(OrderStatusEnum.ALREADY_SELL.getValue());
        // 利润
        orderDO.setProfit(profit);
        // 盈亏率
        orderDO.setProfitRate(StockTools.calcOrderProfitRate(profit, strategyMoney));
        int backDays = StockTools.calcBackDays(orderDO.getCreateTime(), orderDO.getDelayDays());
        // 计算退还递延天数和已递延天数
        Calendar c1 = Calendar.getInstance();
        c1.setTime(new Date());
        int nowDate = c1.get(Calendar.DATE);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(orderDO.getBuyTime());
        int buyDate = c2.get(Calendar.DATE);
        Calendar c3 = Calendar.getInstance();
        c3.setTime(orderDO.getDelayEndTime());
        int delayEndDate = c3.get(Calendar.DATE);
        if (nowDate == buyDate || nowDate == delayEndDate) {
            // 当前时间是卖出日期，退还为0，已递延为递延总天数-1-退换天数
            backDays = 0;
            orderDO.setAlreadyDelayDays(orderDO.getDelayDays() - 1);
        } else {
            orderDO.setAlreadyDelayDays(orderDO.getDelayDays() - 1 - backDays);
        }
        LOGGER.info(LogUtils.appLog("卖出，需要退还的递延金的天数：{}"), backDays);
        BigDecimal backDelayMoney = BigDecimal.ZERO;
        // 退换的递延天数
        orderDO.setBackDelayDays(backDays);
        if (backDays > 0) {
            // 退还递延金
            backDelayMoney = StockTools.calcDelayMoney(strategyMoney, backDays, SystemConfig.getAppJson().getDelayMoneyPercent());
            LOGGER.info(LogUtils.appLog("卖出，退还递延金：{}"), backDelayMoney);
            // 退还的递延金
            orderDO.setBackDelayMoney(backDelayMoney);
            // 递延金
            orderDO.setDelayMoney(BigDecimalUtils.subtract(orderDO.getDelayMoney(), backDelayMoney));
        }
        LOGGER.info(LogUtils.appLog("卖出-更新订单信息：{}"), orderDO);
        orderDO.setUpdateTime(new Date());
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
        // 账户总金额=原账户总金额+利润+退还的递延金
        BigDecimal add = BigDecimalUtils.addAll(BigDecimalUtils.DEFAULT_SCALE, customerFund.getAccountTotalMoney(), profit, backDelayMoney);
        customerFund.setAccountTotalMoney(add);
        // 可用余额=原可用余额+利润+退还的递延金+策略本金
        customerFund.setUsableMoney(BigDecimalUtils.addAll(BigDecimalUtils.DEFAULT_SCALE, customerFund.getUsableMoney(), strategyOwnMoney, profit, backDelayMoney));
        // 交易冻结金额=原交易冻结金额-策略本金
        customerFund.setTradeFreezeMoney(BigDecimalUtils.subtractAll(BigDecimalUtils.DEFAULT_SCALE, customerFund.getTradeFreezeMoney(), strategyOwnMoney));
        // 总盈亏=原总盈亏+利润-服务费-递延费
        BigDecimal add1 = BigDecimalUtils.add(customerFund.getTotalProfitAndLossMoney(), profit);
        BigDecimal net = BigDecimalUtils.subtractAll(BigDecimalUtils.DEFAULT_SCALE, add1, orderDO.getServiceMoney(), orderDO.getDelayMoney());
        customerFund.setTotalProfitAndLossMoney(net);
        // 累计持仓市值总金额=原累计持仓市值总金额-持仓市值
        customerFund.setTotalStrategyMoney(BigDecimalUtils.subtract(customerFund.getTotalStrategyMoney(), strategyMoney));
        // 累计持仓信用金总金额=原累计持仓信用金总金额-持仓信用金
        customerFund.setTotalStrategyOwnMoney(BigDecimalUtils.subtractAll(BigDecimalUtils.DEFAULT_SCALE, customerFund.getTotalStrategyOwnMoney(), strategyOwnMoney));
        // 累计服务费=原累计服务费-退还递延费
        customerFund.setTotalMessageServiceMoney(BigDecimalUtils.subtract(customerFund.getTotalMessageServiceMoney(), backDelayMoney));
        customerFund.setUpdateTime(new Date());
        LOGGER.info(LogUtils.appLog("卖出-更新客户资金信息：{}"), customerFund);
        iCustomerFundService.updateByPrimaryKeySelective(customerFund);

        /**
         * 增加退还递延金流水
         */
        if (backDelayMoney.compareTo(BigDecimal.ZERO) > 0) {
            CustomerFundDetailDO backDelayDetail = new CustomerFundDetailDO();
            backDelayDetail.setUserId(userId);
            backDelayDetail.setUserName(userName);
            backDelayDetail.setAgentId(agent.getAgentId());
            backDelayDetail.setAgentName(agent.getAgentName());
            backDelayDetail.setHappenMoney(backDelayMoney);
            backDelayDetail.setAfterHappenMoney(backDelayMoney);
            backDelayDetail.setFundType(FundTypeEnum.DELAY_BACK.getValue());
            backDelayDetail.setRemark(FundTypeEnum.DELAY_BACK.getDesc());
            LOGGER.info(LogUtils.appLog("卖出-退还递延金流水：{}"), backDelayDetail);
            iCustomerFundDetailService.addOne(backDelayDetail);
        }
        orderTask.removeOrder(orderDO.getOrderId());
    }

}
