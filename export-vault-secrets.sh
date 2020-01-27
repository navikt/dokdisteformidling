#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvdokdisteformidling/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /secrets/serviceuser/srvdokdisteformidling/username)
fi
if test -f /secrets/serviceuser/srvdokdisteformidling/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_***passord=gammelt_passord***)
fi
if test -f /secrets/privateKey/privateKeyPassphrase;
then
    echo "Setting sftp_privateKeyPassphrase"
    export  sftp_privateKeyPassphrase=$(cat /secrets/privateKey/privateKeyPassphrase)
fi
if test -d /var/run/secrets/nais.io/vault;
then
    echo "Setting sftp_username"
    export  sftp_username=$(cat /var/run/secrets/nais.io/vault/sftp_username)
    echo "Setting dokdisteformidling_s3_creds_username"
    export  dokdisteformidling_s3_creds_username=$(cat /var/run/secrets/nais.io/vault/dokdisteformidling_s3_creds_username)
    echo "Setting dokdisteformidling_S3_creds_password"
    export  dokdisteformidling_s3_creds_***passord=gammelt_passord***)
    echo "Setting dokdistmellomlager_s3_storage_crypto_password"
    export  dokdistmellomlager_s3_storage_crypto_***passord=gammelt_passord***)
    echo "Setting maskinporten_clientid"
    export  maskinporten_clientid=$(cat /var/run/secrets/nais.io/vault/maskinporten_clientid)
fi
if test -f /secrets/dpo/dpo.json
then
    echo "Setting dpo_username"
    export dpo_username="$(cat /secrets/dpo/dpo.json | jq -r '.username')"
    echo "Setting dpo_password"
    export dpo_***passord=gammelt_passord***')"
fi
if test -f /secrets/virksomhetssertifikat/credentials.json
then
    echo "Setting virksomhetssertifikat_alias"
    export virksomhetssertifikat_alias="$(cat credentials.json | jq -r '.alias')"
    echo "Setting virksomhetssertifikat_password"
    export virksomhetssertifikat_***passord=gammelt_passord***')"
    echo "Setting virksomhetssertifikat_type"
    export virksomhetssertifikat_type="$(cat credentials.json | jq -r '.type')"
fi
if test -f /secrets/virksomhetssertifikat/key.p12.b64
then
    echo "Setting virksomhetssertifikat_path"
    export virksomhetssertifikat_path="/secrets/virksomhetssertifikat/key.p12.b64"
fi
