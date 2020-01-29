package no.nav.dokdisteformidling;

import no.nav.dokdisteformidling.constants.DomainConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ComponentScan
@Configuration
public class CoreConfig {
	@Bean
	Clock clock() {
		return Clock.system(DomainConstants.DEFAULT_ZONE_ID);
	}
}
