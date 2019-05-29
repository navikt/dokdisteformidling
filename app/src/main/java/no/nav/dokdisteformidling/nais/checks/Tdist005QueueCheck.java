package no.nav.dokdisteformidling.nais.checks;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdisteformidling.nais.selftest.AbstractDependencyCheck;
import no.nav.dokdisteformidling.nais.selftest.ApplicationNotReadyException;
import no.nav.dokdisteformidling.nais.selftest.DependencyType;
import no.nav.dokdisteformidling.nais.selftest.Importance;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;

@Component
public class Tdist005QueueCheck extends AbstractDependencyCheck {

	private final Queue tdist005;
	private final JmsTemplate jmsTemplate;

	@Inject
	public Tdist005QueueCheck(MeterRegistry registry, Queue tdist005, JmsTemplate jmsTemplate) throws JMSException {
		super(DependencyType.QUEUE, "Tdist005Queue", tdist005.getQueueName(), Importance.WARNING, registry);
		this.tdist005 = tdist005;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	protected void doCheck() {
		try {
			checkQueue(tdist005);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("JMS Queue Browser failed to get queue: " + tdist005, e);
		}
	}

	private void checkQueue(final Queue queue) {
		jmsTemplate.browse(queue,
				(session, browser) -> {
					browser.getQueue();
					return null;
				}
		);
	}


}
