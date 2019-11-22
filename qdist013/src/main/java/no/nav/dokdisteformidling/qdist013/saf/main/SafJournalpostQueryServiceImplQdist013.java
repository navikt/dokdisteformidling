package no.nav.dokdisteformidling.qdist013.saf.main;

import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.consumer.saf.graphql.GraphQLRequest;
import no.nav.dokdisteformidling.consumer.saf.graphql.SafGraphqlConsumer;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author Sigurd Midttun, Visma Consulting.
 * <p>
 * This is the main Saf service from which all information needed about the journalpost being distributed is obtained
 */
@Component("SafJournalpostQueryServiceQdist013")
public class SafJournalpostQueryServiceImplQdist013 implements SafJournalpostQueryService<JournalpostQdist013> {

	private static final String JOURNALPOST_QUERY =
			"query journalpost($queryJournalpostId: String!) {\n" +
					"journalpost(journalpostId: $queryJournalpostId) {\n" +
						"journalpostId\n" +
						"sak {\n" +
							"datoOpprettet\n" +
						"}\n" +
						"opprettetAvNavn\n" +
						"journalposttype\n" +
						"bruker{\n" +
							"id\n" +
							"type\n" +
						"}\n" +
						"datoOpprettet\n" +
						"tittel\n" +
						"journalfortAvNavn\n" +
						"temanavn\n" +
						"journalfoerendeEnhet\n" +
						"relevanteDatoer {\n" +
							"dato\n" +
							"datotype\n" +
						"}\n" +
						"dokumenter {\n" +
							"dokumentInfoId\n" +
							"tittel\n" +
							"originalJournalpostId\n" +
							"dokumentvarianter {\n" +
								"variantformat\n" +
								"filtype\n" +
							"}\n" +
						"}\n" +
					"}\n" +
			"}";

	private final SafGraphqlConsumer safGraphqlConsumer;
	private final SafJournalpostValidatorQdist013 safJournalpostValidatorQdist013;
	private final JournalpostQdist013Mapper journalpostQdist013Mapper;

	public SafJournalpostQueryServiceImplQdist013(SafGraphqlConsumer safGraphqlConsumer,
												  SafJournalpostValidatorQdist013 safJournalpostValidatorQdist011,
												  JournalpostQdist013Mapper journalpostQdist013Mapper) {
		this.safGraphqlConsumer = safGraphqlConsumer;
		this.safJournalpostValidatorQdist013 = safJournalpostValidatorQdist011;
		this.journalpostQdist013Mapper = journalpostQdist013Mapper;
	}

	public JournalpostQdist013 hentJournalpost(String journalpostid) {
		SafJournalpost safJournalpost = safGraphqlConsumer.performQuery(GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(Collections.singletonMap("queryJournalpostId", journalpostid))
				.build());

		safJournalpostValidatorQdist013.validate(safJournalpost, journalpostid);
		return journalpostQdist013Mapper.map(safJournalpost);
	}
}