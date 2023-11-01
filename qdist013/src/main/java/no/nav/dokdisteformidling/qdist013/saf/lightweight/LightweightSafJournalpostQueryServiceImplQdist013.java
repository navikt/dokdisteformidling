package no.nav.dokdisteformidling.qdist013.saf.lightweight;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.consumer.saf.graphql.GraphQLRequest;
import no.nav.dokdisteformidling.consumer.saf.graphql.SafGraphqlConsumer;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost.RelevantDato;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonMap;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.Datotype.DATO_JOURNALFOERT;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Component("LightweightSafJournalpostQueryServiceQdist013")
public class LightweightSafJournalpostQueryServiceImplQdist013 implements SafJournalpostQueryService<LightweightSafJournalpostQdist013> {

	private static final String JOURNALPOST_QUERY = """
			query journalpost($queryJournalpostId: String!) {
			  journalpost(journalpostId: $queryJournalpostId) {
			    journalfortAvNavn
			    avsenderMottaker {
			      navn
			    }
			    journalposttype
			    relevanteDatoer {
			      dato
			      datotype
			    }
			  }
			}
			""";

	private static final String UKJENT_NAVN = "UKJENT";
	private final SafGraphqlConsumer safGraphqlConsumer;

	public LightweightSafJournalpostQueryServiceImplQdist013(SafGraphqlConsumer safGraphqlConsumer) {
		this.safGraphqlConsumer = safGraphqlConsumer;
	}

	@Cacheable(LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE)
	public LightweightSafJournalpostQdist013 hentJournalpost(String journalpostId) {
		var request = GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(singletonMap("queryJournalpostId", journalpostId))
				.build();
		SafJournalpost safJournalpost = safGraphqlConsumer.performQuery(request);

		if (safJournalpost.getAvsenderMottaker() == null || isEmpty(safJournalpost.getAvsenderMottaker().getNavn())) {
			log.warn("AvsenderMottakerNavn er null eller tom i respons fra SAF på journalpostId={}", journalpostId);
		}

		return LightweightSafJournalpostQdist013.builder()
				.journalfortAvNavn(safJournalpost.getJournalfortAvNavn())
				.journalposttype(safJournalpost.getJournalposttype())
				.avsenderMottakerNavn(getAvsenderMottakerNavn(safJournalpost.getAvsenderMottaker()))
				.datoJournalfoert(getDatoJournalfoert(safJournalpost.getRelevanteDatoer()))
				.build();
	}

	private String getAvsenderMottakerNavn(SafJournalpost.AvsenderMottaker avsenderMottaker) {
		if (avsenderMottaker == null || isEmpty(avsenderMottaker.getNavn())) {
			return UKJENT_NAVN;
		} else {
			return avsenderMottaker.getNavn();
		}
	}

	public LocalDateTime getDatoJournalfoert(List<RelevantDato> relevanteDatoer) {

		return isRelevantDatoNull(relevanteDatoer) ? null : relevanteDatoer.stream()
				.filter(relevantDato -> DATO_JOURNALFOERT.name().equals(relevantDato.getDatotype()))
				.map(RelevantDato::getDato)
				.findAny()
				.orElse(null);
	}

	private boolean isRelevantDatoNull(List<RelevantDato> relevanteDatoer) {
		return relevanteDatoer == null || relevanteDatoer.isEmpty();
	}
}