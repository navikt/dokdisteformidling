package no.nav.dokdisteformidling.qdist013.saf;

import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.consumer.saf.graphql.GraphQLRequest;
import no.nav.dokdisteformidling.consumer.saf.graphql.SafGraphqlConsumer;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("SafJournalpostQueryServiceQdist013")
public class SafJournalpostQueryServiceImplQdist013 implements SafJournalpostQueryService {

	private static final String JOURNALPOST_QUERY =
			//TODO Endre spørring til de attributtene vi ønsker!

			"query journalpost($queryJournalpostId: String!) {\n" +
					"  journalpost(journalpostId: $queryJournalpostId) {\n" +
					"    dokumenter {\n" +
					"      dokumentInfoId\n" +
					"      tittel\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
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

		safJournalpostValidatorQdist013.validate(safJournalpost);
		return journalpostQdist013Mapper.map(safJournalpost);
	}
}