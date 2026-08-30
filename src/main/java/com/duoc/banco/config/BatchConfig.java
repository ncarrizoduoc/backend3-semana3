package com.duoc.banco.config;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.support.SingleItemPeekableItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import com.duoc.banco.exception.EstadoCuentaNoValidoException;
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

    //-------------------------------------------------------------------
    // Job para identificar transacciones anomalas
    //-------------------------------------------------------------------

    // Job para procesamiento de transacciones
    @Bean
    public Job transaccionJob(
        JobRepository jobRepository,
        @Qualifier("transaccionPartitionStep") Step transaccionPartitionStep,
        TransaccionJobCompletionListener jobCompletionListener
    ) {
        return new JobBuilder("transaccionJob", jobRepository)
        .start(transaccionPartitionStep)
        .listener(jobCompletionListener)
        .build();
    }

    @Bean(name = "transaccionPartitionHandler")
    public TaskExecutorPartitionHandler transaccionPartitionHandler(
        @Qualifier("transaccionMinionStep") Step transaccionMinionStep, 
        @Qualifier("transaccionTaskExecutor") TaskExecutor taskExecutor
    ) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(transaccionMinionStep);
        handler.setTaskExecutor(taskExecutor);
        handler.setGridSize(4); // Número de particiones
        return handler;
    }

    @Bean(name = "transaccionPartitionStep")
    public Step transaccionPartitionStep(JobRepository jobRepository, 
                              @Qualifier("transaccionPartitionHandler") TaskExecutorPartitionHandler partitionHandler,
                              @Qualifier("transaccionPartitioner") Partitioner partitioner) {
        return new StepBuilder("transaccionPartitionStep", jobRepository)
                .partitioner("transaccionMinionStep", partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    // Step para procesamiento de transacciones
    @Bean(name = "transaccionMinionStep")
    public Step transaccionMinionStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<Transaccion> transaccionItemReader,
        ItemProcessor<Transaccion, Transaccion> transaccionItemProcessor,
        ItemWriter<Transaccion> transaccionItemWriter,
        BancoStepExecutionListener stepListener,
        TransaccionSkipListener skipListener
    ) {
        return new StepBuilder("transaccionMinionStep", jobRepository)
            .<Transaccion, Transaccion>chunk(50, transactionManager)
            .reader(transaccionItemReader)
            .processor(transaccionItemProcessor)
            .writer(transaccionItemWriter)
            .faultTolerant()
            .skipLimit(100)
            .skip(FlatFileParseException.class)
            .skip(DateTimeParseException.class)
            .skip(NumberFormatException.class)
            .skip(DuplicateKeyException.class)
            .retryPolicy(retryPolicy())
            .backOffPolicy(backoffPolicy())
            .listener(skipListener)
            .listener(stepListener)
            .build();
    }    

    @Bean(name = "transaccionPartitioner")
    public Partitioner transaccionPartitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();
            int totalData = 1000; // Cantidad total de filas con datos en transacciones.csv
            int partitionSize = (int) Math.ceil((double) totalData / gridSize);

            int start = 0;
            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = new ExecutionContext();
                int end = Math.min(start + partitionSize - 1, totalData - 1);

                context.putInt("start", start);
                context.putInt("end", end);
                context.putString("partitionName", "transaccion-partition-" + i);
                partitions.put("transaccion-partition-" + i, context);

                start += partitionSize;
                if (start >= totalData) {
                    break;
                }
            }
            return partitions;
        };
    }

    //----------------------------------------------------------------------------
    // Job para calcular tasas de interes por cuenta y generar saldos resultantes
    //----------------------------------------------------------------------------

    // Job para procesamiento de intereses
    @Bean
    public Job interesJob(
        JobRepository jobRepository,
        @Qualifier("interesPartitionStep") Step interesPartitionStep,
        InteresJobCompletionListener jobCompletionListener
    ) {
        return new JobBuilder("interesJob", jobRepository)
        .start(interesPartitionStep)
        .listener(jobCompletionListener)
        .build();
    }

    @Bean(name = "interesPartitionHandler")
    public TaskExecutorPartitionHandler interesPartitionHandler(
        @Qualifier("interesMinionStep") Step interesMinionStep, 
        @Qualifier("interesTaskExecutor") TaskExecutor taskExecutor
    ) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(interesMinionStep);
        handler.setTaskExecutor(taskExecutor);
        handler.setGridSize(4); // Número de particiones
        return handler;
    }

    @Bean(name = "interesPartitionStep")
    public Step interesPartitionStep(
        JobRepository jobRepository,
        @Qualifier("interesPartitionHandler") TaskExecutorPartitionHandler partitionHandler,
        @Qualifier("interesPartitioner") Partitioner partitioner
    ) {
        return new StepBuilder("interesPartitionStep", jobRepository)
                .partitioner("interesMinionStep", partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    // Step para procesamiento de intereses
    @Bean(name = "interesMinionStep")
    public Step interesMinionStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<Interes> interesItemReader,
        ItemProcessor<Interes, Interes> interesItemProcessor,
        ItemWriter<Interes> interesItemWriter,
        BancoStepExecutionListener stepListener,
        InteresSkipListener skipListener
    ) {
        return new StepBuilder("interesMinionStep", jobRepository)
            .<Interes, Interes>chunk(50, transactionManager)
            .reader(interesItemReader)
            .processor(interesItemProcessor)
            .writer(interesItemWriter)
            .faultTolerant()
            .skipLimit(1000)
            .skip(InteresNoValidoException.class)
            .skip(FlatFileParseException.class)
            .skip(NumberFormatException.class)
            .skip(DuplicateKeyException.class)
            .retryPolicy(retryPolicy())
            .backOffPolicy(backoffPolicy())
            .listener(skipListener)
            .listener(stepListener)
            .stream((ItemStream) interesItemReader)
            .build();
    }

    @Bean(name = "interesPartitioner")
    public Partitioner interesPartitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();
            int totalData = 1000; // Cantidad total de filas con datos en intereses.csv
            int partitionSize = (int) Math.ceil((double) totalData / gridSize);

            int start = 0;
            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = new ExecutionContext();
                int end = Math.min(start + partitionSize - 1, totalData - 1);

                context.putInt("start", start);
                context.putInt("end", end);
                context.putString("partitionName", "interes-partition-" + i);
                partitions.put("interes-partition-" + i, context);

                start += partitionSize;
                if (start >= totalData) {
                    break;
                }
            }
            return partitions;
        };
    }


    //-------------------------------------------------------------------
    // Job para procesar movimientos y generar estados de cuenta anuales
    //-------------------------------------------------------------------

    // Job para procesar movimientos y generar estados de cuenta
    @Bean
    public Job generarEstadosDeCuentaJob(
        JobRepository jobRepository,
        @Qualifier("movCuentaPartitionStep") Step leerYGuardarMovimientosDeCuentaStep,
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

    // Step 1: Leer movimientos de cuentas desde el CSV y guardarlos en 
    // la tabla temporal MOVIMIENTO_CUENTA en base de datos

    @Bean(name = "movCuentaPartitionHandler")
    public TaskExecutorPartitionHandler movCuentaPartitionHandler(
        @Qualifier("movCuentaMinionStep") Step movCuentaMinionStep, 
        @Qualifier("movimientoCuentaTaskExecutor") TaskExecutor taskExecutor
    ) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(movCuentaMinionStep);
        handler.setTaskExecutor(taskExecutor);
        handler.setGridSize(4); // Número de particiones
        return handler;
    }

    @Bean(name = "movCuentaPartitionStep")
    public Step movCuentaPartitionStep(
        JobRepository jobRepository,
        @Qualifier("movCuentaPartitionHandler") TaskExecutorPartitionHandler partitionHandler,
        @Qualifier("movCuentaPartitioner") Partitioner partitioner
    ) {
        return new StepBuilder("movCuentaPartitionStep", jobRepository)
                .partitioner("movCuentaMinionStep", partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean(name = "movCuentaMinionStep")
    public Step movCuentaMinionStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        @Qualifier("movimientoCuentaItemReader") FlatFileItemReader<MovimientoCuenta> movimientoCuentaItemReader,
        ItemWriter<MovimientoCuenta> movimientoCuentaItemWriter,
        BancoStepExecutionListener stepListener,
        MovimientoCuentaSkipListener skipListener
    ){
       return new StepBuilder("movCuentaMinionStep", jobRepository)
            .<MovimientoCuenta, MovimientoCuenta>chunk(50, transactionManager)
            .reader(movimientoCuentaItemReader)
            .writer(movimientoCuentaItemWriter)
            .faultTolerant()
            .skipLimit(100)
            .skip(MovimientoCuentaNoValidoException.class)
            .skip(FlatFileParseException.class)
            .skip(DateTimeParseException.class)
            .skip(NumberFormatException.class)
            .retryPolicy(retryPolicy())
            .backOffPolicy(backoffPolicy())
            .listener(skipListener)
            .listener(stepListener)
            .build();
    }

    @Bean(name = "movCuentaPartitioner")
    public Partitioner movCuentaPartitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();
            int totalData = 1000; // Cantidad total de filas con datos en cuentas_anuales.csv
            int partitionSize = (int) Math.ceil((double) totalData / gridSize);

            int start = 0;
            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = new ExecutionContext();
                int end = Math.min(start + partitionSize - 1, totalData - 1);

                context.putInt("start", start);
                context.putInt("end", end);
                context.putString("partitionName", "movCuenta-partition-" + i);
                partitions.put("movCuenta-partition-" + i, context);

                start += partitionSize;
                if (start >= totalData) {
                    break;
                }
            }
            return partitions;
        };
    }

    // Step 2: Leer movimientos de cuentas desde la tabla temporal MOVIMIENTO_CUENTA,
    // ordenarlos por cuenta_id y procesarlos para generar el estado de cuenta
    @Bean
    public Step generarEstadosDeCuentaStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<EstadoCuenta> estadoCuentaItemReader, // Reader que agrupa los movimientos de cuenta en estados
        SingleItemPeekableItemReader<MovimientoCuenta> movimientoCuentaPeekableReader,
        EstadoCuentaItemProcessor estadoCuentaItemProcessor,
        ItemWriter<EstadoCuenta> estadoCuentaItemWriter,
        BancoStepExecutionListener stepListener,
        EstadoCuentaSkipListener skipListener
    ){
        return new StepBuilder("generarEstadosDeCuentaStep", jobRepository)
            .<EstadoCuenta, EstadoCuenta>chunk(50, transactionManager)
            .reader(estadoCuentaItemReader)
            .stream(movimientoCuentaPeekableReader) 
            .processor(estadoCuentaItemProcessor)
            .writer(estadoCuentaItemWriter)
            .faultTolerant()
            .skipLimit(100)
            .skip(MovimientoCuentaNoValidoException.class)
            .skip(EstadoCuentaNoValidoException.class)
            .skip(FlatFileParseException.class)
            .skip(DateTimeParseException.class)
            .skip(NumberFormatException.class)
            .retryPolicy(retryPolicy())
            .backOffPolicy(backoffPolicy())
            .listener(skipListener)
            .listener(stepListener)
            .build();
    }

    //---------------------------------------------
    // Policitas para retry (reintentos) y backoff
    //---------------------------------------------

    @Bean
    public SimpleRetryPolicy retryPolicy() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(CannotAcquireLockException.class, true);
        retryableExceptions.put(TransientDataAccessException.class, true);
        // Si se produce una de las excepciones en el HashMap, se reintentará 3 veces
        return new SimpleRetryPolicy(3, retryableExceptions, true);
    }

    @Bean
    public ExponentialBackOffPolicy backoffPolicy() {
        ExponentialBackOffPolicy backoffPolicy = new ExponentialBackOffPolicy();
        backoffPolicy.setInitialInterval(1000); // Intervalo inicial de 1 segundo
        backoffPolicy.setMultiplier(2.0); // Multiplicador para el intervalo exponencial
        backoffPolicy.setMaxInterval(10000); // Intervalo máximo de 10 segundos
        return backoffPolicy;
    }

}
