package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAESOAEPparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

/**
 * Endret og tilpasset for NAV sin bruk fra https://github.com/difi/move-integrasjonspunkt
 *
 * Krypterer innhold til CMS (Cryptographic Message Syntax).
 *
 * https://difi.github.io/felleslosninger/eformidling_nm_sikkerhet.html
 */
class CmsUtil {
	private final ASN1ObjectIdentifier cmsEncryptionAlgorithm;
	private final AlgorithmIdentifier keyEncryptionScheme;

	CmsUtil() {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		keyEncryptionScheme = rsaesOaepIdentifier();
		cmsEncryptionAlgorithm = CMSAlgorithm.AES256_CBC;
	}

	private AlgorithmIdentifier rsaesOaepIdentifier() {
		AlgorithmIdentifier hash = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE);
		AlgorithmIdentifier mask = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hash);
		AlgorithmIdentifier pSource = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_pSpecified, new DEROctetString(new byte[0]));

		ASN1Encodable parameters = new RSAESOAEPparams(hash, mask, pSource);

		return new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSAES_OAEP, parameters);
	}

	void createCMSStreamed(InputStream inputStream, OutputStream outputStream, X509Certificate sertifikat) {
		try {
			JceKeyTransRecipientInfoGenerator recipientInfoGenerator;
			if (keyEncryptionScheme == null) {
				recipientInfoGenerator = new JceKeyTransRecipientInfoGenerator(sertifikat);
			} else {
				recipientInfoGenerator = new JceKeyTransRecipientInfoGenerator(sertifikat, keyEncryptionScheme);
			}

			CMSEnvelopedDataGenerator envelopedDataGenerator = new CMSEnvelopedDataGenerator();
			envelopedDataGenerator.addRecipientInfoGenerator(recipientInfoGenerator);

			CMSEnvelopedDataStreamGenerator cmsEnvelopedDataStreamGenerator = new CMSEnvelopedDataStreamGenerator();
			cmsEnvelopedDataStreamGenerator.addRecipientInfoGenerator(recipientInfoGenerator);

			OutputEncryptor contentEncryptor = new JceCMSContentEncryptorBuilder(cmsEncryptionAlgorithm).build();
			OutputStream open = cmsEnvelopedDataStreamGenerator.open(outputStream, contentEncryptor);
			IOUtils.copyLarge(inputStream, open);
			open.close();
		} catch (CertificateEncodingException e) {
			throw new DokumentpakkingException("Feil med mottakers sertifikat", e);
		} catch (CMSException e) {
			throw new DokumentpakkingException("Kunne ikke generere Cryptographic Message Syntax for dokumentpakke", e);
		} catch (IOException e) {
			throw new DokumentpakkingException("Klarte ikke kryptere dokumentpakke", e);
		}
	}

	InputStream decryptCMSStreamed(InputStream encrypted, PrivateKey privateKey) {
		try {
			CMSEnvelopedDataParser cms;
			cms = new CMSEnvelopedDataParser(encrypted);
			RecipientInformationStore recipients = cms.getRecipientInfos();
			Collection<?> c = recipients.getRecipients();
			Iterator<?> it = c.iterator();
			if (it.hasNext()) {
				JceKeyTransEnvelopedRecipient recipient = new JceKeyTransEnvelopedRecipient(privateKey);
				RecipientInformation recipientInformation = (RecipientInformation) it.next();
				return recipientInformation.getContentStream(recipient).getContentStream();
			}
			throw new DokumentpakkingException("No recipients in CMS package.");
		} catch (CMSException | IOException e) {
			throw new DokumentpakkingException("Klarte ikke kryptere dokumentpakke", e);
		}
	}
}
