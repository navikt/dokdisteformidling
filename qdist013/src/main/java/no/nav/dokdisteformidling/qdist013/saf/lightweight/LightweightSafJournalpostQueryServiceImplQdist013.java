package no.nav.dokdisteformidling.qdist013.saf.lightweight;

import static java.lang.String.format;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.Datotype.DATO_JOURNALFOERT;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.consumer.saf.graphql.GraphQLRequest;
import no.nav.dokdisteformidling.consumer.saf.graphql.SafGraphqlConsumer;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 * <p>
 * This is a lightweight Saf service only used in arkivmeldingMapper to get "journalfortAvNavn" from SafJournalpostQdist013.dokumenter.originalJournalpostId
 */
@Slf4j
@Component("LightweightSafJournalpostQueryServiceQdist013")
public class LightweightSafJournalpostQueryServiceImplQdist013 implements SafJournalpostQueryService<LightweightSafJournalpostQdist013> {

	private static final String JOURNALPOST_QUERY =
			"query journalpost($queryJournalpostId: String!) {\n" +
					"  journalpost(journalpostId: $queryJournalpostId) {\n" +
						"    journalfortAvNavn\n" +
						"    avsenderMottaker{\n" +
						"		navn\n" +
						"	 }\n" +
						"	 relevanteDatoer {\n" +
						"		dato\n" +
						"		datotype\n" +
						"	 }\n" +
					"  }\n" +
					"}\n";

	private static final String UKJENT_NAVN = "UKJENT";
	private final SafGraphqlConsumer safGraphqlConsumer;

	public LightweightSafJournalpostQueryServiceImplQdist013(SafGraphqlConsumer safGraphqlConsumer) {
		this.safGraphqlConsumer = safGraphqlConsumer;
	}

	@Cacheable(LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE)
	public LightweightSafJournalpostQdist013 hentJournalpost(String journalpostId) {
		SafJournalpost safJournalpost = safGraphqlConsumer.performQuery(GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(Collections.singletonMap("queryJournalpostId", journalpostId))
				.build());

		if (safJournalpost.getAvsenderMottaker() == null || isEmpty(safJournalpost.getAvsenderMottaker().getNavn())) {
			log.warn("AvsenderMottakerNavn er null eller tom i respons fra SAF på journalpostId={}", journalpostId);
		}

		return LightweightSafJournalpostQdist013.builder()
				.journalfortAvNavn(safJournalpost.getJournalfortAvNavn())
				.avsenderMottakerNavn(getAvsenderMottakerNavn(safJournalpost.getAvsenderMottaker()))
				.datoJournalfoert(getDatoJournalfoert(safJournalpost.getRelevanteDatoer(), journalpostId))
				.build();
	}

	private String getAvsenderMottakerNavn(SafJournalpost.AvsenderMottaker avsenderMottaker) {
		if (avsenderMottaker == null || isEmpty(avsenderMottaker.getNavn())) {
			return UKJENT_NAVN;
		} else {
			return avsenderMottaker.getNavn();
		}
	}

	public LocalDateTime getDatoJournalfoert(List<SafJournalpost.RelevantDato> relevanteDatoer, String journalpostId) {
		if (relevanteDatoer == null) {
			throw new SafJournalpostValidationException(format("RelevanteDatoer er null i respons fra SAF på journalpostId=%s", journalpostId));
		}

		return relevanteDatoer.stream()
				.filter(relevantDato -> DATO_JOURNALFOERT.name().equals(relevantDato.getDatotype()))
				.map(SafJournalpost.RelevantDato::getDato)
				.findAny()
				.orElseThrow(() -> new SafJournalpostValidationException(format("Kan ikke finne dato journalført i respons fra SAF på journalpostId=%s", journalpostId)));
	}
}