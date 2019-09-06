package com.hb.batch.container;

import com.hb.batch.task.OrderTask;
import com.hb.batch.task.StockTask;
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
    private OrderTask orderTask;

    @Autowired
    private StockTask stockTask;

    @Autowired
    private UserTask userTask;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================");
        System.out.println(" server start complete");
        System.out.println(" you can enjoy yourself");
        System.out.println("========================");
    }

}
