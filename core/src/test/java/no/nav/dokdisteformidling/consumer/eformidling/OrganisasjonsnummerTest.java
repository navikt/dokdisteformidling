package no.nav.dokdisteformidling.consumer.eformidling;

import org.junit.jupiter.api.Test;

import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.asIso6523;
import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.fromIso6523;
import static no.nav.dokdisteformidling.consumer.eformidling.Organisasjonsnummer.isIso6523;
import static org.assertj.core.api.Assertions.assertThat;

class OrganisasjonsnummerTest {

    @Test
    void shouldReturnOrganisasjonsnummerAsIso6523() {
        assertThat(asIso6523("923456789")).isEqualTo("0192:923456789");
    }

    @Test
    void shouldReturnOrganisasjonsnummerAsIso6523WhenInputIsIso6523() {
        assertThat(asIso6523("0192:923456789")).isEqualTo("0192:923456789");
    }

    @Test
    void shouldReturnOrganisasjonsnummerFromIso6523() {
        assertThat(fromIso6523("0192:923456789")).isEqualTo("923456789");
    }

    @Test
    void shouldReturnTrueWhenOrganisasjonsnummerIsIso6523() {
        assertThat(isIso6523("0192:923456789")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrganisasjonsnummerIsNotIso6523() {
        assertThat(isIso6523("923456789")).isFalse();
    }
}