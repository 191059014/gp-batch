package com.hb.batch.scheduler;

import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * ========== 任务调度实现类 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.scheduler.TaskSchedulerImpl.java, v1.0
 * @date 2019年08月24日 17时12分
 */
@Component
public class TaskSchedulerImpl implements ITaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskSchedulerImpl.class);

    @Override
    public void start(String cron, Runnable runnable, String taskId, ThreadPoolTaskScheduler scheduler) {
        scheduler.schedule(runnable, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                return new CronTrigger(cron).nextExecutionTime(triggerContext);
            }
        });
        LOGGER.info("开启任务：{}，cron：{}，并添加到任务集合", taskId, cron);
    }

}
