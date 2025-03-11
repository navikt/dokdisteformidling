Avtaltmelding mellom Trygderetten og Nav
============
## [avtaltmelding_nav_trygderetten_2.0.xsd](avtaltmelding_nav_trygderetten_2.0.xsd)

* Avtaltmelding utarbeidet av Nav 06.03.2025 basert på versjon 1.0
* Variant av Digdir sin arkivmelding brukt av [eFormidling](https://docs.digdir.no/docs/eFormidling/Utvikling/Dokumenttyper/standard_arkivmelding)

## [metadatakatalog.xsd](metadatakatalog.xsd)

* [NOARK5 v5.0 metadatakatalog](http://schema.arkivverket.no/N5/v5.0/metadatakatalog.xsd) - Brukes av `avtaltmelding_nav_trygderetten_2.0.xsd`

## [nav_virksomhet_metadata_1.0.xsd](nav_virksomhet_metadata_1.0.xsd)

* Nav virksomhetsspesifikke metadata
* `navMappe` inneholder `virksomhetsspesifikkeMetadata` for `mappe`

## [bindings.xml](bindings.xml)

* Tilpasninger i JAXB-genererte typer (Java)

## Endringslogg

### 2.0

* La til `xs:schema` `version` attributt
* La til `nav_virksomhet_metadata_1.0.xsd` som vedlegg. Inneholder typen `navMappe` for å sende over Nav arkivsaksnummer (`saksnummer`) til Trygderetten
* Fjernet `DNummerType` typen da denne aldri ble brukt
* Fjernet `DNummer` element fra `part` og `korrespondansepart` da dette aldri ble brukt