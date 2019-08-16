package no.nav.dokdisteformidling.consumer.integrasjonspunkt;

import lombok.Value;

import java.util.List;

/**
 * Simple wrapper for paged response
 *
 * @author Erik Bråten, Visma Consulting.
 */
@Value
public class Page<T> {
	private List<T> content;
}
