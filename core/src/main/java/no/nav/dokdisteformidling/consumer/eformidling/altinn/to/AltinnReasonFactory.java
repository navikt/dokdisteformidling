package no.nav.dokdisteformidling.consumer.eformidling.altinn.to;

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
        return createAltinnReason(initateAltinnFault.getFaultInfo());
    }

    public static AltinnReason from(IBrokerServiceExternalGetAvailableFilesAltinnFaultFaultFaultMessage availableFilesFault) {
        return createAltinnReason(availableFilesFault.getFaultInfo());
    }

    public static AltinnReason from(IBrokerServiceExternalStreamedUploadFileStreamedAltinnFaultFaultFaultMessage uploadFault) {
        return createAltinnReason(uploadFault.getFaultInfo());
    }

    public static AltinnReason from(IBrokerServiceExternalStreamedDownloadFileStreamedAltinnFaultFaultFaultMessage downloadFault) {
        return createAltinnReason(downloadFault.getFaultInfo());
    }

    public static AltinnReason from(IBrokerServiceExternalConfirmDownloadedAltinnFaultFaultFaultMessage confirmFault) {
        return createAltinnReason(confirmFault.getFaultInfo());
    }

    public static AltinnReason from(IBrokerServiceExternalTestAltinnFaultFaultFaultMessage confirmFault) {
        return createAltinnReason(confirmFault.getFaultInfo());
    }

    private static AltinnReason createAltinnReason(no.altinn.brokerserviceexternal.AltinnFault fault) {
        return new AltinnReason(fault.getErrorID(),
                fault.getAltinnErrorMessage().getValue(),
                fault.getUserId().getValue(),
                fault.getAltinnLocalizedErrorMessage().getValue());
    }

    private static AltinnReason createAltinnReason(no.altinn.brokerserviceexternalstreamed.AltinnFault fault) {
        return new AltinnReason(fault.getErrorID(),
                fault.getAltinnErrorMessage().getValue(),
                fault.getUserId().getValue(),
                fault.getAltinnLocalizedErrorMessage().getValue());
    }

}
