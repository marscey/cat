package com.xiaof.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Created by Chaoyun.Yep on 18/8/31.
 */
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    /**
     * 设置定时任务默认 pool-size，默认的是 1，即所有任务都是串行的
     * @param taskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(3);       // 默认最多同时执行 3 个定时任务
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}