package no.nav.dokdisteformidling.qdist013.itest.config;

import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Profile({"itest"})
public class LocalTestCacheConfig {

	public static final String TKAT020_CACHE = "tkat020Cache";
	public static final String TKAT021_CACHE = "tkat021Cache";
	public static final String STS_CACHE = "stsCache";

	@Bean
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				new CaffeineCache(TKAT020_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(TKAT021_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(STS_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.build()),
				new CaffeineCache(LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.build())
		));
		return manager;
	}
}

