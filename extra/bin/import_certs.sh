#!/bin/bash
# name: import_certs.sh
# description: import certificates from /certs when starting geonetwork
# version: 0.1
# date: 2022-01-12
# author: K.Serier

CERTS_DIR=${JAVA_HOME}/jre/lib/security

if [[ -d "${JAVA_HOME}/jre/lib/security" ]]; then
  CERTS_DIR=${JAVA_HOME}/jre/lib/security
elif [[ -d ""${JAVA_HOME}/lib/security"" ]]; then
    CERTS_DIR=${JAVA_HOME}/lib/security
else 
  printf "\n\tERROR: %s and %s not found!\n\n" "${JAVA_HOME}/jre/lib/security" "${JAVA_HOME}/lib/security"
  exit 1
fi


for CERT in /certs/*.*
do
  [[ -e "$CERT" ]] || break  # handle the case of no *.wav files
  ALIAS=$(echo "$CERT" | cut -d. -f1)
  keytool -import -file "${CERT}" -storepass changeit -keystore "$CERTS_DIR/cacerts" -alias "$ALIAS" -noprompt 
done
