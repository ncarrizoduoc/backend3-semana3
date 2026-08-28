package com.duoc.banco.item;

import javax.sql.DataSource;

import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SingleItemPeekableItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.DataClassRowMapper;

import com.duoc.banco.model.MovimientoCuenta;

@Configuration
public class EstadoCuentaItemReaderConfig {

    @Bean
    public JdbcCursorItemReader<MovimientoCuenta> movimientoTablaItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<MovimientoCuenta>()
            .name("movimientoTablaItemReader")
            .dataSource(dataSource)
            .sql("SELECT * FROM movimiento_cuenta ORDER BY cuenta_id")
            .rowMapper(new DataClassRowMapper<>(MovimientoCuenta.class))
            .build();
    }

    @Bean
    public SingleItemPeekableItemReader<MovimientoCuenta> movimientoCuentaPeekableReader(
        JdbcCursorItemReader<MovimientoCuenta> movimientoTablaItemReader
    ) {
        return new SingleItemPeekableItemReader<>(movimientoTablaItemReader);
    }

}
