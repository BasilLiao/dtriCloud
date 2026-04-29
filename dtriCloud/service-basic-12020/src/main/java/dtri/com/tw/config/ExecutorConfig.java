package dtri.com.tw.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

    @Bean(name = "mrpExecutor")
    public Executor mrpExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心線程數：設為 20
        executor.setCorePoolSize(20);
        // 最大線程數：設為 30 更
        executor.setMaxPoolSize(30);
        // 佇列容量
        executor.setQueueCapacity(100);
        // 執行緒名稱前綴 (方便 debug)
        executor.setThreadNamePrefix("MRP-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60); // 最多等 60 秒
        executor.initialize();
        return executor;
    }
}
