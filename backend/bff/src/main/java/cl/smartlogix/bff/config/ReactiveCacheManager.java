package cl.smartlogix.bff.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public class ReactiveCacheManager {

    private final Cache<String, Object> cache;

    public ReactiveCacheManager() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats()
                .build();
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<T> getOrFetch(String key, Duration ttl, Supplier<Mono<T>> fetcher) {
        T cached = (T) cache.getIfPresent(key);
        if (cached != null) {
            return Mono.just(cached);
        }
        return fetcher.get()
                .doOnNext(value -> cache.put(key, value))
                .cache();
    }

    public void evict(String key) {
        cache.invalidate(key);
    }

    public void evictByPrefix(String prefix) {
        cache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
    }
}
