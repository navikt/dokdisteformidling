package no.nav.dokdisteformidling.consumer.eformidling.altinn.services;

import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternalstreamed.*;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.*;
import no.nav.dokdisteformidling.exception.technical.AltinnBrokerServiceWsException;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.ALTINN_BROKERSERVICE_NAMESPACE;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.altinn.to.AltinnReasonFactory.from;

@Slf4j
@Component
public class BrokerServiceExternalStreamedService {
	private static final String ALTINN_OPPLASTING_FEILET = "Altinn opplasting feilet: {}";
	private static final String ALTINN_NEDLASTING_FEILET = "Altinn nedlasting feilet: {}";

	private final IBrokerServiceExternalStreamed brokerServiceExternalStreamed;
	private final ObjectFactory objectFactory;

	@Inject
	public BrokerServiceExternalStreamedService(final IBrokerServiceExternalStreamed brokerServiceExternalStreamed) {
		this.brokerServiceExternalStreamed = brokerServiceExternalStreamed;
		this.objectFactory = new ObjectFactory();
	}

	public ReceiptTo uploadFileToAltinn(String fileReference, String fileName, DataHandler dataHandler) {
		List<Header> headerList = new ArrayList<>();
		Header reportee = null;
		Header reference = null;
		Header filename = null;
		try {
			reportee = new Header(new QName(ALTINN_BROKERSERVICE_NAMESPACE, "Reportee"), NAV_ORGNUMMER, new JAXBDataBinding(String.class));
			reference = new Header(new QName(ALTINN_BROKERSERVICE_NAMESPACE, "Reference"), fileReference, new JAXBDataBinding(String.class));
			filename = new Header(new QName(ALTINN_BROKERSERVICE_NAMESPACE, "FileName"), fileName, new JAXBDataBinding(String.class));
		} catch (JAXBException e) {
			log.error("Feil i uploadFileToAltinn:", e);
		}
		headerList.add(reportee);
		headerList.add(reference);
		headerList.add(filename);

		((BindingProvider) brokerServiceExternalStreamed).getRequestContext().put(Header.HEADER_LIST, headerList);
		StreamedPayloadExternalBE streamedPayloadExternalBE = objectFactory.createStreamedPayloadExternalBE();
		streamedPayloadExternalBE.setDataStream(dataHandler);
		try {
			final ReceiptExternalStreamedBE receiptExternalStreamedBE = brokerServiceExternalStreamed.uploadFileStreamed(streamedPayloadExternalBE);
			return mapReceipt(receiptExternalStreamedBE);
		} catch (IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage e) {
			log.error(ALTINN_OPPLASTING_FEILET, from(e));
			throw new AltinnBrokerServiceWsException(ALTINN_OPPLASTING_FEILET, AltinnReasonFactory.from(e), e);
		}
	}

	private ReceiptTo mapReceipt(ReceiptExternalStreamedBE receiptExternalStreamedBE) {
		return ReceiptTo.builder()
				.lastChanged(receiptExternalStreamedBE.getLastChanged().getValue())
				.parentReceiptId(receiptExternalStreamedBE.getParentReceiptId())
				.receiptHistory(receiptExternalStreamedBE.getReceiptHistory().getValue())
				.receiptId(receiptExternalStreamedBE.getReceiptId())
				.receiptStatusCode(receiptExternalStreamedBE.getReceiptStatusCode().getValue())
				.receiptText(receiptExternalStreamedBE.getReceiptText().getValue())
				.receiptTypeName(receiptExternalStreamedBE.getReceiptTypeName().getValue())
				.build();
	}

	public DownloadResponse downloadFilesFromAltinn(List<FileReference> availableFiles) {
		List<DownloadedFileFromAltinn> result = availableFiles.stream()
				.map(fileReference -> mapResult(fileReference, downloadFile(fileReference.getFileReference())))
				.collect(toList());
		return DownloadResponse.builder().downloadedFileFromAltinn(result).build();
	}

	private DownloadedFileFromAltinn mapResult(FileReference fileReference, DataHandler dataHandler) {
		return DownloadedFileFromAltinn.builder().fileReference(fileReference).dataHandler(dataHandler).build();
	}

	public DataHandler downloadFile(String fileReference) {
		log.info("Laster ned fil med referansenummer: " + fileReference);
		try {
			return brokerServiceExternalStreamed.downloadFileStreamed(fileReference, NAV_ORGNUMMER);// reportee = NAV_ORGNUMMER
		} catch (IBrokerServiceExternalStreamedDownloadFileStreamedAltinnFaultFaultFaultMessage e) {
			log.error(ALTINN_NEDLASTING_FEILET, from(e));
			throw new AltinnBrokerServiceWsException(ALTINN_NEDLASTING_FEILET, AltinnReasonFactory.from(e), e);
		}
	}
}
