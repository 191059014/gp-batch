package com.hb.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ========== 股票查询任务线程池配置类 ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.config.StockQueryTaskExecutorConfig.java, v1.0
 * @date 2019年08月19日 23时31分
 */
@Configuration
@EnableAsync
public class StockQueryTaskExecutorConfig {

    private static final Logger logger = LoggerFactory.getLogger(StockQueryTaskExecutorConfig.class);

    @Value("${stockQueryTask.executor.thread.core_pool_size}")
    private int corePoolSize;
    @Value("${stockQueryTask.executor.thread.max_pool_size}")
    private int maxPoolSize;
    @Value("${stockQueryTask.executor.thread.queue_capacity}")
    private int queueCapacity;
    @Value("${stockQueryTask.executor.thread.name.prefix}")
    private String namePrefix;

    @Bean(name = "asyncStockTaskExecutor")
    public Executor asyncStockTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(corePoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        //配置队列大小
        executor.setQueueCapacity(queueCapacity);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix(namePrefix);
        /**
         * rejection-policy：当pool已经达到max size的时候，如何处理新任务
         * CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        logger.info("股票任务线程池初始化完成");

        return executor;
    }

}
