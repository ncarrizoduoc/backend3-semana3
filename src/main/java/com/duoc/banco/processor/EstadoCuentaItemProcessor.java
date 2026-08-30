package com.duoc.banco.processor;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.banco.model.EstadoCuenta;

@Component
public class EstadoCuentaItemProcessor implements ItemProcessor<EstadoCuenta, EstadoCuenta> {

     @Override
    public EstadoCuenta process(EstadoCuenta estadoCuenta) {
        // El ItemProcessor recibe el EstadoCuenta consolidado desde el Reader
        
        // Filtrar cuentas que no tuvieron movimientos (ni entradas ni salidas)
        if (estadoCuenta.getIngresos() == 0 && estadoCuenta.getSalidas() == 0) {
            return null;
        }
        
        return estadoCuenta;
    }
}
