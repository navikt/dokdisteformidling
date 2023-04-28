package no.nav.dokdisteformidling.consumer.rdist001;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NotNull
public class OppdaterForsendelseRequest {
	private Long forsendelseId;
	private String forsendelseStatus;
	private String konversasjonId;
	private VarselStatusCode varselStatus;
	private String digitalLeverandoeradresse;
	private String digitalPostkasseadresse;
}
