package com.duoc.banco.listener.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class InteresJobCompletionListener implements JobExecutionListener{

    private final ThreadPoolTaskExecutor taskExecutor;
    private final static Logger logger = LoggerFactory.getLogger(InteresJobCompletionListener.class);

    public InteresJobCompletionListener(@Qualifier("interesTaskExecutor") ThreadPoolTaskExecutor taskExecutor){
        this.taskExecutor = taskExecutor;
    }

    @PreDestroy
    public void shutdown(){
        taskExecutor.shutdown();
    }


    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Lógica antes de la ejecución del Job
        System.out.println("Iniciando el job: " + jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Lógica después de la ejecución del Job
        shutdown();
        if (jobExecution.getStatus().isUnsuccessful()){
            logger.warn("Error en la ejecucion del Job {}. Revisar skips y reintentos.", jobExecution.getJobInstance().getJobName());
        } else {
            logger.info("Job completado exitosamente con ID: {}", jobExecution.getId());
        }
        logger.info("Resumen del Job: {}", jobExecution.toString());
    }

}
