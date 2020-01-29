package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.mapper;

import lombok.RequiredArgsConstructor;
import no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to.ServiceCode;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.EformidlingMottakerInfoService;
import no.nav.dokdisteformidling.consumer.eformidling.serviceregistry.MottakerInfo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceCodeMapper {

    private final EformidlingMottakerInfoService mottakerInfoService;

    public ServiceCode getServiceCode() {
        MottakerInfo mottakerInfo = mottakerInfoService.hentMottakerInfoTrygderetten();
        return ServiceCode.builder()
                .externalServiceCode(mottakerInfo.getServiceCode())
                .externalServiceEdictionCode(Integer.valueOf(mottakerInfo.getServiceEditionCode()))
                .build();
    }


}
