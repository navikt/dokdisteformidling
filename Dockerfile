FROM ghcr.io/navikt/baseimages/temurin:21

# Brukes for å hente config fra json filer
USER root
RUN apt-get install -y --no-install-recommends jq

USER apprunner
COPY app/target/app.jar /app/app.jar
COPY export-vault-secrets.sh /init-scripts/10-export-vault-secrets.sh
COPY dokdisteformidling-java-opts.sh /init-scripts/12-dokdisteformidling-java-opts.sh