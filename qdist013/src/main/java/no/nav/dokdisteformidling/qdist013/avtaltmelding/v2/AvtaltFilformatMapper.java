package no.nav.dokdisteformidling.qdist013.avtaltmelding.v2;

import no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013;

import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_ARKIV;
import static no.nav.dokdisteformidling.constants.DomainConstants.VARIANTFORMAT_SLADDET;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.JPEG;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.PDF;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.PNG;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.TIFF;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltFilformat.XLSX;
import static no.nav.dokdisteformidling.qdist013.avtaltmelding.v2.AvtaltmeldingV2Mapper.dokumentInfoContainsSladdetDokumentvariant;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_JPEG;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_PDF;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_PNG;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_TIFF;
import static no.nav.dokdisteformidling.qdist013.saf.main.JournalpostQdist013.DokumentInfo.Dokumentvariant.FILTYPE_XLSX;

final class AvtaltFilformatMapper {

	private AvtaltFilformatMapper() {
		// ingen instansiering
	}

	static AvtaltFilformat map(JournalpostQdist013.DokumentInfo dokumentInfo) {
		String safFiltype = getFiltype(dokumentInfo);

		return switch (safFiltype) {
			case FILTYPE_PDF -> PDF;
			case FILTYPE_JPEG -> JPEG;
			case FILTYPE_PNG -> PNG;
			case FILTYPE_TIFF -> TIFF;
			case FILTYPE_XLSX -> XLSX;
			default ->
					throw new AvtaltmeldingV2MappingException("Klarte ikke mappe format, filtype er ikke støttet. journalpost.dokumenter.dokumentvariant.filtype=" + safFiltype);
		};
	}

	private static String getFiltype(JournalpostQdist013.DokumentInfo dokumentInfo) {
		if (dokumentInfoContainsSladdetDokumentvariant(dokumentInfo)) {
			return dokumentInfo.getDokumentvarianter().stream()
					.filter(dokumentvariant -> VARIANTFORMAT_SLADDET.equals(dokumentvariant.getVariantformat()))
					.findAny()
					.get()
					//Ok, already validated in SafJournalpostValidatorQdist013.
					.getFiltype();
		} else {
			return dokumentInfo.getDokumentvarianter().stream()
					.filter(dokumentvariant -> VARIANTFORMAT_ARKIV.equals(dokumentvariant.getVariantformat()))
					.findAny()
					.get()//Ok, already validated in SafJournalpostValidatorQdist013.
					.getFiltype();
		}
	}
}
