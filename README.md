# Proyecto Spring Batch - Banco XYZ
El objetivo de este proyecto es definir una serie de operaciones automatizadas sobre un conjunto de datos de un banco.
La primera operación busca detectar anomalías en transacciones monetarias (como montos o fecha no válidos).
La segunda operación calculará el saldo resultante para cuentas bancarias según la tasa de interés correspondiente al tipo de cuenta.
La tercera operación generará estados de cuenta anuales según los movimientos registrados para cada cuenta. Cada estado de cuenta incluirá los ingresos totales, salidas (o gastos) totales y diferencia de saldo resultante.

## Requisitos previos
- **Java 21**: Asegúrate de tener instalado JDK 21.
- **Maven 3.9.x** o superior: Para compilar y ejecutar el proyecto.

## Tecnologías utilizadas
- **Java 21**
- **Spring Batch**
- **Spring Boot**
- **Maven**

## Estructura del proyecto
El proyecto incluye clases de configuración para ejecutar los distintos Jobs, así como los archivos .csv que se usarán obtener los datos bancarios:

### Package model
Incluye las clases que se usarán para representar los datos y procesarlos:

- **`EstadoCuenta`**: Clase que representa el estado anual de una cuenta bancaria.
- **`Interes`**: Clase que representa el saldo resultante de una cuenta tras aplicar la tasa de interés correspondientes según su tipo de cuenta. Incluye atributos como `saldoInicial`, `saldoFinal` y `tasaInteres`.
- **`MovimientoCuenta`**: Clase que representa un movimiento (de ingreso o salida de saldo) de una cuenta bancaria.
- **`Transaccion`**: Clase que representa una transacción. Incluye el atributo `observaciones` para registrar anomalías en la transacción.

### Package config
Contiene las clases de configuración de los Jobs, Steps y objetos ThreadPoolTaskExecutor (para ejecucion multihilo):

- **`BatchConfig`**: Clase donde se crean los Steps y Jobs.
- **`BatchJobRunner`**: Clase que orquesta la ejecución en secuencia de los Jobs.
- **`DataSourceConfig`**: Clase que configura la ejecución de consultas y transacciones a la base de datos.
- **`TaskExecutorConfig`**: Clase que crea los Beans `TaskExecutor`, encargados de la ejecución multihilo de los Steps.

### Package exception
Contiene excepciones personalizadas para ser usadas por las clases del package `item` y `processor` durante el procesamiento de datos:

- **`EstadoCuentaNoValidoException`**
- **`InteresNoValidoException`**
- **`MovimientoCuentaNoValidoException`**

### Package item
Contiene las clases de configuración de Readers y Writers:

- **`EstadoCuentaItemReaderConfig`**: Clase que configura la lectura de movimientos de cuenta desde la tabla `MOVIMIENTO_CUENTA` de la base de datos.
- **`EstadoCuentaItemWriterConfig`**: Clase que configura el guardado de objetos `EstadoCuenta` en la tabla `ESTADO_CUENTA` de la base de datos.
- **`InteresItemReaderConfig`**: Clase que configura la lectura de datos de cuentas bancarias desde el archivo `intereses.csv`.
- **`InteresItemWriterConfig`**: Clase que configura el guardado de objetos `Interes` en la tabla `INTERESES` de la base de datos.
- **`MovimientoCuentaItemReaderConfig`**: Clase que configura la lectura de movimientos de cuenta desde el archivo `cuentas_anuales.csv`.
- **`MovimientoCuentaItemWriterConfig`**: Clase que configura el guardado de objetos `MovimientoCuenta` en la tabla `MOVIMIENTO_CUENTA` de la base de datos.
- **`TransaccionItemReaderConfig`**: Clase que configura la lectura de datos de transacciones desde el archivo `transacciones.csv`.
- **`TransaccionItemWriterConfig`**: Clase que configura el guardado de objetos `Transaccion` en la tabla `TRANSACCIONES` de la base de datos.

### Package listener
Contiene listeners que registran, a traves de logs, la ejecución de Steps, Jobs y skips:

- **`BancoStepExecutionListener`**: Registra un log al iniciar y al finalizar un Step.

### Package listener/job
Contiene listeners que registran, en consola, la ejecución de Jobs

- **`EstadoCuentaCompletionListener`**: Registra un log al iniciar y finalizar el Job `generarEstadosDeCuentaJob`.
- **`InteresJobCompletionListener`**: Registra un log al iniciar y finalizar el Job `InteresJob`.
- **`TransaccionJobCompletionListener`**: Registra un log al iniciar y finalizar el Job `TransaccionJob`.

### Package listener/skip
Contiene listeners que registran, en consola, cuando se produce un skip durante un Step:

- **`EstadoCuentaSkipListener`**: Registra un log cuando se produce un skip durante el Step `EstadoCuentaStep`.
- **`IntresSkipListener`**: Registra un log cuando se produce un skip durante el Step `InteresStep`.
- **`MovimientoCuentaSkipListener`**: Registra un log cuando se produce un skip durante el Step `MovimientoCuentaStep`.
- **`TransaccionSkipListener`**: Registra un log cuando se produce un skip durante el Step `TransaccionStep`.


### Package processor
Contiene las clases que definen el procesamiento de datos durante los Jobs:

- **`EstadoCuentaItemProcessor`**: Clase que procesa los movimientos de las cuentas bancarias y genera estados de cuenta acumulados, incluyendo los ingresos totales, salidas totales y diferencia de saldo resultante.
- **`InteresItemProcessor`**: Clase que calcula el saldo final de cada cuenta bancaria, según su saldo inicial y tasa de interés correspondiente al tipo de cuenta.
- **`TransaccionItemProcessor`**: Clase que procesa las transacciones, añadiendo observaciones si detecta anomalías en los datos de la transacción o el titular de la cuenta.

# Como utilizar
Para poder utilizar este proyecto, debes modificar los parámetros de conexión a la base de datos en `application.properties` para ajustarlos a tu base de datos. Luego, deberás ejecutar el proyecto (main class: `BancoApplication.java`).