package no.nav.dokdisteformidling.consumer.eformidling.serviceregistry;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Security;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.dokdisteformidling.AppTestUtils.classpathToString;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.AVTALTMELDING_PROCESS;
import static no.nav.dokdisteformidling.consumer.eformidling.EformidlingConstants.TRYGDERETTEN_ORGNUMMER;
import static no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.ServiceIdentifier.DPO;
import static no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.ServiceIdentifier.DPV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EformidlingMottakerInfoServiceTest {

    public static final String DPO_SERVICE_CODE = "4192";
    public static final String DPO_SERVICE_EDITION_CODE = "270815";

    @Mock
    ServiceRegistryConsumer serviceRegistryConsumerMock;
    @InjectMocks
    EformidlingMottakerInfoService eformidlingMottakerInfoService;

    @BeforeAll
    static void setUp() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldHentMottakerInfo() {
        when(serviceRegistryConsumerMock.getIdentifierResource(eq(TRYGDERETTEN_ORGNUMMER), eq(AVTALTMELDING_PROCESS)))
                .thenReturn(baseIdentifierResource()
                        .serviceRecords(singletonList(createServiceRecord(createDpoService())))
                        .build());

        final MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();

        assertThat(mottakerInfo.getOrgnummer()).isEqualTo(TRYGDERETTEN_ORGNUMMER);
        assertThat(mottakerInfo.getServiceCode()).isEqualTo(DPO_SERVICE_CODE);
        assertThat(mottakerInfo.getServiceEditionCode()).isEqualTo(DPO_SERVICE_EDITION_CODE);
        assertThat(mottakerInfo.getPemCertificate()).isEqualTo(classpathToString("secrets/itest.pem"));
        assertThat(mottakerInfo.getX509Certificate()).isNotNull();
    }

    @Test
    void shouldHentDpoMottakerInfoWhenResponseFromServiceRegistryContainsMultipleServiceRecords() {
        when(serviceRegistryConsumerMock.getIdentifierResource(eq(TRYGDERETTEN_ORGNUMMER), eq(AVTALTMELDING_PROCESS)))
                .thenReturn(baseIdentifierResource()
                        .serviceRecords(asList(createServiceRecord(createDpvService()), createServiceRecord(createDpoService())))
                        .build());

        final MottakerInfo mottakerInfo = eformidlingMottakerInfoService.hentMottakerInfoTrygderetten();

        assertThat(mottakerInfo.getOrgnummer()).isEqualTo(TRYGDERETTEN_ORGNUMMER);
        assertThat(mottakerInfo.getServiceCode()).isEqualTo(DPO_SERVICE_CODE);
        assertThat(mottakerInfo.getServiceEditionCode()).isEqualTo(DPO_SERVICE_EDITION_CODE);
        assertThat(mottakerInfo.getPemCertificate()).isEqualTo(classpathToString("secrets/itest.pem"));
        assertThat(mottakerInfo.getX509Certificate()).isNotNull();
    }

    @Test
    void shouldThrowMottakerInfoIkkeFunnetExceptionWhenNoDpoServiceExists() {
        when(serviceRegistryConsumerMock.getIdentifierResource(eq(TRYGDERETTEN_ORGNUMMER), eq(AVTALTMELDING_PROCESS)))
                .thenReturn(baseIdentifierResource()
                        .serviceRecords(singletonList(createServiceRecord(createDpvService())))
                        .build());

        var exception = assertThrows(MottakerInfoIkkeFunnetException.class, () -> eformidlingMottakerInfoService.hentMottakerInfoTrygderetten());
        assertThat(exception.getMessage()).contains("Fant ikke mottakerinfo for organisasjon");
    }

    @Test
    void shouldThrowMottakerInfoIkkeFunnetExceptionWhenNoPemCertificateIsSet() {
        final ServiceRecord serviceRecord = createServiceRecord(createDpoService());
        serviceRecord.setPemCertificate(null);
        when(serviceRegistryConsumerMock.getIdentifierResource(eq(TRYGDERETTEN_ORGNUMMER), eq(AVTALTMELDING_PROCESS)))
                .thenReturn(baseIdentifierResource()
                        .serviceRecords(singletonList(serviceRecord))
                        .build());

        var exception = assertThrows(MottakerInfoIkkeFunnetException.class, () -> eformidlingMottakerInfoService.hentMottakerInfoTrygderetten());
        assertThat(exception.getMessage()).contains("Fant ikke PEM sertifikat.");
    }

    private ServiceRecord createServiceRecord(Service service) {
        return ServiceRecord.builder()
                .organisationNumber(TRYGDERETTEN_ORGNUMMER)
                .pemCertificate(classpathToString("secrets/itest.pem"))
                .service(service)
                .process(AVTALTMELDING_PROCESS)
                .documentTypes(singletonList("urn:no:difi:arkivmelding:xsd::arkivmelding"))
                .build();
    }

    private Service createDpoService() {
        return Service.builder()
                .identifier(DPO)
                .endpointUrl("https://www.altinn.no")
                .serviceCode(DPO_SERVICE_CODE)
                .serviceEditionCode(DPO_SERVICE_EDITION_CODE)
                .securityLevel(4)
                .build();
    }

    private Service createDpvService() {
        return Service.builder()
                .identifier(DPV)
                .endpointUrl("https://www.altinn.no")
                .serviceCode("1337")
                .serviceEditionCode("42")
                .securityLevel(4)
                .build();
    }

    private IdentifierResource.IdentifierResourceBuilder baseIdentifierResource() {
        return IdentifierResource.builder()
                .infoRecord(InfoRecord.builder()
                        .organizationName("Trygderetten")
                        .identifier(TRYGDERETTEN_ORGNUMMER)
                        .build());
    }
}