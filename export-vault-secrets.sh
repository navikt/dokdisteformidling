#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvdokdisteformidling/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /var/run/secrets/nais.io/srvdokdisteformidling/username)
fi
if test -f /var/run/secrets/nais.io/srvdokdisteformidling;
then
    echo "Setting serviceuser_password"
    export  serviceuser_***passord=gammelt_passord***)
fi
if test -f /secrets/privateKey/privateKeyFile;
then
    echo "Setting sftp_privateKey"
    export  sftp_privateKey=$(cat /secrets/privateKey/privateKeyFile)
fi
if test -f /secrets/privateKey/privateKeyPassphrase;
then
    echo "Setting sftp_privateKeyPassphrase"
    export  sftp_privateKeyPassphrase=$(cat /secrets/privateKey/privateKeyPassphrase)
fi