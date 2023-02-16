package no.nav.dokdisteformidling.qdist013.itest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.SAF_JOURNALPOST_QDIST013_CACHE;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.STS_CACHE;

@Configuration
@Profile({"itest"})
public class LocalTestCacheConfig {

    @Bean
    CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Arrays.asList(
                new CaffeineCache(STS_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(0, MINUTES)
                        .maximumSize(0)
                        .build()),
                new CaffeineCache(LIGHTWEIGHT_SAF_JOURNALPOST_QDIST013_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(0, SECONDS)
                        .maximumSize(0)
                        .build()),
                new CaffeineCache(SAF_JOURNALPOST_QDIST013_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(0, SECONDS)
                        .build())
        ));
        return manager;
    }
}

