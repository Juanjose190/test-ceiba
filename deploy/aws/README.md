# Despliegue en AWS

Esta carpeta contiene una ruta de despliegue práctica para la prueba técnica:

- Elastic Beanstalk ejecuta la API Spring Boot dockerizada.
- PostgreSQL se administra con RDS dentro del ambiente de Elastic Beanstalk.
- El perfil Spring `aws` lee las variables de entorno que Elastic Beanstalk expone para RDS.
- El endpoint de Actuator se usa como health check del ambiente.

## Para Evaluar la API Desplegada

La API ya está desplegada en:

```text
http://bike-rental-api-prod.eba-feizmgrc.us-east-1.elasticbeanstalk.com
```

Health check público:

```text
http://bike-rental-api-prod.eba-feizmgrc.us-east-1.elasticbeanstalk.com/actuator/health
```

Endpoint principal:

```text
http://bike-rental-api-prod.eba-feizmgrc.us-east-1.elasticbeanstalk.com/api/bicicletas/disponibles
```

Credenciales de prueba:

```text
usuario: admin
password: J5QI3eGTkUEa0a1TVO3UAHDt
```

Estas credenciales son solo para probar la API desplegada. El evaluador no necesita acceso a la cuenta de AWS.

## Por Qué Esta Opción

Elastic Beanstalk mantiene el despliegue entendible para una API pequeña, pero usa componentes reales de AWS: EC2, load balancer/proxy, RDS, artefactos en S3, variables de entorno y health checks.

ECS Fargate + RDS separado también sería válido, pero agrega más superficie de red, IAM y configuración. Para una prueba técnica, Beanstalk ofrece un buen balance entre valor técnico y simplicidad operativa.

## Prerrequisitos Para Desplegar Tu Propia Copia

1. Configurar AWS localmente. Es preferible usar IAM Identity Center/SSO o un usuario IAM con permisos limitados. No usar credenciales root.
2. Validar la sesión:

```bash
aws sts get-caller-identity
```

3. Exportar los secretos requeridos:

```bash
export AWS_REGION=us-east-1
export DB_PASSWORD='cambia-esto-por-una-password-fuerte'
export APP_SECURITY_PASSWORD='cambia-esto-tambien'
```

Estos valores se cambian únicamente cuando alguien va a crear o actualizar su propio ambiente en AWS. No son necesarios para consumir la API ya desplegada.

## Desplegar

```bash
bash deploy/aws/deploy-elastic-beanstalk.sh
```

Cuando el ambiente esté listo, obtener la URL pública:

```bash
aws elasticbeanstalk describe-environments \
  --application-name bike-rental-api \
  --environment-names bike-rental-api-prod \
  --query 'Environments[0].CNAME' \
  --output text
```

Health check:

```bash
curl "http://<CNAME>/actuator/health"
```

Petición autenticada:

```bash
curl -u "admin:${APP_SECURITY_PASSWORD}" \
  "http://<CNAME>/api/bicicletas/disponibles"
```

## Credenciales en Elastic Beanstalk

La API usa autenticación básica HTTP. En el ambiente desplegado, el usuario y contraseña se guardan como variables de entorno de Elastic Beanstalk.

Si eres quien administra la cuenta AWS, puedes recuperar la contraseña configurada con:

```bash
AWS_PROFILE=bike-rental-deployer AWS_REGION=us-east-1 \
aws elasticbeanstalk describe-configuration-settings \
  --application-name bike-rental-api \
  --environment-name bike-rental-api-prod \
  --query "ConfigurationSettings[0].OptionSettings[?Namespace=='aws:elasticbeanstalk:application:environment' && OptionName=='APP_SECURITY_PASSWORD'].Value | [0]" \
  --output text
```

## Destruir

Para evitar costos inesperados:

```bash
bash deploy/aws/destroy-elastic-beanstalk.sh
```

El script de despliegue configura la política de eliminación de la base de datos acoplada como `Snapshot`. Por eso, al terminar el ambiente, Elastic Beanstalk debería conservar un snapshot final de RDS en vez de eliminar todo silenciosamente.

## Trade-off

Este despliegue usa la opción de RDS acoplado a Elastic Beanstalk para mantener simplicidad. En producción, la base de datos normalmente debería administrarse con ciclo de vida separado, idealmente con Terraform, CloudFormation o CDK, subredes privadas, Secrets Manager y políticas explícitas de backup.
