package no.nav.dokdisteformidling.qdist013.saf.main;

import no.nav.dokdisteformidling.consumer.saf.SafJournalpostQueryService;
import no.nav.dokdisteformidling.consumer.saf.graphql.GraphQLRequest;
import no.nav.dokdisteformidling.consumer.saf.graphql.SafGraphqlConsumer;
import no.nav.dokdisteformidling.consumer.saf.journalpost.SafJournalpost;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import static java.util.Collections.singletonMap;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.SAF_JOURNALPOST_QDIST013_CACHE;

@Component("SafJournalpostQueryServiceQdist013")
public class SafJournalpostQueryServiceImplQdist013 implements SafJournalpostQueryService<JournalpostQdist013> {

	private static final String JOURNALPOST_QUERY = """
			query journalpost($queryJournalpostId: String!) {
			  journalpost(journalpostId: $queryJournalpostId) {
			    journalpostId
			    sak {
			      arkivsaksnummer
			      datoOpprettet
			    }
			    opprettetAvNavn
			    journalposttype
			    bruker {
			      id
			      type
			    }
			    datoOpprettet
			    tittel
			    journalfortAvNavn
			    temanavn
			    tema
			    journalfoerendeEnhet
			    relevanteDatoer {
			      dato
			      datotype
			    }
			    dokumenter {
			      dokumentInfoId
			      dokumentstatus
			      tittel
			      originalJournalpostId
			      dokumentvarianter {
			        variantformat
			        filtype
			      }
			    }
			  }
			}
			""";

	private final SafGraphqlConsumer safGraphqlConsumer;
	private final SafJournalpostValidatorQdist013 safJournalpostValidatorQdist013;
	private final JournalpostQdist013Mapper journalpostQdist013Mapper;

	public SafJournalpostQueryServiceImplQdist013(SafGraphqlConsumer safGraphqlConsumer,
												  SafJournalpostValidatorQdist013 safJournalpostValidatorQdist013,
												  JournalpostQdist013Mapper journalpostQdist013Mapper) {
		this.safGraphqlConsumer = safGraphqlConsumer;
		this.safJournalpostValidatorQdist013 = safJournalpostValidatorQdist013;
		this.journalpostQdist013Mapper = journalpostQdist013Mapper;
	}

	@Override
	@Cacheable(SAF_JOURNALPOST_QDIST013_CACHE)
	public JournalpostQdist013 hentJournalpost(String journalpostid) {
		var request = GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(singletonMap("queryJournalpostId", journalpostid))
				.build();

		SafJournalpost safJournalpost = safGraphqlConsumer.performQuery(request);

		safJournalpostValidatorQdist013.validate(safJournalpost, journalpostid);

		return journalpostQdist013Mapper.map(safJournalpost);
	}
}