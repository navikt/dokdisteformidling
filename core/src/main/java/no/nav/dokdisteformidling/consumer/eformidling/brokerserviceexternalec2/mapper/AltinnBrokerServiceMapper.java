package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.mapper;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.ServiceCode;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.UploadManifest;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AltinnBrokerServiceMapper {

    private final EformidlingMottakerInfoService mottakerInfoService;
    private static final String FILE_NAME = "sbd.zip";

    public AltinnBrokerServiceMapper(EformidlingMottakerInfoService mottakerInfoService) {
        this.mottakerInfoService = mottakerInfoService;
    }

    public UploadManifest mapUploadManifest(List<String> fileList, String senderReference) {
        MottakerInfo mottakerInfo = mottakerInfoService.hentMottakerInfoTrygderetten();
        return UploadManifest.builder()
                .serviceCode(mottakerInfo.getServiceCode())
                .serviceEditionCode(mottakerInfo.getServiceEditionCode())
                .fileZipName(FILE_NAME)
                .files(fileList)
                .senderReference(senderReference)
                .build();

    }

    public ServiceCode getServiceCode() {
        MottakerInfo mottakerInfo = mottakerInfoService.hentMottakerInfoTrygderetten();
        return ServiceCode.builder()
                .externalServiceCode(mottakerInfo.getServiceCode())
                .externalServiceEdictionCode(Integer.valueOf(mottakerInfo.getServiceEditionCode()))
                .build();
    }


}
