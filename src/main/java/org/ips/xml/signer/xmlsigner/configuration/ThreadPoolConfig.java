package org.ips.xml.signer.xmlsigner.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Value("${xml.signer.thread.count:5}")
    private int threadCount;

    @Bean(name = "signerExecutor")
    public Executor signerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadCount);

        executor.setMaxPoolSize(threadCount * 2);
        executor.setQueueCapacity(500); // adjust based on load
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("SignerThread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
