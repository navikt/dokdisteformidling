package no.nav.dokdisteformidling.qdist013.avtaltmelding.v2;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
enum AvtaltFilformat {
	JPEG("jpeg", "jpeg"),
	PNG("png", "png"),
	PDF("pdf", "pdf"),
	TIFF("tiff", "tiff"),
	XLSX("xlsx", "xlsx");

	private final String format;
	private final String filendelse;

	AvtaltFilformat(String format, String filendelse) {
		this.format = format;
		this.filendelse = filendelse;
	}
}
