package com.duoc.banco.exception;

public class EstadoCuentaNoValidoException extends RuntimeException{

    public EstadoCuentaNoValidoException(String message) {
        super(message);
    }

    public EstadoCuentaNoValidoException(String message, Throwable cause) {
        super(message, cause);
    }

}
