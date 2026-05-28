# Bike Rental API

API REST en Spring Boot para registrar bicicletas, iniciar/finalizar alquileres, consultar disponibilidad y revisar el historial por bicicleta.

## Tecnologías

- Java 21
- Spring Boot 3.5
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL
- Jakarta Bean Validation
- Spring Security
- Spring Actuator
- JUnit 5 + AssertJ
- H2 para pruebas automatizadas de persistencia
- Docker + Docker Compose

## Arquitectura

La solución usa una arquitectura por capas:

- `controller`: expone los endpoints REST y traduce DTOs.
- `service`: contiene las reglas de negocio y coordina cambios de estado.
- `repository`: define acceso a datos con Spring Data JPA.
- `model`: entidades y enums del dominio.
- `exception`: manejo centralizado de errores JSON con códigos HTTP apropiados.

Elegí una arquitectura por capas con persistencia relacional en PostgreSQL. Aunque el enunciado permite libertad en almacenamiento, Postgres aporta un diferencial razonable: datos persistentes, restricciones por identificadores, portabilidad con Docker y una ruta clara hacia consultas/reportes operativos. La lógica de tarifas y multas está aislada en `RentalCostCalculator`, lo que facilita probarla sin depender de controladores.

La decisión arquitectónica completa está documentada en `docs/architecture.md`, incluyendo drivers no funcionales, estilos candidatos y trade-offs.

## Drivers y Trade-offs

- **Correctitud de reglas de negocio:** el cálculo de dinero y tiempo está aislado y probado.
- **Mantenibilidad:** controladores, servicios, repositorios, DTOs y excepciones están separados.
- **Persistencia confiable:** PostgreSQL evita perder alquileres al reiniciar la aplicación.
- **Consistencia ante concurrencia:** las operaciones críticas usan transacciones y bloqueo pesimista para no alquilar dos veces la misma bicicleta.
- **Simplicidad operacional:** Docker Compose levanta API y base de datos con una sola orden.
- **Seguridad básica:** autenticación HTTP Basic, API stateless y credenciales por variables de entorno.
- **Observabilidad básica:** endpoints de health/liveness/readiness con Spring Actuator.

Trade-off principal: PostgreSQL agrega configuración frente a una solución en memoria, pero mejora el realismo del entregable y permite validar persistencia con JPA sin sobredimensionar hacia microservicios.

## Supuestos

- `MONTAÑA` y `ELÉCTRICA` se aceptan con o sin tildes en JSON (`MONTAÑA`/`MONTANA`, `ELÉCTRICA`/`ELECTRICA`).
- Si no se envía `startTime` al iniciar un alquiler, se usa la fecha/hora actual del servidor.
- Si no se envía `endTime` al finalizar un alquiler, se usa la fecha/hora actual del servidor.
- La duración estimada se registra en horas enteras y debe ser mayor o igual a 1.
- Los datos iniciales se cargan de forma idempotente: si la bicicleta ya existe, no se duplica ni falla el arranque.

## Datos Iniciales

Al iniciar la aplicación se cargan estas bicicletas:

| Código  | Tipo     | Estado inicial    |
|---------|----------|-------------------|
| BIC-001 | URBANA   | DISPONIBLE        |
| BIC-002 | MONTAÑA  | DISPONIBLE        |
| BIC-003 | ELÉCTRICA| DISPONIBLE        |
| BIC-004 | MONTAÑA  | EN_MANTENIMIENTO  |
| BIC-005 | URBANA   | DISPONIBLE        |

## Ejecutar Localmente

Primero levanta PostgreSQL:

```bash
docker compose up -d postgres
```

Luego ejecuta la API:

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

Credenciales por defecto:

```
usuario: admin
password: admin123
```

Puedes cambiarlas con variables de entorno:

```bash
APP_SECURITY_USERNAME=operador APP_SECURITY_PASSWORD=secreto mvn spring-boot:run
```

Configuración por defecto de base de datos local:

```
url:      jdbc:postgresql://localhost:5432/bike_rental
usuario:  bike_user
password: bike_password
```

## Ejecutar con Docker

```bash
docker compose up --build
```

Esto levanta dos servicios: `postgres` (PostgreSQL 16) y `bike-rental-api` (Spring Boot conectada a Postgres).

```bash
# Validar
curl http://localhost:8080/actuator/health

# Detener
docker compose down
```

## Despliegue en AWS

El proyecto incluye una ruta de despliegue en AWS usando Elastic Beanstalk + RDS PostgreSQL.
La guía y los scripts están en `deploy/aws/`.

La API se despliega como contenedor Docker en Elastic Beanstalk. PostgreSQL se provisiona
con RDS y el perfil `aws` toma las variables que Beanstalk inyecta automáticamente.

API desplegada:

```
http://bike-rental-api-prod.eba-feizmgrc.us-east-1.elasticbeanstalk.com
```

Credenciales de prueba:

```
usuario: admin
password: J5QI3eGTkUEa0a1TVO3UAHDt
```

> Ambiente temporal de evaluación — se destruye al finalizar la revisión.

Para evitar costos después de la prueba:

```bash
bash deploy/aws/destroy-elastic-beanstalk.sh
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

# Filtrando por tipo
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

Reemplaza `<ID_ALQUILER>` con el id devuelto al iniciar el alquiler.

```bash
curl -X PUT http://localhost:8080/api/alquileres/<ID_ALQUILER>/finalizar \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "endTime": "2026-04-28T11:20:00"
  }'
```

Para una bicicleta MONTAÑA, estimada en 2 horas y devuelta a las 3h20min, el total será $25.000: 4 horas facturadas de uso real y 2 horas de multa.

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

## Pruebas Automatizadas

El proyecto incluye dos niveles de pruebas:

- **Tests unitarios puros** para `RentalCostCalculator`, sin Spring ni base de datos.
- **Tests de integración** para `RentalService`, usando Spring Boot + JPA + H2 en modo PostgreSQL.

Reglas cubiertas:

- Redondeo al alza del tiempo real de uso.
- No redondear cuando el uso cae exactamente en una hora completa.
- Cálculo de multa por retraso con redondeo al alza.
- Rechazo de alquiler sobre bicicleta no disponible.
- Cambio de estado al iniciar/finalizar alquiler.
- Rechazo de finalización doble de alquiler.
- Persistencia del costo total y marca de multa al finalizar tarde.

Los tests de integración usan el perfil `test`, configurado en `src/test/resources/application-test.properties`, para validar JPA sin requerir una instancia externa de Postgres en CI o en la máquina del evaluador.

## Decisiones de Inyección de Dependencias

El proyecto usa inyección por constructor. Se evita la inyección por campo con `@Autowired` porque dificulta pruebas, inmutabilidad y lectura de dependencias obligatorias:

```java
@Service
public class RentalService {

    private final BicycleRepository bicycleRepository;
    private final RentalRepository rentalRepository;

    public RentalService(BicycleRepository bicycleRepository, RentalRepository rentalRepository) {
        this.bicycleRepository = bicycleRepository;
        this.rentalRepository = rentalRepository;
    }
}
```

## Evolución Propuesta: Idempotencia

Como mejora futura, los endpoints críticos de escritura podrían aceptar un header `Idempotency-Key`. Esto permitiría que un cliente reintente una solicitud después de un timeout o fallo de red sin riesgo de crear dos alquileres o finalizar dos veces la misma operación.

La API persistiría la llave, el hash del request y la respuesta generada. Si llega el mismo request con la misma llave, se devuelve la respuesta original; si llega la misma llave con un cuerpo diferente, se rechaza como conflicto. Es una mejora típica en APIs de pagos, reservas y operaciones sensibles.
