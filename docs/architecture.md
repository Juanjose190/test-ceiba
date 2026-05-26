# Decisiones de Arquitectura

## Contexto

La empresa necesita reemplazar hojas de cálculo por una API que controle disponibilidad de bicicletas, registre alquileres y calcule cobros/multas sin errores manuales. Aunque el dominio es pequeño, las reglas de cobro son sensibles: un cálculo mal implementado impacta directamente al cliente y a la operación.

## Drivers Arquitectónicos

### Funcionales

- Registrar bicicletas con código único, tipo y estado.
- Iniciar alquileres solo sobre bicicletas disponibles.
- Finalizar alquileres calculando costo base, multa y liberando la bicicleta.
- Consultar disponibilidad con filtro opcional por tipo.
- Consultar historial por bicicleta.

### No Funcionales

| Driver | Prioridad | Impacto arquitectónico |
| --- | --- | --- |
| Correctitud de reglas de negocio | Alta | La lógica de cobro debe quedar aislada y cubierta por pruebas unitarias. |
| Simplicidad operacional | Alta | La app debe ejecutarse localmente y con Docker sin base de datos externa. |
| Mantenibilidad | Alta | Separación por responsabilidades para modificar reglas sin tocar controladores. |
| Observabilidad básica | Media | Health checks con Spring Actuator para validar arranque y readiness. |
| Seguridad básica | Media | Autenticación HTTP Basic, sesiones stateless y credenciales por variables de entorno. |
| Evolución a persistencia real | Media | Repositorios encapsulan el almacenamiento actual en memoria. |
| Escalabilidad | Baja/Media | El volumen esperado para una prueba técnica no justifica microservicios ni mensajería. |

## Restricciones y Supuestos

- Spring Boot es obligatorio.
- El enunciado no exige base de datos ni despliegue en nube.
- El plazo es corto, por lo que la arquitectura debe maximizar claridad y confiabilidad antes que infraestructura.
- Se documenta como supuesto que el almacenamiento es en memoria y se reinicia al levantar la aplicación.

## Estilos Candidatos

| Estilo | Ventajas | Costos / trade-offs | Encaje |
| --- | --- | --- | --- |
| Capas clásicas | Simple, familiar para Spring, rápida de revisar. | Puede mezclar dominio con aplicación si se descuida. | Bueno para el tamaño actual. |
| Hexagonal / Puertos y Adaptadores | Aísla dominio e infraestructura, facilita cambiar persistencia. | Más clases/interfaces para un dominio pequeño. | Bueno como evolución si se agrega BD. |
| Clean Architecture estricta | Alta independencia del framework. | Sobredimensionada para una API de prueba pequeña. | Parcial, no estricta. |
| Microservicios | Escalado independiente y ownership por dominio. | Complejidad operativa, red, consistencia distribuida, despliegue. | Descartado. |
| Event-driven | Útil para auditoría y procesos asíncronos. | Añade broker, contratos y eventual consistency. | Futuro posible, no necesario ahora. |

## Decisión

Se eligió una arquitectura por capas con orientación de dominio:

- `model`: entidades y enums del dominio.
- `service`: reglas de negocio y casos de uso.
- `repository`: frontera de acceso a datos.
- `controller`: API REST y DTOs.
- `exception`: traducción uniforme de errores a HTTP.
- `config`: configuración transversal, seguridad, datos iniciales y reloj.

La decisión busca un equilibrio: suficientemente simple para ser revisada rápido, pero con puntos de extensión claros. La lógica crítica de dinero y tiempo vive en `RentalCostCalculator`, una clase pequeña, pura y testeable. Los repositorios en memoria permiten cumplir el alcance sin atar el dominio a una tecnología de base de datos.

## Trade-offs

- Se sacrifica persistencia real para mantener foco en reglas de negocio y pruebas. El costo de migrar a JPA o MongoDB queda contenido en la capa `repository`.
- Se usa HTTP Basic por simplicidad. Para producción se migraría a OAuth2/JWT o integración con un proveedor de identidad.
- Se evita microservicios porque el dominio no tiene límites suficientemente grandes ni carga que justifique complejidad distribuida.
- Se documentan supuestos explícitos en README para que el evaluador vea criterio ante ambigüedades.

## Atributos de Calidad Cubiertos

- Correctitud: tests unitarios para redondeo, multa, estado y finalización doble.
- Seguridad básica: API protegida por usuario/contraseña configurable.
- Observabilidad: `/actuator/health`, liveness y readiness probes.
- Portabilidad: Dockerfile multi-stage y `docker-compose.yml`.
- Mantenibilidad: servicios enfocados y DTOs separados del modelo.

## Posible Evolución

1. Agregar persistencia con PostgreSQL y Spring Data JPA.
2. Introducir migraciones con Flyway.
3. Separar interfaces de repositorio como puertos si aparecen nuevas fuentes de datos.
4. Agregar OpenAPI/Swagger para contrato interactivo.
5. Publicar eventos de `RentalStarted` y `RentalFinished` para auditoría o facturación externa.
6. Agregar pruebas de integración con MockMvc/Testcontainers cuando exista base de datos.
