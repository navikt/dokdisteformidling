package no.nav.dokdisteformidling.qdist011;

import static no.nav.dokdisteformidling.constants.RouteConstants.QDIST011_SERVICE_ID;
import static no.nav.dokdisteformidling.metrics.MetricLabels.LABEL_ERROR_TYPE;
import static no.nav.dokdisteformidling.metrics.MetricLabels.LABEL_EXCEPTION_NAME;
import static no.nav.dokdisteformidling.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.dokdisteformidling.metrics.MetricLabels.TYPE_FUNCTIONAL_EXCEPTION;
import static no.nav.dokdisteformidling.metrics.MetricLabels.TYPE_TECHNICAL_EXCEPTION;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.dokdisteformidling.exception.functional.AbstractDokdisteformidlingFunctionalException;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.ValidationException;
import org.apache.camel.support.RoutePolicySupport;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class Qdist011MetricsRoutePolicy extends RoutePolicySupport {

	private final MeterRegistry registry;
	private Timer.Sample timer;

	static final String QDIST011_PROCESS_TIMER = "dok_request_latency";
	private static final String QDIST011_PROCESS_TIMER_DESCRIPTION = "prosesseringstid for kall inn til qdist011";
	private static final String QDIST011_EXCEPTION = "request_exception_total";

	@Inject
	public Qdist011MetricsRoutePolicy(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void onExchangeBegin(Route route, Exchange exchange) {
		timer = Timer.start(registry);
	}

	@Override
	public void onExchangeDone(Route route, Exchange exchange) {
		Exception exception = getException(exchange);

		timer.stop(Timer.builder(QDIST011_PROCESS_TIMER)
				.description(QDIST011_PROCESS_TIMER_DESCRIPTION)
				.tags(LABEL_PROCESS, QDIST011_SERVICE_ID)
				.publishPercentileHistogram(true)
				.register(registry));

		if (exception != null) {
			if (isFunctionalException(exception)) {
				registry.counter(QDIST011_EXCEPTION,
						LABEL_ERROR_TYPE, TYPE_FUNCTIONAL_EXCEPTION,
						LABEL_EXCEPTION_NAME, exception.getClass().getSimpleName(),
						LABEL_PROCESS, QDIST011_SERVICE_ID).increment();
			} else {
				registry.counter(QDIST011_EXCEPTION,
						LABEL_ERROR_TYPE, TYPE_TECHNICAL_EXCEPTION,
						LABEL_EXCEPTION_NAME, exception.getClass().getCanonicalName(),
						LABEL_PROCESS, QDIST011_SERVICE_ID).increment();
			}
		}
	}

	private boolean isFunctionalException(Exception e) {
		return (e instanceof AbstractDokdisteformidlingFunctionalException || e instanceof ValidationException);
	}

	private Exception getException(Exchange exchange) {
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		if (exception == null && exchange.getException() instanceof Exception) {
			exception = (Exception) exchange.getException().getCause();
		}
		return exception;
	}
}
