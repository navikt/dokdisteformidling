dokdisteformidling
================

## Funksjonalitet

dokdisteformidling tilbyr distribusjon av forsendelser gjennom DigDir [eFormidling](https://www.digdir.no/digitale-felleslosninger/eformidling/782).

Den tilbyr kun DPO (Digital post til offentlig virksomhet) for mottakere:

* Trygderetten

## API

JMS grensesnitt:

* [QDIST013 - Distribuer forsendelse til Trygderetten](https://confluence.adeo.no/display/BOA/QDIST013+-+DistribuerForsendelseTilTrygderetten?src=contextnavpagetreemode)

Periodisk jobb:

* [SDIST001 - Oppdater eFormidling status](https://confluence.adeo.no/display/BOA/SDIST001+-+oppdaterEformidlingStatus?src=contextnavpagetreemode)

## Utvikling

### Forutsetninger

* JDK 11
* Maven 3

### Bygging

Kjøre tester:

```
mvn clean verify
```

Kjøre tester og bygge `app.jar`:

```
mvn clean package
```

## Deploy

Bygges og deployes gjennom [dok-jenkins pipeline](https://dok-jenkins.adeo.no/view/Dokumentdistribusjon/job/dokdisteformidling/).

## Support

Support for tjenester på denne appen kan rettes til Team Dokumentløsninger på slack:

* [\#team_dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)