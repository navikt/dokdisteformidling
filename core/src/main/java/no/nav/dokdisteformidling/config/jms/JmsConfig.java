
package no.nav.dokdisteformidling.config.jms;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.props.SrvAppserverProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING;
import static com.ibm.msg.client.jms.JmsConstants.USER_AUTHENTICATION_MQCSP;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT;

/**
 * @author Sigurd Midttun, Visma Consulting AS
 */
@Configuration
@Profile({"nais", "local"})
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;

	@Bean
	public Queue qdist013(@Value("${dokdisteformidling_qdist013_dist_trygderetten.queuename}") String qdist013QueueName) throws JMSException {
		return new MQQueue(qdist013QueueName);
	}

	@Bean
	public Queue qdist013FunksjonellFeil(@Value("${dokdisteformidling_qdist013_funk_feil.queuename}") String qdist013FunksjonellFeil) throws JMSException {
		return new MQQueue(qdist013FunksjonellFeil);
	}

	@Bean
	public ConnectionFactory wmqConnectionFactory(final MqGatewayAlias mqGatewayAlias,
												  final @Value("${dokdisteformidling_channel.name}") String channelName,
												  final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		return createConnectionFactory(mqGatewayAlias, channelName, srvAppserverProperties);
	}

	private UserCredentialsConnectionFactoryAdapter createConnectionFactory(final MqGatewayAlias mqGatewayAlias,
																			final String channelName,
																			final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setChannel(channelName);
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
		connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		connectionFactory.setBooleanProperty(USER_AUTHENTICATION_MQCSP, false);
		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);
		adapter.setUsername(srvAppserverProperties.getUsername());
		adapter.setPassword(srvAppserverProperties.getPassword());
		return adapter;
	}
}
