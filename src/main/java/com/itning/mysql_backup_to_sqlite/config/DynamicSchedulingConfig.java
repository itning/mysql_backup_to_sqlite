package com.itning.mysql_backup_to_sqlite.config;

import com.itning.mysql_backup_to_sqlite.engine.JobEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Map;
import java.util.Objects;

/**
 * @author ning.wang
 * @since 2023/4/3 10:26
 */
@Slf4j
@Configuration
@EnableScheduling
public class DynamicSchedulingConfig implements SchedulingConfigurer {

    private final BackupProperties backupProperties;

    @Autowired
    public DynamicSchedulingConfig(BackupProperties backupProperties) {
        this.backupProperties = backupProperties;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (Objects.isNull(backupProperties.getJob())) {
            return;
        }
        for (Map.Entry<String, BackupProperties.Job> item : backupProperties.getJob().entrySet()) {
            taskRegistrar.addCronTask(new JobEngine(item.getKey(), item.getValue()), item.getValue().getCron());
        }
    }
}
