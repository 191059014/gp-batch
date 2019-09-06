package com.hb.batch.test;

import com.hb.batch.task.StockTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ========== Description ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.test.TestTaskController.java, v1.0
 * @date 2019年08月24日 16时56分
 */
@RestController
@RequestMapping("test/task")
public class TestTaskController {

    @Autowired
    private StockTask stockTask;

    @GetMapping("/test")
    public void test() {
    }

}
