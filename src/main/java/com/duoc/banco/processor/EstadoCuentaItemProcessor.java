package com.duoc.banco.processor;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.banco.exception.EstadoCuentaNoValidoException;
import com.duoc.banco.model.EstadoCuenta;

@Component
public class EstadoCuentaItemProcessor implements ItemProcessor<EstadoCuenta, EstadoCuenta> {

     @Override
    public EstadoCuenta process(EstadoCuenta estadoCuenta) {
        // El procesador ya recibe el objeto consolidado directo desde el Reader
        // Aquí puedes añadir lógica de negocio o auditorías si es necesario.
        
        // Ejemplo opcional: Filtrar cuentas que no tuvieron movimientos reales
        if (estadoCuenta.getIngresos() == 0 && estadoCuenta.getSalidas() == 0) {
            /*
            throw new EstadoCuentaNoValidoException(
                "Error al procesar estado de cuenta con ID: " + estadoCuenta.getCuentaId() +
                " . La cuenta no registra movimientos."
            );
            */
           return null;
        }
        
        return estadoCuenta;
    }
}
