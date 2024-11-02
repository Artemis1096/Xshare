package com.example.xshare.backend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringContext {
    private static ConfigurableApplicationContext context;

    public static void initializeContext(Class<?> primarySource) {
        if (context == null) {
            context = SpringApplication.run(primarySource); // Using SpringApplication.run for Spring Boot
        }
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static void setContext(ApplicationContext applicationContext) {
        context = (ConfigurableApplicationContext) applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    // Optional: To gracefully close Spring when JavaFX exits
    public static void closeContext() {
        if (context != null) {
            context.close();
        }
    }
}
