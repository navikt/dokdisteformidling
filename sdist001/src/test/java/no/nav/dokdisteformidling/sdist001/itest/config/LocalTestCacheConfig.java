package no.nav.dokdisteformidling.sdist001.itest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static no.nav.dokdisteformidling.config.cache.LokalCacheConfig.AZURE_TOKEN_CACHE;

@Configuration
@Profile({"itest"})
public class LocalTestCacheConfig {

    @Bean
    CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(singletonList(
                new CaffeineCache(AZURE_TOKEN_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(0, MINUTES)
                        .build())
        ));
        return manager;
    }
}

