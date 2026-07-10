package com.example.EnlazadosTW.configs;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuracion del executor asincrono para tareas como push notifications.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean(name = "notificationsTaskExecutor")
	public Executor notificationsTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(6);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("notifications-");
		executor.initialize();
		return executor;
	}
}
