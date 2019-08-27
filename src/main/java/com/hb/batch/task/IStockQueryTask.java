package com.hb.batch.task;

import org.springframework.beans.factory.InitializingBean;

import java.util.Set;

/**
 * ========== Description ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.IStockQueryTask.java, v1.0
 * @date 2019年08月20日 00时15分
 */
public interface IStockQueryTask extends InitializingBean {

    /**
     * ########## 开始任务 ##########
     *
     * @param cron   定时任务表达式
     * @param userId 用户ID
     */
    void start(String cron, Set<String> conditionSet, String userId);

    /**
     * ########## 停止任务 ##########
     *
     * @param userId 用户ID
     */
    void stop(String userId);

}
