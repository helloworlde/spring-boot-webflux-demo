package io.github.helloworlde.mysql.config;

import com.github.jasync.r2dbc.mysql.JasyncConnectionFactory;
import com.github.jasync.sql.db.SSLConfiguration;
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

import java.nio.charset.Charset;

/**
 * @author HelloWood
 */
@Configuration
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.datasource.host}")
    private String host;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.port}")
    private Integer port;

    @Value("${spring.datasource.database}")
    private String database;

    @Override
    public ConnectionFactory connectionFactory() {
        SSLConfiguration sslConfiguration = new SSLConfiguration();
        com.github.jasync.sql.db.Configuration configuration = new com.github.jasync.sql.db.Configuration(
                username,
                host,
                port,
                password,
                database,
                sslConfiguration,
                Charset.forName("UTF-8")
        );

        MySQLConnectionFactory connectionFactory = new MySQLConnectionFactory(configuration);
        return new JasyncConnectionFactory(connectionFactory);
    }
}
