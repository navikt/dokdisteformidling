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
public class Qdist013QueueCheck extends AbstractDependencyCheck {

	private final Queue qdist013;
	private final JmsTemplate jmsTemplate;

	@Inject
	public Qdist013QueueCheck(MeterRegistry registry, Queue qdist013, JmsTemplate jmsTemplate) throws JMSException {
		super(DependencyType.QUEUE, "Qdist013Queue", qdist013.getQueueName(), Importance.CRITICAL, registry);
		this.qdist013 = qdist013;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	protected void doCheck() {
		try {
			checkQueue(qdist013);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("JMS Queue Browser failed to get queue: " + qdist013, e);
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
