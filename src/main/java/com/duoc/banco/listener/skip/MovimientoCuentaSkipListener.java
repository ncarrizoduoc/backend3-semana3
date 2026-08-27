package com.duoc.banco.listener.skip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

import com.duoc.banco.model.MovimientoCuenta;

@Component
public class MovimientoCuentaSkipListener implements SkipListener<MovimientoCuenta, MovimientoCuenta>{

    private static final Logger logger = LoggerFactory.getLogger(MovimientoCuentaSkipListener.class);

    public MovimientoCuentaSkipListener(){
        logger.info("Creando MovimientoCuentaSkipListener");
    }

    @Override
    public void onSkipInRead(Throwable t){
        logger.warn("Error al leer movimiento de cuenta: " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(MovimientoCuenta mov, Throwable t){
        logger.warn("Error al procesar movimiento de cuenta: " + mov + ". Causa del error: " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(MovimientoCuenta mov, Throwable t){
        logger.warn("Error al escribir movimiento de cuenta: " + mov + ". Causa del error: " + t.getMessage());
    }

}
