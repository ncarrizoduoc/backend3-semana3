package com.duoc.banco.config;

import org.springframework.batch.infrastructure.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.duoc.banco.model.MovimientoCuenta;

@Configuration
public class MovimientoCuentaItemWriterConfig {

    @Bean
    public JdbcBatchItemWriter<MovimientoCuenta> movimientoCuentaItemWriter(
        NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        return new JdbcBatchItemWriterBuilder<MovimientoCuenta>()
            .namedParametersJdbcTemplate(namedParameterJdbcTemplate)
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("INSERT INTO movimiento_cuenta (id, cuenta_id, monto, descripcion) VALUES (:id, :cuentaId, :monto, :descripcion)")
            .assertUpdates(true)
            .build();
    }

}
