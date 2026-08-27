package com.duoc.banco.listener.skip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

import com.duoc.banco.model.Interes;

@Component
public class InteresSkipListener implements SkipListener<Interes, Interes>{

    private static final Logger logger = LoggerFactory.getLogger(InteresSkipListener.class);

    public InteresSkipListener(){
        logger.info("Creando InteresSkipListener");
    }

    @Override
    public void onSkipInRead(Throwable t) {
        logger.warn("Error al leer interes: " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(Interes interes, Throwable t){
        logger.warn("Error al procesar interes: " + interes + ". Causa del error: " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(Interes interes, Throwable t){
        logger.warn("Error al escribir interes: " + interes + ". Causa del error: " + t.getMessage());
    }

}
