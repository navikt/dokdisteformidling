
package no.nav.dokdisteformidling.config.jms;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import no.nav.dokdisteformidling.config.alias.MqGatewayAlias;
import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import javax.net.ssl.SSLSocketFactory;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_ENCODING;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CM_CLIENT;

@Configuration
@Profile({"nais", "local"})
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;
	private static final String ANY_TLS13_OR_HIGHER = "*TLS13ORHIGHER";

	@Bean
	public Queue qdist013(@Value("${dokdisteformidling_qdist013_dist_trygderetten.queuename}") String qdist013QueueName) throws JMSException {
		return new MQQueue(qdist013QueueName);
	}

	@Bean
	public Queue qdist013FunksjonellFeil(@Value("${dokdisteformidling_qdist013_funk_feil.queuename}") String qdist013FunksjonellFeil) throws JMSException {
		return new MQQueue(qdist013FunksjonellFeil);
	}

	@Bean
	public ConnectionFactory connectionFactory(final MqGatewayAlias mqGatewayAlias,
											   final ServiceuserAlias serviceuserAlias) throws JMSException {
		return createConnectionFactory(mqGatewayAlias, serviceuserAlias);
	}

	private JmsPoolConnectionFactory createConnectionFactory(final MqGatewayAlias mqGatewayAlias,
															 final ServiceuserAlias serviceuserAlias) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
		connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);

		if (mqGatewayAlias.getChannel().isEnabletls()) {
			connectionFactory.setSSLCipherSuite(ANY_TLS13_OR_HIGHER);
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			connectionFactory.setSSLSocketFactory(factory);
			connectionFactory.setChannel(mqGatewayAlias.getChannel().getSecurename());
		} else {
			connectionFactory.setChannel(mqGatewayAlias.getChannel().getName());
		}

		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);
		adapter.setUsername(serviceuserAlias.getUsername());
		adapter.setPassword(serviceuserAlias.getPassword());

		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(adapter);
		pooledFactory.setMaxConnections(10);
		return pooledFactory;
	}
}
