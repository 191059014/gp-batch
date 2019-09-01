package com.hb.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * ========== 线程池配置类 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.config.TaskExecutorConfig.java, v1.0
 * @date 2019年08月19日 23时31分
 */
@Configuration
@EnableAsync
public class TaskExecutorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorConfig.class);

    @Value("${orderQueryTask.thread.core_pool_size}")
    private int orderQueryThreadPoolSize;
    @Value("${orderQueryTask.thread.name.prefix}")
    private String orderQueryThreadNamePrefix;

    @Bean(name = "orderQueryTaskScheduler")
    public ThreadPoolTaskScheduler orderQueryTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(orderQueryThreadPoolSize);
        threadPoolTaskScheduler.setThreadNamePrefix(orderQueryThreadNamePrefix);
        threadPoolTaskScheduler.initialize();
        LOGGER.info("初始化订单查询线程池完成");
        return threadPoolTaskScheduler;
    }

    @Value("${stockQueryTask.thread.core_pool_size}")
    private int stockQueryThreadPoolSize;
    @Value("${stockQueryTask.thread.name.prefix}")
    private String stockQueryThreadNamePrefix;

    @Bean(name = "stockQueryTaskScheduler")
    public ThreadPoolTaskScheduler stockQueryTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(stockQueryThreadPoolSize);
        threadPoolTaskScheduler.setThreadNamePrefix(stockQueryThreadNamePrefix);
        threadPoolTaskScheduler.initialize();
        LOGGER.info("初始化股票查询线程池完成");
        return threadPoolTaskScheduler;
    }

    @Value("${userTask.thread.core_pool_size}")
    private int userThreadPoolSize;
    @Value("${userTask.thread.name.prefix}")
    private String userThreadNamePrefix;

    @Bean(name = "userTaskScheduler")
    public ThreadPoolTaskScheduler userTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(userThreadPoolSize);
        threadPoolTaskScheduler.setThreadNamePrefix(userThreadNamePrefix);
        threadPoolTaskScheduler.initialize();
        LOGGER.info("初始化用户任务线程池完成");
        return threadPoolTaskScheduler;
    }

    @Value("${userOrderExecutor.thread.core_pool_size}")
    private int userOrderThreadPoolSize;
    @Value("${userOrderExecutor.thread.name.prefix}")
    private String userOrderThreadNamePrefix;

    @Bean(name = "userOrderTaskScheduler")
    public ThreadPoolTaskScheduler userOrderTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(userOrderThreadPoolSize);
        threadPoolTaskScheduler.setThreadNamePrefix(userOrderThreadNamePrefix);
        threadPoolTaskScheduler.initialize();
        LOGGER.info("初始化用户订单处理线程池完成");
        return threadPoolTaskScheduler;
    }

}
