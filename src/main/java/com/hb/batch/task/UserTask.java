package com.hb.batch.task;

import com.hb.batch.runable.UserRunnable;
import com.hb.batch.scheduler.ITaskScheduler;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * ========== 用户任务 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.task.UserTask.java, v1.0
 * @date 2019年08月24日 18时09分
 */
@Component
public class UserTask implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTask.class);

    @Autowired
    private ITaskScheduler iTaskScheduler;

    @Autowired
    @Qualifier("userTaskScheduler")
    private ThreadPoolTaskScheduler userTaskScheduler;

    @Value("${userTask.cron.default}")
    private String defaultCron;

    @Override
    public void afterPropertiesSet() throws Exception {

    }


    /**
     * ########## 开启用户任务 ##########
     */
    public void startTask() {
        Runnable runnable = new UserRunnable();
        iTaskScheduler.start(defaultCron, runnable, getTaskId(), userTaskScheduler);
    }

    /**
     * ########## 获取任务ID ##########
     *
     * @return taskId
     */
    private String getTaskId() {
        return "userTask_" + System.currentTimeMillis();
    }

}
