package com.hb.batch.container;

import com.hb.batch.task.OrderQueryTask;
import com.hb.batch.task.StockQueryTask;
import com.hb.batch.task.UserTask;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ========== 项目启动之后 ==========
 *
 * @author Mr.huang
 * @version com.hb.web.container.SpringRunner.java, v1.0
 * @date 2019年06月11日 19时18分
 */
@RestController
@RequestMapping("self/tools")
@Api(tags = "系统工具")
public class SelfRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfRunner.class);

    @Autowired
    private OrderQueryTask orderQueryTask;

    @Autowired
    private StockQueryTask stockQueryTask;

    @Autowired
    private UserTask userTask;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================");
        System.out.println(" server start complete");
        System.out.println(" you can enjoy yourself");
        System.out.println("========================");

        System.out.println(" 开始订单查询任务");
        orderQueryTask.loadPendingOrders();
        orderQueryTask.startTask();

        System.out.println(" 开始股票查询任务");
        stockQueryTask.startTask();

        System.out.println(" 开始用户任务");
        userTask.startTask();

    }

}
