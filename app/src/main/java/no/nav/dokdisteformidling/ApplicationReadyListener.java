package no.nav.dokdisteformidling;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdisteformidling.config.props.FeatureToggleProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logFeatureToggles(applicationReadyEvent);
    }

    private void logFeatureToggles(ApplicationReadyEvent applicationReadyEvent) {
        final FeatureToggleProperties featureToggleProperties = applicationReadyEvent.getApplicationContext().getBean(FeatureToggleProperties.class);
        log.info("{}", featureToggleProperties);
    }
}