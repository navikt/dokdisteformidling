package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;

import lombok.experimental.UtilityClass;
import no.altinn.brokerserviceexternaec2.IBrokerServiceExternalEC2ConfirmDownloadedECAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternaec2.IBrokerServiceExternalEC2GetAvailableFilesECAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternaec2.IBrokerServiceExternalEC2InitiateBrokerServiceECAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalec2streamed.IBrokerServiceExternalEC2StreamedDownloadFileStreamedECAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalec2streamed.IBrokerServiceExternalEC2StreamedUploadFileStreamedECAltinnFaultFaultFaultMessage;

@UtilityClass
public class AltinnResonFactory {

    public static AltinnReason from(IBrokerServiceExternalEC2InitiateBrokerServiceECAltinnFaultFaultFaultMessage initateAltinnFault) {
        String message = initateAltinnFault.getFaultInfo().getAltinnErrorMessage().getValue();
        Integer id = initateAltinnFault.getFaultInfo().getErrorID();
        String userId = initateAltinnFault.getFaultInfo().getUserId().getValue();
        String localized = initateAltinnFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalEC2GetAvailableFilesECAltinnFaultFaultFaultMessage availableFilesFault) {
        String message = availableFilesFault.getFaultInfo().getAltinnErrorMessage().getValue();
        Integer id = availableFilesFault.getFaultInfo().getErrorID();
        String userId = availableFilesFault.getFaultInfo().getUserId().getValue();
        String localized = availableFilesFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalEC2StreamedUploadFileStreamedECAltinnFaultFaultFaultMessage uploadFault) {
        String message = uploadFault.getFaultInfo().getAltinnErrorMessage().getValue();
        Integer id = uploadFault.getFaultInfo().getErrorID();
        String userId = uploadFault.getFaultInfo().getUserId().getValue();
        String localized = uploadFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalEC2StreamedDownloadFileStreamedECAltinnFaultFaultFaultMessage downloadFault) {
        String message = downloadFault.getFaultInfo().getAltinnErrorMessage().getValue();
        Integer id = downloadFault.getFaultInfo().getErrorID();
        String userId = downloadFault.getFaultInfo().getUserId().getValue();
        String localized = downloadFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalEC2ConfirmDownloadedECAltinnFaultFaultFaultMessage confirmFault) {

        final String message = confirmFault.getFaultInfo().getAltinnErrorMessage().getValue();
        final Integer id = confirmFault.getFaultInfo().getErrorID();
        final String userId = confirmFault.getFaultInfo().getUserId().getValue();
        final String localized = confirmFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }



}
