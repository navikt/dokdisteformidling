package no.nav.dokdisteformidling.qdist013.avtaltmelding.v2;

import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.JPEG;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.PDF;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.PNG;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.TIFF;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.XLSX;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_JPEG;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_PDF;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_PNG;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_TIFF;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_XLSX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvtaltFilformatMapperTest {

	@ParameterizedTest
	@MethodSource("provideSafFiltyper")
	void shouldMapAllSafFiltyperWhenArkivVariant(String safFiltype, AvtaltFilformat expectedAvtaltFilformat) {
		AvtaltFilformat avtaltFilformat = AvtaltFilformatMapper.map(JournalpostQdist013.DokumentInfo.builder()
				.dokumentvarianter(List.of(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
						.variantformat("ARKIV")
						.filtype(safFiltype)
						.build()))
				.build());

		assertThat(avtaltFilformat).isEqualTo(expectedAvtaltFilformat);
	}

	@ParameterizedTest
	@MethodSource("provideSafFiltyper")
	void shouldMapAllSafFiltyperWhenSladdetVariant(String safFiltype, AvtaltFilformat expectedAvtaltFilformat) {
		AvtaltFilformat avtaltFilformat = AvtaltFilformatMapper.map(JournalpostQdist013.DokumentInfo.builder()
				.dokumentvarianter(List.of(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
						.variantformat("SLADDET")
						.filtype(safFiltype)
						.build()))
				.build());

		assertThat(avtaltFilformat).isEqualTo(expectedAvtaltFilformat);
	}

	private static Stream<Arguments> provideSafFiltyper() {
		return Stream.of(
				Arguments.of(FILTYPE_JPEG, JPEG),
				Arguments.of(FILTYPE_PNG, PNG),
				Arguments.of(FILTYPE_PDF, PDF),
				Arguments.of(FILTYPE_TIFF, TIFF),
				Arguments.of(FILTYPE_XLSX, XLSX)
		);
	}

	@Test
	void shouldThrowExceptionWhenUnknownFiltype() {
		assertThatThrownBy(() -> AvtaltFilformatMapper.map(JournalpostQdist013.DokumentInfo.builder()
				.dokumentvarianter(List.of(JournalpostQdist013.DokumentInfo.Dokumentvariant.builder()
						.variantformat("ARKIV")
						.filtype("CSV")
						.build()))
				.build())).isInstanceOf(AvtaltmeldingV2MappingException.class);
	}
}