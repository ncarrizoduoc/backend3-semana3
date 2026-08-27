package com.duoc.banco.config;

import java.time.format.DateTimeParseException;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.support.SingleItemPeekableItemReader;
import org.springframework.batch.infrastructure.repeat.policy.SimpleCompletionPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.duoc.banco.exception.InteresNoValidoException;
import com.duoc.banco.exception.MovimientoCuentaNoValidoException;
import com.duoc.banco.listener.BancoStepExecutionListener;
import com.duoc.banco.listener.job.EstadoCuentaCompletionListener;
import com.duoc.banco.listener.job.InteresJobCompletionListener;
import com.duoc.banco.listener.job.TransaccionJobCompletionListener;
import com.duoc.banco.listener.skip.EstadoCuentaSkipListener;
import com.duoc.banco.listener.skip.InteresSkipListener;
import com.duoc.banco.listener.skip.MovimientoCuentaSkipListener;
import com.duoc.banco.listener.skip.TransaccionSkipListener;
import com.duoc.banco.model.EstadoCuenta;
import com.duoc.banco.model.Interes;
import com.duoc.banco.model.MovimientoCuenta;
import com.duoc.banco.model.Transaccion;
import com.duoc.banco.processor.EstadoCuentaItemProcessor;

@Configuration
public class BatchConfig {

    // Paso para procesamiento de transacciones
    @Bean
    public Step transaccionStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<Transaccion> transaccionItemReader,
        ItemProcessor<Transaccion, Transaccion> transaccionItemProcessor,
        ItemWriter<Transaccion> transaccionItemWriter,
        @Qualifier("transaccionTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
        BancoStepExecutionListener stepListener,
        TransaccionSkipListener skipListener
    ) {
        return new StepBuilder("transaccionStep", jobRepository)
            .<Transaccion, Transaccion>chunk(5, transactionManager)
            .reader(transaccionItemReader)
            .processor(transaccionItemProcessor)
            .writer(transaccionItemWriter)
            .faultTolerant()
            .skipLimit(5)
            .skip(FlatFileParseException.class)
            .skip(DateTimeParseException.class)
            .skip(NumberFormatException.class)
            .retryLimit(3)
            .retry(CannotAcquireLockException.class)
            .retry(TransientDataAccessException.class)
            .listener(skipListener)
            .listener(stepListener)
            .taskExecutor(taskExecutor)
            .build();
    }

    // Job para procesamiento de transacciones
    @Bean
    public Job transaccionJob(
        JobRepository jobRepository,
        Step transaccionStep,
        TransaccionJobCompletionListener jobCompletionListener
    ) {
        return new JobBuilder("transaccionJob", jobRepository)
        .start(transaccionStep)
        .listener(jobCompletionListener)
        .build();
    }

    // Step para procesamiento de intereses
    @Bean
    public Step interesStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<Interes> interesItemReader,
        ItemProcessor<Interes, Interes> interesItemProcessor,
        ItemWriter<Interes> interesItemWriter,
        @Qualifier("interesTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
        BancoStepExecutionListener stepListener,
        InteresSkipListener skipListener
    ) {

        return new StepBuilder("interesStep", jobRepository)
            .<Interes, Interes>chunk(new SimpleCompletionPolicy(5), transactionManager)
            .reader(interesItemReader)
            .processor(interesItemProcessor)
            .writer(interesItemWriter)
            .faultTolerant()
            .skipLimit(5)
            .skip(InteresNoValidoException.class)
            .skip(FlatFileParseException.class)
            .skip(NumberFormatException.class)
            .retryLimit(3)
            .retry(CannotAcquireLockException.class)
            .retry(TransientDataAccessException.class)
            .listener(skipListener)
            .listener(stepListener)
            .stream((ItemStream) interesItemReader)
            .taskExecutor(taskExecutor)
            .build();
    }

    // Job para procesamiento de intereses
    @Bean
    public Job interesJob(
        JobRepository jobRepository,
        Step interesStep,
        InteresJobCompletionListener jobCompletionListener
    ) {
        return new JobBuilder("interesJob", jobRepository)
        .start(interesStep)
        .listener(jobCompletionListener)
        .build();
    }

    //-------------------------------------------------------------------
    // Job para procesar movimientos y generar estados de cuenta anuales
    //-------------------------------------------------------------------

    // Step 1: Leer movimientos de cuentas desde el CSV y guardarlos en 
    // la tabla temporal MOVIMIENTO_CUENTA en base de datos
    @Bean
    public Step leerYGuardarMovimientosDeCuentaStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        FlatFileItemReader<MovimientoCuenta> movimientoCuentaItemReader,
        ItemWriter<MovimientoCuenta> movimientoCuentaItemWriter,
        @Qualifier("movimientoCuentaTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
        BancoStepExecutionListener stepListener,
        MovimientoCuentaSkipListener skipListener
    ){
       return new StepBuilder("leerYGuardarMovimientosDeCuentaStep", jobRepository)
            .<MovimientoCuenta, MovimientoCuenta>chunk(5, transactionManager)
            .reader(movimientoCuentaItemReader)
            .writer(movimientoCuentaItemWriter)
            .faultTolerant()
            .skipLimit(5)
            .skip(FlatFileParseException.class)
            .skip(DateTimeParseException.class)
            .skip(NumberFormatException.class)
            .retryLimit(3)
            .retry(CannotAcquireLockException.class)
            .retry(TransientDataAccessException.class)
            .listener(skipListener)
            .listener(stepListener)
            .taskExecutor(taskExecutor)
            .build();
    
    }

    // Step 2: Leer movimientos de cuentas desde la tabla temporal MOVIMIENTO_CUENTA,
    // ordenarlos por cuenta_id y procesarlos para generar el estado de cuenta
    @Bean
    public Step generarEstadosDeCuentaStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        SingleItemPeekableItemReader<MovimientoCuenta> movimientoCuentaPeekableReader,
        EstadoCuentaItemProcessor estadoCuentaItemProcessor,
        ItemWriter<EstadoCuenta> estadoCuentaItemWriter,
        BancoStepExecutionListener stepListener,
        EstadoCuentaSkipListener skipListener
    ){
        ItemReader<MovimientoCuenta> reader = () -> {
            MovimientoCuenta movActual = movimientoCuentaPeekableReader.read();
            MovimientoCuenta movSiguiente = movimientoCuentaPeekableReader.peek();

            if (movActual != null){
                boolean esUltimo = (movSiguiente == null || !movSiguiente.getCuentaId().equals(movActual.getCuentaId()));
                movActual.setUltimoDelGrupo(esUltimo);
            }
            return movActual;
        };

        return new StepBuilder("generarEstadosDeCuentaStep", jobRepository)
            .<MovimientoCuenta, EstadoCuenta>chunk(5, transactionManager)
            .reader(reader)
            .stream(movimientoCuentaPeekableReader)
            .processor(estadoCuentaItemProcessor)
            .writer(estadoCuentaItemWriter)
            .faultTolerant()
            .skipLimit(5)
            .skip(MovimientoCuentaNoValidoException.class)
            .skip(FlatFileParseException.class)
            .skip(DateTimeParseException.class)
            .skip(NumberFormatException.class)
            .retryLimit(3)
            .retry(CannotAcquireLockException.class)
            .retry(TransientDataAccessException.class)
            .listener(skipListener)
            .listener(stepListener)
            .build();
    }

    // Job para procesar movimientos y generar estados de cuenta
    @Bean
    public Job generarEstadosDeCuentaJob(
        JobRepository jobRepository,
        Step leerYGuardarMovimientosDeCuentaStep,
        Step generarEstadosDeCuentaStep,
        EstadoCuentaCompletionListener jobCompletionListener
    ){
        return new org.springframework.batch.core.job.builder.JobBuilder(
            "generarEstadosDeCuentaJob",
            jobRepository
        )
        .start(leerYGuardarMovimientosDeCuentaStep)
        .next(generarEstadosDeCuentaStep)
        .listener(jobCompletionListener)
        .build();
    }

}
