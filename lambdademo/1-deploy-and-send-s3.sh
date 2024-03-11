#!/bin/bash
S3_BUCKET=testfloatbucket
S3_PREFIX=app
ZIP_FILE=lambda-demo.zip
gradle build -DzipFileName=$ZIP_FILE -i -x test
CODE_URI=s3://$S3_BUCKET/$S3_PREFIX/$ZIP_FILE
aws s3 cp ./build/distributions/$ZIP_FILE $CODE_URI
# sam deploy --template-file template.yaml --stack-name lambda-demo  --capabilities CAPABILITY_IAM
