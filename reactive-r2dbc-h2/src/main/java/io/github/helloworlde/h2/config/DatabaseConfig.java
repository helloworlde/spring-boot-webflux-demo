package io.github.helloworlde.h2.config;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

/**
 * @author HelloWood
 */
@Configuration
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.datasource.name}")
    private String datasourceName;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(H2ConnectionConfiguration.builder()
                                                                .inMemory(datasourceName)
                                                                .username(username)
                                                                .password(password)
                                                                .build());
    }
}
