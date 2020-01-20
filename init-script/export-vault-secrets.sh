if test -f /var/run/secrets/nais.io/srvdokdisteformidling;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /serviceuser/data/test/srvdokdisteformidling/username)
fi
if test -f /var/run/secrets/nais.io/srvdokdisteformidling;
then
    echo "Setting serviceuser_password"
    export  serviceuser_***passord=gammelt_passord***)
fi
if test -f /var/run/secrets/nais.io/vault;
then
    echo "Setting sftp_privateKey"
    export  sftp_privateKey=$(cat /kv/preprod/fss/dokdisteformidling/privatekey)
fi
if test -f /var/run/secrets/nais.io/vault;
then
    echo "Setting sftp_privateKeyPassphrase"
    export  sftp_privateKeyPassphrase=$(cat /kv/preprod/fss/dokdisteformidling/privatekey_passphrase)
fi