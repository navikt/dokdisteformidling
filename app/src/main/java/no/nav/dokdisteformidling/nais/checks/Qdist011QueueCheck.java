package no.nav.dokdisteformidling.nais.checks;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdisteformidling.nais.selftest.DependencyType;
import no.nav.dokdisteformidling.nais.selftest.AbstractDependencyCheck;
import no.nav.dokdisteformidling.nais.selftest.ApplicationNotReadyException;
import no.nav.dokdisteformidling.nais.selftest.Importance;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;

@Component
public class Qdist011QueueCheck extends AbstractDependencyCheck {

	private final Queue qdist011;
	private final JmsTemplate jmsTemplate;

	@Inject
	public Qdist011QueueCheck(MeterRegistry registry, Queue qdist011, JmsTemplate jmsTemplate) throws JMSException {
		super(DependencyType.QUEUE, "Qdist011Queue", qdist011.getQueueName(), Importance.CRITICAL, registry);
		this.qdist011 = qdist011;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	protected void doCheck() {
		try {
			checkQueue(qdist011);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("JMS Queue Browser failed to get queue: " + qdist011, e);
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
