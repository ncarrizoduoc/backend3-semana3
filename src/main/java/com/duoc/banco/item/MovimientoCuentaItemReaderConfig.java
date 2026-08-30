package com.duoc.banco.item;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.duoc.banco.exception.MovimientoCuentaNoValidoException;
import com.duoc.banco.model.MovimientoCuenta;

@Configuration
public class MovimientoCuentaItemReaderConfig {
    
    @Bean(name = "movimientoCuentaItemReader")
    @StepScope
    public FlatFileItemReader<MovimientoCuenta> movimientoCuentaItemReader(
        @Value("${app.input-cuentas}") Resource inputFile,
        @Value("#{stepExecutionContext['start']}") int start, // Inyecta el límite inferior de la partición
        @Value("#{stepExecutionContext['end']}") int end // Inyecta el límite superior de la partición
    ) {
        FlatFileItemReader<MovimientoCuenta> reader =  new FlatFileItemReaderBuilder<MovimientoCuenta>()
            .name("movimientoCuentaItemReader")
            .resource(inputFile)
            .encoding("UTF-8")
            .linesToSkip(1)
            .delimited()
            .delimiter(",")
            .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
            .fieldSetMapper(movimientoCuentaFieldSetMapper())
            .build();

        reader.setCurrentItemCount(start); // Límite inferior de la partición 
        reader.setMaxItemCount(end + 1); // Límite superior de la partición (se suma 1 porque maxItemCount es exclusivo)

        return reader;
    }

    private FieldSetMapper<MovimientoCuenta> movimientoCuentaFieldSetMapper() {
        return fieldSet -> {
            MovimientoCuenta movimientoCuenta = new MovimientoCuenta();
            movimientoCuenta.setCuentaId(fieldSet.readLong("cuentaId"));
            movimientoCuenta.setMonto(fieldSet.readInt("monto"));
            movimientoCuenta.setDescripcion(fieldSet.readString("descripcion"));

            // Validar que el monto de la transaccion no sea 0 (se realiza en este paso porque hacerlo en el siguiente
            // puede generar errores al agregar transacciones a los montos acumulados)
            if (movimientoCuenta.getMonto() == 0){
                throw new MovimientoCuentaNoValidoException("El monto del movimiento de cuenta no puede ser 0");
            }
            
            return movimientoCuenta;
        };
    }

}
