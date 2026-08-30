package com.duoc.banco.item;

import javax.sql.DataSource;

import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SingleItemPeekableItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.DataClassRowMapper;

import com.duoc.banco.exception.MovimientoCuentaNoValidoException;
import com.duoc.banco.model.EstadoCuenta;
import com.duoc.banco.model.MovimientoCuenta;

@Configuration
public class EstadoCuentaItemReaderConfig {

    @Bean
    public JdbcCursorItemReader<MovimientoCuenta> movimientoTablaItemReader(DataSource dataSource) {
        // Obtener movimientos de cuenta desde la base de datos ordenados por ID
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

   @Bean
    public ItemReader<EstadoCuenta> estadoCuentaItemReader(
        SingleItemPeekableItemReader<MovimientoCuenta> movimientoCuentaPeekableReader
    ) {
        return () -> {
            // Se extrae el primer movimiento de la tabla (obtenido por movimientoCuentaPeekableReader)
            MovimientoCuenta primerMov = movimientoCuentaPeekableReader.read();
            if (primerMov == null) {
                return null; // Fin de los datos (detiene el Step)
            }

            Long cuentaIdActual = primerMov.getCuentaId();
            int ingresos = 0;
            int salidas = 0;

            // Agregar el primer movimiento a los montos acumulados
            if (primerMov.getMonto() > 0) {
                ingresos += primerMov.getMonto();
            } else if (primerMov.getMonto() < 0) {
                salidas += Math.abs(primerMov.getMonto());
            } else {
                throw new MovimientoCuentaNoValidoException("El monto del movimiento no puede ser 0");
            }

            // Seguir leyendo mientras el siguiente movimiento pertenezca a la misma cuenta
            MovimientoCuenta proximoMov = movimientoCuentaPeekableReader.peek();
            while (proximoMov != null && proximoMov.getCuentaId().equals(cuentaIdActual)) {
                
                // Consume (lee) el movimiento validado en el peek anterior
                MovimientoCuenta movConsumido = movimientoCuentaPeekableReader.read();
                
                if (movConsumido.getMonto() > 0) {
                    ingresos += movConsumido.getMonto();
                } else if (movConsumido.getMonto() < 0) {
                    salidas += Math.abs(movConsumido.getMonto());
                } else {
                    throw new MovimientoCuentaNoValidoException("El monto del movimiento no puede ser 0");
                }
                
                // Mira el siguiente elemento en la fila sin extraerlo
                proximoMov = movimientoCuentaPeekableReader.peek();
            }

            // Retorna el EstadoCuenta resultante para la cuenta actual
            int diferenciaTotal = ingresos - salidas;
            return new EstadoCuenta(cuentaIdActual, ingresos, salidas, diferenciaTotal);
        };
    }

}
