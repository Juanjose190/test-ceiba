#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

: "${AWS_REGION:=us-east-1}"
: "${APP_NAME:=bike-rental-api}"
: "${ENV_NAME:=bike-rental-api-prod}"
: "${DB_NAME:=bike_rental}"
: "${DB_USER:=bikeuser}"
: "${DB_INSTANCE_CLASS:=db.t4g.micro}"
: "${DB_ALLOCATED_STORAGE:=20}"
: "${APP_SECURITY_USERNAME:=admin}"
: "${EC2_INSTANCE_PROFILE:=aws-elasticbeanstalk-ec2-role}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "ERROR: DB_PASSWORD is required. Export it before running this script." >&2
  exit 1
fi

if [[ -z "${APP_SECURITY_PASSWORD:-}" ]]; then
  echo "ERROR: APP_SECURITY_PASSWORD is required. Export it before running this script." >&2
  exit 1
fi

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text --region "$AWS_REGION")"
BUCKET="${ARTIFACT_BUCKET:-${APP_NAME}-${ACCOUNT_ID}-${AWS_REGION}-eb-artifacts}"
VERSION_LABEL="${VERSION_LABEL:-$(date +%Y%m%d%H%M%S)-$(git rev-parse --short HEAD 2>/dev/null || echo local)}"
ZIP_FILE="target/${APP_NAME}-${VERSION_LABEL}.zip"
SOLUTION_STACK_NAME="${SOLUTION_STACK_NAME:-}"

echo "Packaging application version ${VERSION_LABEL}..."
mkdir -p target
zip -qr "$ZIP_FILE" Dockerfile pom.xml src .dockerignore

if ! aws s3api head-bucket --bucket "$BUCKET" --region "$AWS_REGION" >/dev/null 2>&1; then
  echo "Creating artifact bucket ${BUCKET}..."
  if [[ "$AWS_REGION" == "us-east-1" ]]; then
    aws s3api create-bucket --bucket "$BUCKET" --region "$AWS_REGION" >/dev/null
  else
    aws s3api create-bucket \
      --bucket "$BUCKET" \
      --region "$AWS_REGION" \
      --create-bucket-configuration "LocationConstraint=${AWS_REGION}" >/dev/null
  fi
fi

aws s3 cp "$ZIP_FILE" "s3://${BUCKET}/${APP_NAME}/${VERSION_LABEL}.zip" --region "$AWS_REGION" >/dev/null

if ! aws elasticbeanstalk describe-applications \
  --application-names "$APP_NAME" \
  --region "$AWS_REGION" \
  --query 'Applications[0].ApplicationName' \
  --output text 2>/dev/null | grep -q "^${APP_NAME}$"; then
  echo "Creating Elastic Beanstalk application ${APP_NAME}..."
  aws elasticbeanstalk create-application \
    --application-name "$APP_NAME" \
    --description "Bike Rental API Spring Boot application" \
    --region "$AWS_REGION" >/dev/null
fi

echo "Creating Elastic Beanstalk application version..."
aws elasticbeanstalk create-application-version \
  --application-name "$APP_NAME" \
  --version-label "$VERSION_LABEL" \
  --source-bundle "S3Bucket=${BUCKET},S3Key=${APP_NAME}/${VERSION_LABEL}.zip" \
  --region "$AWS_REGION" >/dev/null

if [[ -z "$SOLUTION_STACK_NAME" ]]; then
  SOLUTION_STACK_NAME="$(aws elasticbeanstalk list-available-solution-stacks \
    --query "SolutionStacks[?contains(@, 'Amazon Linux 2023')]|[?contains(@, 'running Docker')]|[-1]" \
    --output text \
    --region "$AWS_REGION")"
fi

if [[ "$SOLUTION_STACK_NAME" == "None" || -z "$SOLUTION_STACK_NAME" ]]; then
  echo "ERROR: Could not find an Elastic Beanstalk Docker solution stack for Amazon Linux 2023." >&2
  exit 1
fi

OPTION_SETTINGS_FILE="$(mktemp)"
cat > "$OPTION_SETTINGS_FILE" <<JSON
[
  {
    "Namespace": "aws:elasticbeanstalk:application:environment",
    "OptionName": "SPRING_PROFILES_ACTIVE",
    "Value": "aws"
  },
  {
    "Namespace": "aws:elasticbeanstalk:application:environment",
    "OptionName": "APP_SECURITY_USERNAME",
    "Value": "${APP_SECURITY_USERNAME}"
  },
  {
    "Namespace": "aws:elasticbeanstalk:application:environment",
    "OptionName": "APP_SECURITY_PASSWORD",
    "Value": "${APP_SECURITY_PASSWORD}"
  },
  {
    "Namespace": "aws:elasticbeanstalk:environment:process:default",
    "OptionName": "HealthCheckPath",
    "Value": "/actuator/health"
  },
  {
    "Namespace": "aws:autoscaling:launchconfiguration",
    "OptionName": "IamInstanceProfile",
    "Value": "${EC2_INSTANCE_PROFILE}"
  },
  {
    "Namespace": "aws:rds:dbinstance",
    "OptionName": "HasCoupledDatabase",
    "Value": "true"
  },
  {
    "Namespace": "aws:rds:dbinstance",
    "OptionName": "DBEngine",
    "Value": "postgres"
  },
  {
    "Namespace": "aws:rds:dbinstance",
    "OptionName": "DBInstanceClass",
    "Value": "${DB_INSTANCE_CLASS}"
  },
  {
    "Namespace": "aws:rds:dbinstance",
    "OptionName": "DBAllocatedStorage",
    "Value": "${DB_ALLOCATED_STORAGE}"
  },
  {
    "Namespace": "aws:rds:dbinstance",
    "OptionName": "DBName",
    "Value": "${DB_NAME}"
  },
  {
    "Namespace": "aws:rds:dbinstance",
    "OptionName": "DBUser",
    "Value": "${DB_USER}"
  },
  {
    "Namespace": "aws:rds:dbinstance",
    "OptionName": "DBPassword",
    "Value": "${DB_PASSWORD}"
  },
  {
    "Namespace": "aws:rds:dbinstance",
    "OptionName": "DBDeletionPolicy",
    "Value": "Snapshot"
  }
]
JSON

if aws elasticbeanstalk describe-environments \
  --application-name "$APP_NAME" \
  --environment-names "$ENV_NAME" \
  --include-deleted false \
  --region "$AWS_REGION" \
  --query 'Environments[0].EnvironmentName' \
  --output text 2>/dev/null | grep -q "^${ENV_NAME}$"; then
  echo "Updating environment ${ENV_NAME}..."
  aws elasticbeanstalk update-environment \
    --application-name "$APP_NAME" \
    --environment-name "$ENV_NAME" \
    --version-label "$VERSION_LABEL" \
    --option-settings "file://${OPTION_SETTINGS_FILE}" \
    --region "$AWS_REGION" >/dev/null
else
  echo "Creating environment ${ENV_NAME}..."
  aws elasticbeanstalk create-environment \
    --application-name "$APP_NAME" \
    --environment-name "$ENV_NAME" \
    --version-label "$VERSION_LABEL" \
    --solution-stack-name "$SOLUTION_STACK_NAME" \
    --option-settings "file://${OPTION_SETTINGS_FILE}" \
    --region "$AWS_REGION" >/dev/null
fi

rm -f "$OPTION_SETTINGS_FILE"

echo "Deployment started. Watch status with:"
echo "aws elasticbeanstalk describe-environments --application-name ${APP_NAME} --environment-names ${ENV_NAME} --region ${AWS_REGION}"
