package com.duoc.banco.config;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.duoc.banco.model.MovimientoCuenta;

@Configuration
public class MovimientoCuentaItemReaderConfig {
    @Bean
    public FlatFileItemReader<MovimientoCuenta> movimientoCuentaItemReader(
        @Value("${app.input-cuentas}") Resource inputFile
    ) {
        return new FlatFileItemReaderBuilder<MovimientoCuenta>()
            .name("movimientoCuentaItemReader")
            .resource(inputFile)
            .encoding("UTF-8")
            .linesToSkip(1)
            .delimited()
            .delimiter(",")
            .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
            .fieldSetMapper(movimientoCuentaFieldSetMapper())
            .build();
    }

    private FieldSetMapper<MovimientoCuenta> movimientoCuentaFieldSetMapper() {
        return fieldSet -> {
            MovimientoCuenta movimientoCuenta = new MovimientoCuenta();
            movimientoCuenta.setCuentaId(fieldSet.readLong("cuentaId"));
            movimientoCuenta.setMonto(fieldSet.readInt("monto"));
            movimientoCuenta.setDescripcion(fieldSet.readString("descripcion"));
            return movimientoCuenta;
        };
    }

}
