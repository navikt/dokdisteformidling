package no.nav.dokdisteformidling.itest.config;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Profile("itest")
public class JmsItestConfig {

	@Bean
	public Queue qdist011(@Value("${dokdisteformidling_qdist011_dist_til_dpi.queuename}") String qdist011QueueName) {
		return new ActiveMQQueue(qdist011QueueName);
	}

	@Bean
	public Queue qdist011FunksjonellFeil(@Value("${dokdisteformidling_qdist011_funk_feil.queuename}") String qdist011FunksjonellFeil) {
		return new ActiveMQQueue(qdist011FunksjonellFeil);
	}

	@Bean
	public Queue qdist013(@Value("${dokdisteformidling_qdist013_dist_trygderetten.queuename}") String qdist013QueueName) {
		return new ActiveMQQueue(qdist013QueueName);
	}

	@Bean
	public Queue qdist013FunksjonellFeil(@Value("${dokdisteformidling_qdist013_funk_feil.queuename}") String qdist013FunksjonellFeil) {
		return new ActiveMQQueue(qdist013FunksjonellFeil);
	}

	@Bean
	public Queue tdist005(@Value("${dokdistSdpBatchIntern.queuename}") String tdist005QueueName) {
		return new ActiveMQQueue(tdist005QueueName);
	}

	@Bean
	public Queue backoutQueue() {
		return new ActiveMQQueue("ActiveMQ.DLQ");
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public BrokerService broker() {
		BrokerService service = new BrokerService();
		service.setPersistent(false);
		return service;
	}

	@Bean
	public ConnectionFactory activemqConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?create=false");
		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		redeliveryPolicy.setMaximumRedeliveries(0);
		activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
		return activeMQConnectionFactory;
	}
}

