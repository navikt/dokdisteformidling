FROM navikt/java:8

# Brukes for å hente config fra json filer
RUN apt-get update && apt-get install -y --no-install-recommends jq

COPY app/target/app.jar /app/app.jar
COPY export-vault-secrets.sh /init-scripts/10-export-vault-secrets.sh

ENV JAVA_OPTS="-Xmx1024m \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"