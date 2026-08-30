package com.duoc.banco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// Configuracion de ejecucion multihilos para los Steps de los Jobs
@Configuration
public class TaskExecutorConfig {

    @Bean(name = "transaccionTaskExecutor")
    public ThreadPoolTaskExecutor transaccionTaskExecutor(){
        return taskExecutor(4, 8, 50, "TransaccionThread-");
    }

    @Bean(name = "interesTaskExecutor")
    public ThreadPoolTaskExecutor interesTaskExecutor(){
        return taskExecutor(4, 8, 50, "InteresThread-");
    }

    @Bean(name = "movimientoCuentaTaskExecutor")
    public ThreadPoolTaskExecutor movimientoCuentaTaskExecutor(){
        return taskExecutor(4, 8, 50, "MovimientoCuentaThread-");
    }

    // Metodo para crear un taskExecutor con parametros
    public ThreadPoolTaskExecutor taskExecutor(int core, int max, int queue, String nombre){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queue);
        executor.setThreadNamePrefix(nombre);
        executor.initialize();
        return executor;
    }

}
