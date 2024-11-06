package no.nav.dokdisteformidling.sdist001.domain;

import java.time.LocalDateTime;

public record EformidlingStatusOppdatering(String konversasjonId, String status, LocalDateTime statusTidspunkt) {
}
