DROP TABLE IF EXISTS transacciones CASCADE;
DROP TABLE IF EXISTS intereses CASCADE;
DROP TABLE IF EXISTS movimiento_cuenta CASCADE;
DROP TABLE IF EXISTS estado_cuenta CASCADE;

CREATE TABLE IF NOT EXISTS transacciones (
    id BIGINT PRIMARY KEY,
    fecha DATE NOT NULL,
    monto INT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    observaciones VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS intereses (
    cuenta_id BIGINT,
    nombre VARCHAR(100) NOT NULL,
    saldo_inicial INT NOT NULL,
    saldo_final INT NOT NULL,
    edad INT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    tasa_interes DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (cuenta_id, tipo)
);

-- Tabla para almacenar y ordenar los movimientos de cuentas_anuales.csv
CREATE TABLE IF NOT EXISTS movimiento_cuenta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    monto INT NOT NULL,
    descripcion VARCHAR(255) NOT NULL
);

-- Index para acelerar el ordenamiento por cuenta_id de la tabla MOVIMIENTO_CUENTA
CREATE INDEX idx_movimiento_cuenta_cuenta_id ON movimiento_cuenta (cuenta_id);

CREATE TABLE IF NOT EXISTS estado_cuenta (
    cuenta_id BIGINT PRIMARY KEY,
    ingresos INT NOT NULL,
    salidas INT NOT NULL,
    diferencia INT NOT NULL
);