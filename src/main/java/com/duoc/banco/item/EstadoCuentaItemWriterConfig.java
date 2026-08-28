package com.duoc.banco.item;

import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.duoc.banco.model.EstadoCuenta;

@Component
public class EstadoCuentaItemWriterConfig {
    @Bean
    public ItemWriter<EstadoCuenta> estadoCuentaItemWriter(
        NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        return new JdbcBatchItemWriterBuilder<EstadoCuenta>()
            .namedParametersJdbcTemplate(
                namedParameterJdbcTemplate
            )
            .itemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<>()
            )
            .sql(
                "INSERT INTO estado_cuenta (cuenta_id, ingresos, salidas, diferencia) " +
                "VALUES (:cuentaId, :ingresos, :salidas, :diferencia)"
            )
            .assertUpdates(true) // Verifica que se haya actualizado al menos una fila en la base de datos
            .build();
    }

}
