#!/usr/bin/env sh

JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.keyStore=${DOKDISTEFORMIDLING_KEYSTORE}"
JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.keyStoreType=jks"
JAVA_OPTS="${JAVA_OPTS} -Xmx6144m -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=nais"

export JAVA_OPTS
