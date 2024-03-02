package earth.angelson.security.config;

import earth.angelson.security.AuthorizationInterceptor;
import earth.angelson.security.cache.TokenCacheService;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class SecurityConfiguration {

	@Bean
	public TokenCacheService tokenCacheService() {
		return new TokenCacheService("http://localhost:8081/account/info");
	}

	@Bean
	public AuthorizationInterceptor authorizationInterceptor(TokenCacheService tokenCacheService) {
		return new AuthorizationInterceptor(tokenCacheService);
	}

	@Bean
	public CacheManager caffeineCacheManager() {
		CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
		caffeineCacheManager
			.setCaffeine(Caffeine.newBuilder()
				.expireAfterWrite(15, TimeUnit.MINUTES));
		return caffeineCacheManager;
	}

}
