#!/usr/bin/env sh

echo "starter init-script"
ls -la /secrets/serviceuser

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
    echo $dokdisteformidling_s3_creds_username
    echo "Setting dokdisteformidling_S3_creds_password"
    export  dokdisteformidling_S3_creds_***passord=gammelt_passord***)
    echo "Setting dokdistmellomlager_s3_storage_crypto_password"
    export  dokdistmellomlager_s3_storage_crypto_***passord=gammelt_passord***)
fi
