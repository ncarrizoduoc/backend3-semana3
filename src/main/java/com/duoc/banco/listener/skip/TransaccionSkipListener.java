package com.duoc.banco.listener.skip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

import com.duoc.banco.model.Transaccion;

@Component
public class TransaccionSkipListener implements SkipListener<Transaccion, Transaccion>{

    private static final Logger logger = LoggerFactory.getLogger(TransaccionSkipListener.class);

    public TransaccionSkipListener(){
        logger.info("Creando TransaccionSkipListener");
    }

    @Override
    public void onSkipInRead(Throwable t){
        logger.warn("Error al leer la transaccion: " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(Transaccion transaccion, Throwable t){
        logger.warn("Error al procesar la transaccion: " + transaccion + ". Causa del error: " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(Transaccion transaccion, Throwable t){
        logger.warn("Error al escribir la transaccion: " + transaccion + ". Causa del error: " + t.getMessage());
    }

}
