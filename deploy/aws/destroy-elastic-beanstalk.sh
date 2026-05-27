#!/usr/bin/env bash
set -euo pipefail

: "${AWS_REGION:=us-east-1}"
: "${APP_NAME:=bike-rental-api}"
: "${ENV_NAME:=bike-rental-api-prod}"

echo "Terminating Elastic Beanstalk environment ${ENV_NAME}..."
aws elasticbeanstalk terminate-environment \
  --environment-name "$ENV_NAME" \
  --region "$AWS_REGION"

echo "Elastic Beanstalk will also handle the coupled RDS database according to its DBDeletionPolicy."
echo "Current policy in deploy script: Snapshot."
