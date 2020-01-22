#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvdokdisteformidling/username;
then
    echo "Setting serviceuser.username"
    export  serviceuser.username=$(cat /secrets/serviceuser/srvdokdisteformidling/username)
fi
if test -f /secrets/serviceuser/srvdokdisteformidling/password;
then
    echo "Setting serviceuser.password"
    export  serviceuser.***passord=gammelt_passord***)
fi
if test -f /secrets/privateKey/privateKeyFile;
then
    echo "Setting sftp.privateKey"
    export  sftp.privateKey=$(cat /secrets/privateKey/privateKeyFile)
fi
if test -f /secrets/privateKey/privateKeyPassphrase;
then
    echo "Setting sftp.privateKeyPassphrase"
    export  sftp.privateKeyPassphrase=$(cat /secrets/privateKey/privateKeyPassphrase)
fi