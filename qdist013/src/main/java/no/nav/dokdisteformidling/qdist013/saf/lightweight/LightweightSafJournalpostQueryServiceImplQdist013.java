package no.nav.dokdisteformidling.qdist013.saf.lightweight;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.consumer.saf.graphql.GraphQLRequest;
import no.nav.dokdisteformidling.consumer.saf.graphql.SafGraphqlConsumer;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import no.nav.dokdisteformidling.exception.functional.SafJournalpostValidationException;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author Sigurd Midttun, Visma Consulting.
 * <p>
 * This is a lightweight Saf service only used in arkivmeldingMapper to get "journalfortAvNavn" from SafJournalpostQdist013.dokumenter.originalJournalpostId
 */
@Component("LightweightSafJournalpostQueryServiceQdist013")
public class LightweightSafJournalpostQueryServiceImplQdist013 implements SafJournalpostQueryService<LightweightSafJournalpostQdist013> {

	private static final String JOURNALPOST_QUERY =
			"query journalpost($queryJournalpostId: String!) {\n" +
					"  journalpost(journalpostId: $queryJournalpostId) {\n" +
					"    journalfortAvNavn\n" +
					"  }\n" +
					"}\n";
	private final SafGraphqlConsumer safGraphqlConsumer;

	public LightweightSafJournalpostQueryServiceImplQdist013(SafGraphqlConsumer safGraphqlConsumer) {
		this.safGraphqlConsumer = safGraphqlConsumer;
	}

	public LightweightSafJournalpostQdist013 hentJournalpost(String journalpostid) {
		SafJournalpost safJournalpost = safGraphqlConsumer.performQuery(GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(Collections.singletonMap("queryJournalpostId", journalpostid))
				.build());

		if (isEmpty(safJournalpost.getJournalfortAvNavn())) {
			throw new SafJournalpostValidationException("JournalfoertAvNavn er null eller tom i respons fra SAF");
		}

		return LightweightSafJournalpostQdist013.builder().journalfortAvNavn(safJournalpost.getJournalfortAvNavn()).build();
	}
}