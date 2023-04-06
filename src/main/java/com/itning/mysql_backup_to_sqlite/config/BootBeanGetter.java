package com.itning.mysql_backup_to_sqlite.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author itning
 * @since 2023/4/6 22:22
 */

@Component
public class BootBeanGetter implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setApplicationContextStatic(applicationContext);
    }

    public static <T> T getClassBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContextStatic(final ApplicationContext applicationContext) {
        BootBeanGetter.applicationContext = applicationContext;
    }
}
