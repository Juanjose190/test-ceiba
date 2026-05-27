# AWS Deployment

This folder contains a pragmatic AWS deployment path for the technical test:

- Elastic Beanstalk running the Dockerized Spring Boot API.
- PostgreSQL managed by RDS through the Elastic Beanstalk environment.
- Spring profile `aws` reads RDS-provided environment variables.
- Actuator health endpoint configured as the environment health check.

## Why This Option

Elastic Beanstalk keeps the deployment understandable for a small API while still using real AWS building blocks: EC2, load balancer/proxy, RDS, S3 artifacts, environment variables and health checks. ECS Fargate + standalone RDS would also be valid, but it adds more networking and IAM surface for a technical test.

## Prerequisites

1. Configure AWS locally. Prefer IAM Identity Center/SSO or an IAM user with limited permissions. Do not use root credentials.
2. Validate the session:

```bash
aws sts get-caller-identity
```

3. Export required secrets:

```bash
export AWS_REGION=us-east-1
export DB_PASSWORD='change-me-with-a-strong-password'
export APP_SECURITY_PASSWORD='change-me-too'
```

## Deploy

```bash
bash deploy/aws/deploy-elastic-beanstalk.sh
```

When the environment is ready, get the public URL:

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

Authenticated request:

```bash
curl -u "admin:${APP_SECURITY_PASSWORD}" "http://<CNAME>/api/bicicletas/disponibles"
```

## Destroy

To avoid unexpected costs:

```bash
bash deploy/aws/destroy-elastic-beanstalk.sh
```

The deployment script configures the coupled RDS deletion policy as `Snapshot`, so terminating the environment should keep a final DB snapshot instead of silently deleting everything.

## Trade-off

This uses Elastic Beanstalk's coupled RDS option for simplicity. For production, the database should usually be managed as a separate lifecycle resource, ideally with Terraform/CloudFormation/CDK, private subnets, secrets management and explicit backup policies.
