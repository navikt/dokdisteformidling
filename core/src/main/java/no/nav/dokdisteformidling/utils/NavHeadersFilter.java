package no.nav.dokdisteformidling.utils;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.dokdisteformidling.constants.MdcConstants.CALL_ID;

public class NavHeadersFilter implements ExchangeFilterFunction {
	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

		if(MDC.get(CALL_ID) != null) {
			return next.exchange(ClientRequest.from(request).headers((headers) -> headers.set(CALL_ID, MDC.get(CALL_ID))).build());
		}
		return next.exchange(request);
	}
}