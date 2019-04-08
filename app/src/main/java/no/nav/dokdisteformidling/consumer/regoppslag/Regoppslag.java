package no.nav.dokdisteformidling.consumer.regoppslag;


import no.nav.dokdisteformidling.consumer.regoppslag.to.AdresseTo;
import no.nav.dokdisteformidling.consumer.regoppslag.to.HentAdresseRequestTo;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public interface Regoppslag {

	AdresseTo treg002HentAdresse(HentAdresseRequestTo request);
}