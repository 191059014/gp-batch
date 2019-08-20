package com.hb.batch.task;

import org.springframework.beans.factory.InitializingBean;

import java.util.Set;

/**
 * ========== Description ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.ITask.java, v1.0
 * @date 2019年08月20日 00时15分
 */
public interface ITask extends InitializingBean {

    /**
     * ########## 开始任务 ##########
     *
     * @param cron 定时任务表达式
     */
    void start(String cron, Set<String> conditionSet);

    /**
     * ########## 停止任务 ##########
     */
    void stop();

}
