# Bike Rental API

API REST en Spring Boot para registrar bicicletas, iniciar/finalizar alquileres, consultar disponibilidad y revisar el historial por bicicleta.

## Tecnologías

- Java 21
- Spring Boot 3.5
- Maven
- Spring Web
- Jakarta Bean Validation
- JUnit 5 + AssertJ
- Repositorios en memoria con `ConcurrentHashMap`

## Arquitectura

La solución usa una arquitectura por capas:

- `controller`: expone los endpoints REST y traduce DTOs.
- `service`: contiene las reglas de negocio y coordina cambios de estado.
- `repository`: abstrae el almacenamiento en memoria.
- `model`: entidades y enums del dominio.
- `exception`: manejo centralizado de errores JSON con códigos HTTP apropiados.

Elegí almacenamiento en memoria porque el enunciado no exige persistencia y esto mantiene el foco en las reglas de negocio evaluadas. La lógica de tarifas y multas está aislada en `RentalCostCalculator`, lo que facilita probarla sin depender de Spring ni de controladores.

La decisión arquitectónica completa está documentada en [docs/architecture.md](docs/architecture.md), incluyendo drivers no funcionales, estilos candidatos y trade-offs.

## Drivers y Trade-offs

Drivers principales:

- Correctitud de reglas de negocio: el cálculo de dinero y tiempo está aislado y probado.
- Mantenibilidad: controladores, servicios, repositorios, DTOs y excepciones están separados.
- Simplicidad operacional: funciona sin dependencias externas y también con Docker.
- Seguridad básica: autenticación HTTP Basic, API stateless y credenciales por variables de entorno.
- Observabilidad básica: endpoints de health/liveness/readiness con Spring Actuator.

Trade-off principal: se usa almacenamiento en memoria para priorizar claridad y alcance. Si el sistema crece, la capa `repository` permite migrar a PostgreSQL/JPA sin reescribir controladores ni reglas de negocio.

## Supuestos

- `MONTAÑA` y `ELÉCTRICA` se aceptan con o sin tildes en JSON (`MONTAÑA`/`MONTANA`, `ELÉCTRICA`/`ELECTRICA`).
- Si no se envía `startTime` al iniciar un alquiler, se usa la fecha/hora actual del servidor.
- Si no se envía `endTime` al finalizar un alquiler, se usa la fecha/hora actual del servidor.
- La duración estimada se registra en horas enteras y debe ser mayor o igual a 1.
- Los datos se reinician cada vez que arranca la aplicación.

## Datos Iniciales

Al iniciar la aplicación se cargan estas bicicletas:

| Código | Tipo | Estado inicial |
| --- | --- | --- |
| BIC-001 | URBANA | DISPONIBLE |
| BIC-002 | MONTAÑA | DISPONIBLE |
| BIC-003 | ELÉCTRICA | DISPONIBLE |
| BIC-004 | MONTAÑA | EN_MANTENIMIENTO |
| BIC-005 | URBANA | DISPONIBLE |

## Ejecutar Localmente

```bash
mvn spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8080
```

Credenciales por defecto:

```text
usuario: admin
password: admin123
```

Puedes cambiarlas con variables de entorno:

```bash
APP_SECURITY_USERNAME=operador APP_SECURITY_PASSWORD=secreto mvn spring-boot:run
```

## Ejecutar con Docker

Construir y levantar:

```bash
docker compose up --build
```

Validar health check:

```bash
curl http://localhost:8080/actuator/health
```

Detener:

```bash
docker compose down
```

## Ejecutar Tests

```bash
mvn test
```

## Endpoints Principales

### Registrar Bicicleta

```bash
curl -X POST http://localhost:8080/api/bicicletas \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "code": "BIC-006",
    "type": "URBANA",
    "status": "DISPONIBLE"
  }'
```

### Consultar Bicicletas Disponibles

```bash
curl -u admin:admin123 http://localhost:8080/api/bicicletas/disponibles
```

Filtrando por tipo:

```bash
curl -u admin:admin123 "http://localhost:8080/api/bicicletas/disponibles?type=MONTAÑA"
```

### Iniciar Alquiler

```bash
curl -X POST http://localhost:8080/api/alquileres \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "bicycleCode": "BIC-002",
    "customerName": "Laura Gómez",
    "startTime": "2026-04-28T08:00:00",
    "estimatedDurationHours": 2
  }'
```

### Finalizar Alquiler

Reemplaza `<ID_ALQUILER>` con el `id` devuelto al iniciar el alquiler.

```bash
curl -X PUT http://localhost:8080/api/alquileres/<ID_ALQUILER>/finalizar \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "endTime": "2026-04-28T11:20:00"
  }'
```

Para una bicicleta `MONTAÑA`, estimada en 2 horas y devuelta a las 3h20min, el total será `$25.000`: 4 horas facturadas de uso real y 2 horas de multa.

### Historial de Alquileres por Bicicleta

```bash
curl -u admin:admin123 http://localhost:8080/api/bicicletas/BIC-002/alquileres
```

## Manejo de Errores

Los errores se responden en JSON. Ejemplo al intentar alquilar una bicicleta en mantenimiento:

```json
{
  "timestamp": "2026-04-28T10:00:00",
  "status": 409,
  "error": "Conflict",
  "messages": [
    "La bicicleta BIC-004 no está disponible. Estado actual: EN_MANTENIMIENTO"
  ]
}
```

## Reglas Probadas

- Redondeo al alza del tiempo real de uso.
- No redondear cuando el uso cae exactamente en una hora completa.
- Cálculo de multa por retraso con redondeo al alza.
- Rechazo de alquiler sobre bicicleta no disponible.
- Cambio de estado al iniciar/finalizar alquiler.
- Rechazo de finalización doble de alquiler.
