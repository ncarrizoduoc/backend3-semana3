package com.duoc.banco.listener.skip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

import com.duoc.banco.model.EstadoCuenta;
import com.duoc.banco.model.MovimientoCuenta;

@Component
public class EstadoCuentaSkipListener implements SkipListener<MovimientoCuenta, EstadoCuenta>{

    private static final Logger logger = LoggerFactory.getLogger(EstadoCuentaSkipListener.class);

    public EstadoCuentaSkipListener(){
        logger.info("Creando EstadoCuentaSkipListener");
    }

    @Override
    public void onSkipInRead(Throwable t){
        logger.warn("Error al leer movimiento de cuenta desde base de datos: " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(MovimientoCuenta mov, Throwable t){
        logger.warn("Error al procesar estado de cuenta: " + mov + ". Causa del error: " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(EstadoCuenta estado, Throwable t){
        logger.warn("Error al escribir estado de cuenta: " + estado + ". Causa del error: " + t.getMessage());
    }

}
