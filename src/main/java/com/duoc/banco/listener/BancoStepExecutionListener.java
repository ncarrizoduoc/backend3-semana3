package com.duoc.banco.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class BancoStepExecutionListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(BancoStepExecutionListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Lógica antes de la ejecución del paso
        logger.info("Iniciando el paso: " + stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // Lógica después de la ejecución del paso
        
        long readSkipCount = stepExecution.getReadSkipCount();
        long processSkipCount = stepExecution.getProcessSkipCount();
        long writeSkipCount = stepExecution.getWriteSkipCount();
        
        // Generar mensaje con nombre del paso y conteo de skips por fase (lectura, procesamiento y escritura)
        logger.info(
            "Fin del paso: " + stepExecution.getStepName() +
            "\nSkips en lectura: " + readSkipCount +
            "\nSkips en procesamiento: " +  processSkipCount +
            "\nSkips en escritura: " +  writeSkipCount
        );
        
        return stepExecution.getExitStatus();
    }

}
