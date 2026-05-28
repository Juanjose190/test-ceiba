# Despliegue en AWS

La API está desplegada en Elastic Beanstalk con RDS (PostgreSQL). El perfil Spring `aws`
lee las variables de entorno que Beanstalk inyecta para la conexión a base de datos.

## API desplegada

```
http://bike-rental-api-prod.eba-feizmgrc.us-east-1.elasticbeanstalk.com
```

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Health check público |
| `/api/bicicletas/disponibles` | Requiere autenticación |

Credenciales:

```
usuario: admin
password: J5QI3eGTkUEa0a1TVO3UAHDt
```

> Ambiente temporal de evaluación — se destruye al finalizar la revisión.

## Por qué Beanstalk

Beanstalk es suficiente para este dominio y usa componentes reales de AWS: EC2,
load balancer, RDS, S3 para artefactos y health checks nativos. ECS Fargate + RDS
separado también funcionaría pero añade superficie de red e IAM innecesaria para
una API de este tamaño.

En producción la base de datos debería tener ciclo de vida independiente, subredes
privadas, Secrets Manager y migraciones con Flyway o Liquibase. Acá está acoplada
a Beanstalk por simplicidad.

## Reproducir el despliegue

Requiere AWS CLI configurado con un usuario IAM o SSO con permisos suficientes
(no usar credenciales root).

```bash
# Verificar sesión
aws sts get-caller-identity

# Exportar secretos
export AWS_REGION=us-east-1
export DB_PASSWORD='tu-password-para-rds'
export APP_SECURITY_PASSWORD='tu-password-para-la-api'

# Desplegar
bash deploy/aws/deploy-elastic-beanstalk.sh
```

Una vez listo, obtener la URL:

```bash
aws elasticbeanstalk describe-environments \
  --application-name bike-rental-api \
  --environment-names bike-rental-api-prod \
  --query 'Environments[0].CNAME' \
  --output text
```

## Destruir

```bash
bash deploy/aws/destroy-elastic-beanstalk.sh
```

El script marca la base de datos con política `Snapshot`, así que Beanstalk genera
un snapshot final de RDS antes de eliminar el ambiente.
