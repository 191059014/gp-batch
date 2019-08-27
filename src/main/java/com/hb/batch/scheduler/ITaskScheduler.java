package com.hb.batch.scheduler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * ========== 任务调度接口 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.scheduler.ITaskScheduler.java, v1.0
 * @date 2019年08月24日 17时05分
 */
public interface ITaskScheduler {

    /**
     * ########## 开启任务调度 ##########
     * @param cron      表达式
     * @param runnable  runnable对象
     * @param taskId    任务唯一标识
     * @param scheduler
     * @param futureMap
     */
    void start(String cron, Runnable runnable, String taskId, ThreadPoolTaskScheduler scheduler, Map<String, ScheduledFuture> futureMap);

    /**
     * ########## 停止任务调度 ##########
     * @param taskId    任务唯一标识
     * @param futureMap
     *
     */
    void stop(String taskId, Map<String, ScheduledFuture> futureMap);

}
