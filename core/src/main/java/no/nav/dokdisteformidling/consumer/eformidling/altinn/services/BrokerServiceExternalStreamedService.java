package no.nav.dokdisteformidling.consumer.eformidling.altinn.services;

import jakarta.activation.DataHandler;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.ws.BindingProvider;
import lombok.extern.slf4j.Slf4j;
import no.altinn.brokerserviceexternalstreamed.BrokerServiceExternalStreamedSF;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamed;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedDownloadFileStreamedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalstreamed.ObjectFactory;
import no.altinn.brokerserviceexternalstreamed.ReceiptExternalStreamedBE;
import no.altinn.brokerserviceexternalstreamed.StreamedPayloadExternalBE;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.from.DownloadedMessageFromAltinn;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.AltinnReasonFactory;
import no.nav.dokdisteformidling.consumer.eformidling.altinn.to.ReceiptTo;
import no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.exceptions.DokumentpakkingException;
import no.nav.dokdisteformidling.exception.technical.AltinnBrokerServiceWsException;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.NAV_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.altinn.to.AltinnReasonFactory.from;
import static org.apache.cxf.headers.Header.HEADER_LIST;

@Slf4j
@Component
public class BrokerServiceExternalStreamedService {

	private static final String ALTINN_OPPLASTING_FEILET = "Altinn opplasting feilet: {}";
	private static final String ALTINN_NEDLASTING_FEILET = "Altinn nedlasting feilet: {}";
	private static final String ALTINN_AVLESING_AV_MELDING_FEILET = "Altinn avlesning av melding feilet: ";
	private static final String ALTINN_BROKERSERVICEEXTERNALSTREAMED_NAMESPACE = BrokerServiceExternalStreamedSF.SERVICE.getNamespaceURI();

	private final IBrokerServiceExternalStreamed brokerServiceExternalStreamed;
	private final ObjectFactory objectFactory;

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
			reportee = new Header(new QName(ALTINN_BROKERSERVICEEXTERNALSTREAMED_NAMESPACE, "Reportee"), NAV_ORGNUMMER, new JAXBDataBinding(String.class));
			reference = new Header(new QName(ALTINN_BROKERSERVICEEXTERNALSTREAMED_NAMESPACE, "Reference"), fileReference, new JAXBDataBinding(String.class));
			filename = new Header(new QName(ALTINN_BROKERSERVICEEXTERNALSTREAMED_NAMESPACE, "FileName"), fileName, new JAXBDataBinding(String.class));
		} catch (JAXBException e) {
			log.error("Feil i uploadFileToAltinn:", e);
		}

		headerList.add(reportee);
		headerList.add(reference);
		headerList.add(filename);

		((BindingProvider) brokerServiceExternalStreamed).getRequestContext().put(HEADER_LIST, headerList);
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

	public List<DownloadedMessageFromAltinn> downloadFilesFromAltinn(List<String> filreferanser) {
		return filreferanser.stream()
				.map(filreferanse -> mapReferenceToDownloadedFile(filreferanse, downloadFile(filreferanse)))
				.collect(toList());
	}

	private DownloadedMessageFromAltinn mapReferenceToDownloadedFile(String filreferanse, DataHandler dataHandler) {
		InputStream inputStream;

		try {
			inputStream = dataHandler.getInputStream();
		} catch (IOException | IllegalStateException e) {
			log.error(ALTINN_AVLESING_AV_MELDING_FEILET, e);
			throw new DokumentpakkingException(ALTINN_AVLESING_AV_MELDING_FEILET, e);
		}

		return DownloadedMessageFromAltinn.builder().filreferanse(filreferanse).inputStream(inputStream).build();
	}

	public DataHandler downloadFile(String filreferanse) {

		log.info("Laster ned fil fra Altinn med filreferanse={}", filreferanse);

		try {
			final DataHandler dataHandler = brokerServiceExternalStreamed.downloadFileStreamed(filreferanse, NAV_ORGNUMMER);
			log.info("Lastet ned fil fra Altinn med filreferanse={}", filreferanse);
			return dataHandler;
		} catch (IBrokerServiceExternalStreamedDownloadFileStreamedAltinnFaultFaultFaultMessage e) {
			log.error(ALTINN_NEDLASTING_FEILET, from(e));
			throw new AltinnBrokerServiceWsException(ALTINN_NEDLASTING_FEILET, AltinnReasonFactory.from(e), e);
		}
	}
}
