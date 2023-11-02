package no.nav.dokdisteformidling.sdist001.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class EformidlingStatusOppdatering {

	String konversasjonId;
	String status;
	LocalDateTime statusTidspunkt;
}
