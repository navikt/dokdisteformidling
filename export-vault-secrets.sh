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

if test -f "$KLAGEINSTANS_VIRKSOMHETSSERTIFIKAT_CREDENTIALS"
then
    echo "Setting virksomhetssertifikat_alias"
    export virksomhetssertifikat_alias="$(cat $KLAGEINSTANS_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.alias')"
    echo "Setting virksomhetssertifikat_password"
    export virksomhetssertifikat_password="$(cat $KLAGEINSTANS_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.password')"
    echo "Setting virksomhetssertifikat_type"
    export virksomhetssertifikat_type="$(cat $KLAGEINSTANS_VIRKSOMHETSSERTIFIKAT_CREDENTIALS | jq -r '.type')"
fi

if test -f "$KLAGEINSTANS_VIRKSOMHETSSERTIFIKAT_KEY"
then
    echo "Setting virksomhetssertifikat_path"
    export virksomhetssertifikat_path="file://$KLAGEINSTANS_VIRKSOMHETSSERTIFIKAT_KEY"
fi

if test -f /var/run/secrets/nais.io/vault/gcloud_serviceaccount
then
    echo "Setting GOOGLE_APPLICATION_CREDENTIALS"
    export GOOGLE_APPLICATION_CREDENTIALS=/var/run/secrets/nais.io/vault/gcloud_serviceaccount
fi
