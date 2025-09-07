package blog.sammi.lab.notes.infrastructure.config;

import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

@Configuration
public class MemcachedConfig {
    
    @Value("${memcached.host:localhost}")
    private String host;
    
    @Value("${memcached.port:11211}")
    private int port;
    
    @Bean
    public MemcachedClient memcachedClient() throws IOException {
        return new MemcachedClient(new InetSocketAddress(host, port));
    }
}
