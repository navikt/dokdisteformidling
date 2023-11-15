package no.nav.dokdisteformidling.consumer.eformidling.altinn.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.activation.DataSource;
import java.io.InputStream;
import java.io.OutputStream;

@Data
@AllArgsConstructor(staticName = "of")
public class InputStreamDataSource implements DataSource {

	private InputStream is;

	@Override
	public InputStream getInputStream() {
		return this.is;
	}

	@Override
	public OutputStream getOutputStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType() {
		return "application/octet-stream";
	}

	@Override
	public String getName() {
		return "InputStreamDataSource";
	}

}
