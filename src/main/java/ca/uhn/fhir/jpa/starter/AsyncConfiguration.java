package ca.uhn.fhir.jpa.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Import(AppProperties.class)
public class AsyncConfiguration {

	@Autowired
	AppProperties appProperties;

	@Bean(name = "asyncTaskExecutor")
	public Executor asyncExecutor()
	{
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(appProperties.getExecutor_core_pool_size());
		executor.setMaxPoolSize(appProperties.getExecutor_max_pool_size());
		executor.setQueueCapacity(appProperties.getExecutor_queue_capacity());
		executor.setThreadNamePrefix(appProperties.getExecutor_thread_name_prefix());
		executor.initialize();
		return executor;
	}
}
