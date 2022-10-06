#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvdokdisteformidling/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /secrets/serviceuser/srvdokdisteformidling/username)
fi
if test -f /secrets/serviceuser/srvdokdisteformidling/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_password=$(cat /secrets/serviceuser/srvdokdisteformidling/password)
fi
if test -f /var/run/secrets/nais.io/vault/dpo.json
then
    echo "Setting dpo_username"
    export dpo_username="$(cat /var/run/secrets/nais.io/vault/dpo.json | jq -r '.username')"
    echo "Setting dpo_password"
    export dpo_password="$(cat /var/run/secrets/nais.io/vault/dpo.json | jq -r '.password')"
fi
if test -f /secrets/virksomhetssertifikat/credentials.json
then
    echo "Setting virksomhetssertifikat_alias"
    export virksomhetssertifikat_alias="$(cat /secrets/virksomhetssertifikat/credentials.json | jq -r '.alias')"
    echo "Setting virksomhetssertifikat_password"
    export virksomhetssertifikat_password="$(cat /secrets/virksomhetssertifikat/credentials.json | jq -r '.password')"
    echo "Setting virksomhetssertifikat_type"
    export virksomhetssertifikat_type="$(cat /secrets/virksomhetssertifikat/credentials.json | jq -r '.type')"
fi
if test -f /secrets/virksomhetssertifikat/key.p12.b64
then
    echo "Setting virksomhetssertifikat_path"
    export virksomhetssertifikat_path="file:///secrets/virksomhetssertifikat/key.p12.b64"
fi

if test -f /var/run/secrets/nais.io/vault/gcloud_serviceaccount
then
    echo "Setting GOOGLE_APPLICATION_CREDENTIALS"
    export GOOGLE_APPLICATION_CREDENTIALS=/var/run/secrets/nais.io/vault/gcloud_serviceaccount
fi

if test -f /var/run/secrets/nais.io/certificate/keystore
then
    echo "Setting DOKDISTEFORMIDLING_KEYSTORE"
    CERT_PATH='/var/run/secrets/nais.io/certificate/keystore-extracted'
    openssl base64 -d -A -in /var/run/secrets/nais.io/certificate/keystore -out $CERT_PATH
    export DOKDISTEFORMIDLING_KEYSTORE=$CERT_PATH
fi

if test -f /var/run/secrets/nais.io/certificate/keystorepassword
then
    echo "Setting DOKDISTEFORMIDLING_KEYSTORE_PASSWORD"
    export DOKDISTEFORMIDLING_KEYSTORE_PASSWORD=$(cat /var/run/secrets/nais.io/certificate/keystorepassword)
fi