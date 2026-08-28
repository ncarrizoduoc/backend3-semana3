package com.duoc.banco.item;

import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.duoc.banco.model.Interes;

@Configuration
public class InteresItemWriterConfig {

    @Bean
    public ItemWriter<Interes> interesItemWriter(
        NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        return new JdbcBatchItemWriterBuilder<Interes>()
            .namedParametersJdbcTemplate(
                namedParameterJdbcTemplate
            )
            .itemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<>()
            )
            .sql(
                "INSERT INTO intereses (cuenta_id, nombre, saldo_inicial, saldo_final, edad, tipo, tasa_interes) " +
                "VALUES (:cuentaId, :nombre, :saldoInicial, :saldoFinal, :edad, :tipo, :tasaInteres)"
            )
            .assertUpdates(true) // Verifica que se haya actualizado al menos una fila en la base de datos
            .build();
    }

}
