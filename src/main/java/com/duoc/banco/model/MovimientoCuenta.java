package com.duoc.banco.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCuenta {
    private Long id;
    private Long cuentaId;
    private Integer monto;
    private String descripcion;
    private boolean ultimoDelGrupo = false;

}
