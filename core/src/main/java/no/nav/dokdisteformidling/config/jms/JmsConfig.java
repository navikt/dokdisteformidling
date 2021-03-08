
package no.nav.dokdisteformidling.config.jms;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.wmq.WMQConstants;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Sigurd Midttun, Visma Consulting AS
 */
@Configuration
@Profile({"nais", "local"})
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;

	@Bean
	public Queue qdist011(@Value("${dokdisteformidling_qdist011_dist_til_dpi.queuename}") String qdist011QueueName) throws JMSException {
		return new MQQueue(qdist011QueueName);
	}

	@Bean
	public Queue qdist013(@Value("${dokdisteformidling_qdist013_dist_trygderetten.queuename}") String qdist013QueueName) throws JMSException {
		return new MQQueue(qdist013QueueName);
	}

	@Bean
	public Queue qdist011FunksjonellFeil(@Value("${dokdisteformidling_qdist011_funk_feil.queuename}") String qdist011FunksjonellFeil) throws JMSException {
		return new MQQueue(qdist011FunksjonellFeil);
	}

	@Bean
	public Queue qdist013FunksjonellFeil(@Value("${dokdisteformidling_qdist013_funk_feil.queuename}") String qdist013FunksjonellFeil) throws JMSException {
		return new MQQueue(qdist013FunksjonellFeil);
	}

	@Bean
	public Queue tdist005(@Value("${dokdistSdpBatchIntern.queuename}") String tdist005QueueName) throws JMSException {
		return new MQQueue(tdist005QueueName);
	}

	@Bean
	public ConnectionFactory wmqConnectionFactory(final MqGatewayAlias mqGatewayAlias,
												  final @Value("${dokdisteformidling_channel.name}") String channelName,
												  final ServiceuserAlias serviceuserAlias) throws JMSException {
		return createConnectionFactory(mqGatewayAlias, channelName, serviceuserAlias);
	}

	private UserCredentialsConnectionFactoryAdapter createConnectionFactory(final MqGatewayAlias mqGatewayAlias,
																			final String channelName,
																			final ServiceuserAlias serviceuserAlias) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setChannel(channelName);
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);
		if(mqGatewayAlias.isTlsbroker()) {
			adapter.setUsername(serviceuserAlias.getUsername());
			adapter.setPassword(serviceuserAlias.getPassword());
		} else {
			connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, false);
			adapter.setUsername(serviceuserAlias.getUsername());
			adapter.setPassword(serviceuserAlias.getPassword());
		}

		return adapter;
	}
}
