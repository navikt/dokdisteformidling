package no.nav.dokdisteformidling.itest.config;


import no.nav.dokdisteformidling.config.alias.ServiceuserAlias;
import no.nav.dokdisteformidling.config.sts.STSConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Profile("itest")
public class STSTestConfig extends STSConfig {

	public STSTestConfig(ServiceuserAlias serviceuserAlias) {
		super(serviceuserAlias);
	}

	@Override
	public void configureSTS(Object port) {

	}

}
