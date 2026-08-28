package com.duoc.banco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// Configuracion de ejecucion multihilos para los Steps de los Jobs
@Configuration
public class TaskExecutorConfig {

    @Bean(name = "transaccionTaskExecutor")
    public ThreadPoolTaskExecutor transaccionTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("TransaccionThread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "interesTaskExecutor")
    public ThreadPoolTaskExecutor interesTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("InteresThread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "movimientoCuentaTaskExecutor")
    public ThreadPoolTaskExecutor movimientoCuentaTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("MovimientoCuentaThread-");
        executor.initialize();
        return executor;
    }

    /*
    @Bean(name = "estadoCuentaTaskExecutor")
    public ThreadPoolTaskExecutor estadoCuentaTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("EstadoCuentaThread-");
        executor.initialize();
        return executor;
    }
    */

}
