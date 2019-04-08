
package no.nav.dokdisteformidling.config.jms;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.WMQConstants;
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

/**
 * @author Sigurd Midttun, Visma Consulting AS
 */
@Configuration
@Profile("nais")
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;

	@Bean
	public Queue qdist0XX(@Value("${dokdisteformidling_qdist0XX_dist_s_print.queuename}") String qdist0xxQueueName) throws JMSException { // todo bruk eller kast
		return new MQQueue(qdist0xxQueueName);
	}

	@Bean
	public Queue qdist0XXFunksjonellFeil(@Value("${dokdisteformidling_qdist0XX_funk_feil.queuename}") String qdist0xxFunksjonellFeil) throws JMSException {// todo bruk eller kast
		return new MQQueue(qdist0xxFunksjonellFeil);
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
		connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);
		adapter.setUsername(srvAppserverProperties.getUsername());
		adapter.setPassword(srvAppserverProperties.getPassword());
		return adapter;
	}
}
