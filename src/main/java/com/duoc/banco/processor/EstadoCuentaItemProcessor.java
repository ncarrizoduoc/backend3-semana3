package com.duoc.banco.processor;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.banco.exception.MovimientoCuentaNoValidoException;
import com.duoc.banco.model.EstadoCuenta;
import com.duoc.banco.model.MovimientoCuenta;

@Component
public class EstadoCuentaItemProcessor implements ItemProcessor<MovimientoCuenta, EstadoCuenta> {

    // Valores acumulados (ingresos, salidas y diferencia) de una cuenta
    private Integer ingresosActuales = 0;
    private Integer salidasActuales = 0;
    private Integer diferenciaTotal = 0;

    @Override
    public EstadoCuenta process(MovimientoCuenta movimiento) {
        if (movimiento.getDescripcion() == null || movimiento.getDescripcion().isBlank()){
            // Si la descripcion del movimiento es null o blank, lanza excepcion
            throw new MovimientoCuentaNoValidoException("El movimiento no tiene descripcion");
        }

        // Se suma el monto del movimiento a los valores acumulados de la cuenta
        sumarMovimiento(movimiento);
        
        // Si un movimiento es el ultimo de la lista para una cuenta, se retorna el estado de 
        // cuenta acumulado
        if (movimiento.isUltimoDelGrupo()){
            EstadoCuenta estadoCuenta = new EstadoCuenta(
                movimiento.getCuentaId(),
                ingresosActuales,
                salidasActuales,
                diferenciaTotal
            );

            // Se reinician los valores acumulados y se retorna el estado de cuenta
            reset();
            return estadoCuenta;
        }
        return null;
    }

    // Sumar el monto de un movimiento de cuenta a los totales acumulados
    private void sumarMovimiento(MovimientoCuenta movimiento) throws MovimientoCuentaNoValidoException {
        if (movimiento.getMonto() > 0) {
            ingresosActuales += movimiento.getMonto();
        } else if (movimiento.getMonto() < 0) {
            salidasActuales += Math.abs(movimiento.getMonto());
        } else {
            // Si el monto del movimiento es 0, lanza una excepcion
            throw new MovimientoCuentaNoValidoException("El monto del movimiento no puede ser 0");
        }
        diferenciaTotal = ingresosActuales - salidasActuales;
    }

    // Reiniciar valores acumulados
    private void reset() {
        ingresosActuales = 0;
        salidasActuales = 0;
        diferenciaTotal = 0;
    }
}
