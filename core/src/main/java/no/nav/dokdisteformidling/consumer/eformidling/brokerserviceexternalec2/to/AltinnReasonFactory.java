package no.nav.dokdisteformidling.consumer.eformidling.brokerserviceexternalec2.to;

import lombok.experimental.UtilityClass;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalConfirmDownloadedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalGetAvailableFilesAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalInitiateBrokerServiceAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternal.IBrokerServiceExternalTestAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedDownloadFileStreamedAltinnFaultFaultFaultMessage;
import no.altinn.brokerserviceexternalstreamed.IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage;

@UtilityClass
public class AltinnReasonFactory {

    public static AltinnReason from(IBrokerServiceExternalInitiateBrokerServiceAltinnFaultFaultFaultMessage initateAltinnFault) {
        String message = initateAltinnFault.getFaultInfo().getAltinnErrorMessage().getValue();
        Integer id = initateAltinnFault.getFaultInfo().getErrorID();
        String userId = initateAltinnFault.getFaultInfo().getUserId().getValue();
        String localized = initateAltinnFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalGetAvailableFilesAltinnFaultFaultFaultMessage availableFilesFault) {
        String message = availableFilesFault.getFaultInfo().getAltinnErrorMessage().getValue();
        Integer id = availableFilesFault.getFaultInfo().getErrorID();
        String userId = availableFilesFault.getFaultInfo().getUserId().getValue();
        String localized = availableFilesFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage uploadFault) {
        String message = uploadFault.getFaultInfo().getAltinnErrorMessage().getValue();
        Integer id = uploadFault.getFaultInfo().getErrorID();
        String userId = uploadFault.getFaultInfo().getUserId().getValue();
        String localized = uploadFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalStreamedDownloadFileStreamedAltinnFaultFaultFaultMessage downloadFault) {
        String message = downloadFault.getFaultInfo().getAltinnErrorMessage().getValue();
        Integer id = downloadFault.getFaultInfo().getErrorID();
        String userId = downloadFault.getFaultInfo().getUserId().getValue();
        String localized = downloadFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalConfirmDownloadedAltinnFaultFaultFaultMessage confirmFault) {
        final String message = confirmFault.getFaultInfo().getAltinnErrorMessage().getValue();
        final Integer id = confirmFault.getFaultInfo().getErrorID();
        final String userId = confirmFault.getFaultInfo().getUserId().getValue();
        final String localized = confirmFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }

    public static AltinnReason from(IBrokerServiceExternalTestAltinnFaultFaultFaultMessage confirmFault) {
        final String message = confirmFault.getFaultInfo().getAltinnErrorMessage().getValue();
        final Integer id = confirmFault.getFaultInfo().getErrorID();
        final String userId = confirmFault.getFaultInfo().getUserId().getValue();
        final String localized = confirmFault.getFaultInfo().getAltinnLocalizedErrorMessage().getValue();
        return new AltinnReason(id, message, userId, localized);
    }
}
