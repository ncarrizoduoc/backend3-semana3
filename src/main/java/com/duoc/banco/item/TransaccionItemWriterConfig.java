package com.duoc.banco.item;

import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.duoc.banco.model.Transaccion;

@Configuration
public class TransaccionItemWriterConfig {

    @Bean
    public ItemWriter<Transaccion> transaccionItemWriter(
        NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        return new JdbcBatchItemWriterBuilder<Transaccion>()
            .namedParametersJdbcTemplate(
                namedParameterJdbcTemplate
            )
            .itemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<>()
            )
            .sql(
                "INSERT INTO transacciones (id, fecha, monto, tipo, observaciones) " +
                "VALUES (:id, :fecha, :monto, :tipo, :observaciones)"
            )
            .assertUpdates(true) // Verifica que se haya actualizado al menos una fila en la base de datos
            .build();
    }

}
