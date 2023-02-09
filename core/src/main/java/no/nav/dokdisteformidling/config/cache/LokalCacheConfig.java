package no.nav.dokdisteformidling.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
@EnableCaching
public class LokalCacheConfig {

	public static final String AZURE_TOKEN_CACHE = "AzureToken";
	public static final String STS_CACHE = "stsCache";
	public static final String LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE = "LightweightSafJournalpostQdist013Cache";
	public static final String SAF_JOURNALPOST_QDIST013_CACHE = "SafJournalpostQueryServiceImplQdist013Cache";

	@Bean
	@Primary
	@Profile({"nais", "local"})
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				new CaffeineCache(AZURE_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(55, MINUTES)
						.build()),
				new CaffeineCache(STS_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(55, MINUTES)
						.build()),
				new CaffeineCache(LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(30, SECONDS)
						.build()),
				new CaffeineCache(SAF_JOURNALPOST_QDIST013_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(30, SECONDS)
						.build())
		));
		return manager;
	}
}
