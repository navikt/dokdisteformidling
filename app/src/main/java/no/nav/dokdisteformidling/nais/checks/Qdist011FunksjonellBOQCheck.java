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
public class Qdist011FunksjonellBOQCheck extends AbstractDependencyCheck {

	private final Queue qdist011FunksjonellFeil;
	private final JmsTemplate jmsTemplate;

	@Inject
	public Qdist011FunksjonellBOQCheck(MeterRegistry registry, Queue qdist011FunksjonellFeil, JmsTemplate jmsTemplate) throws JMSException {
		super(DependencyType.QUEUE, "qdist011FunksjonellFeilQueue", qdist011FunksjonellFeil.getQueueName(), Importance.CRITICAL, registry);
		this.qdist011FunksjonellFeil = qdist011FunksjonellFeil;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	protected void doCheck() {
		try {
			checkQueue(qdist011FunksjonellFeil);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("JMS Queue Browser failed to get queue: " + qdist011FunksjonellFeil, e);
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
