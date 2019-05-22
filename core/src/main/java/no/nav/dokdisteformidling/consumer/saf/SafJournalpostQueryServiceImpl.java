package no.nav.dokdisteformidling.consumer.saf;


import no.nav.dokdisteformidling.consumer.saf.graphql.GraphQLRequest;
import no.nav.dokdisteformidling.consumer.saf.graphql.JournalpostToMapper;
import no.nav.dokdisteformidling.consumer.saf.graphql.JournalpostToValidator;
import no.nav.dokdisteformidling.consumer.saf.graphql.SafGraphqlConsumer;
import no.nav.dokdisteformidling.consumer.saf.journalpost.Journalpost;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class SafJournalpostQueryServiceImpl implements SafJournalpostQueryService {

	private static final String JOURNALPOST_QUERY =
			"query journalpost($queryJournalpostId: String!) {\n" +
					"  journalpost(journalpostId: $queryJournalpostId) {\n" +
					"    dokumenter {\n" +
					"      dokumentInfoId\n" +
					"      tittel\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	private final SafGraphqlConsumer safGraphqlConsumer;
	private final JournalpostToMapper journalpostMapper = new JournalpostToMapper();
	private final JournalpostToValidator journalpostToValidator = new JournalpostToValidator();

	public SafJournalpostQueryServiceImpl(SafGraphqlConsumer safGraphqlConsumer) {
		this.safGraphqlConsumer = safGraphqlConsumer;
	}

	public Journalpost hentJournalpost(String journalpostid) {

		return journalpostMapper.map(
				journalpostToValidator.validateAndReturn(
						safGraphqlConsumer.performQuery(GraphQLRequest.builder()
								.query(JOURNALPOST_QUERY)
								.operationName("journalpost")
								.variables(Collections.singletonMap("queryJournalpostId", journalpostid))
								.build())
			)
		);
	}
}