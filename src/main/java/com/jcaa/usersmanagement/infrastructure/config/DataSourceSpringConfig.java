package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.persistence.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Locale;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class DataSourceSpringConfig {

  private static final String PROP_DB_ENGINE   = "${db.engine:mysql}";
  private static final String PROP_DB_HOST     = "${db.host}";
  private static final String PROP_DB_PORT     = "${db.port}";
  private static final String PROP_DB_NAME     = "${db.name}";
  private static final String PROP_DB_USERNAME = "${db.username}";
  private static final String PROP_DB_PASSWORD = "${db.password}";
  private static final String PROP_DB_SSL_MODE = "${db.ssl-mode}";

  private static final String ENGINE_POSTGRESQL = "postgresql";
  private static final String PG_JDBC_URL_TEMPLATE = "jdbc:postgresql://%s:%d/%s?sslmode=%s";

  private static final String LOG_DATASOURCE_INIT =
      "[DataSourceSpringConfig] DataSource inicializado. motor={} host={} port={}";

  @Value(PROP_DB_ENGINE)
  private String dbEngine;

  @Value(PROP_DB_HOST)
  private String dbHost;

  @Value(PROP_DB_PORT)
  private int dbPort;

  @Value(PROP_DB_NAME)
  private String dbName;

  @Value(PROP_DB_USERNAME)
  private String dbUsername;

  @Value(PROP_DB_PASSWORD)
  private String dbPassword;

  @Value(PROP_DB_SSL_MODE)
  private String dbSslMode;

  @Bean
  public DataSource dataSource() {
    final HikariConfig hikariConfig = new HikariConfig();

    if (ENGINE_POSTGRESQL.equalsIgnoreCase(dbEngine)) {
      hikariConfig.setJdbcUrl(buildPostgresJdbcUrl());
      hikariConfig.setUsername(dbUsername);
      hikariConfig.setPassword(dbPassword);
    } else {
      final DatabaseConfig config =
          new DatabaseConfig(dbHost, dbPort, dbName, dbUsername, dbPassword, dbSslMode);
      hikariConfig.setJdbcUrl(config.buildJdbcUrl());
      hikariConfig.setUsername(config.username());
      hikariConfig.setPassword(config.password());
    }

    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(2);
    hikariConfig.setConnectionTimeout(30_000);

    log.info(LOG_DATASOURCE_INIT, dbEngine, dbHost, dbPort);
    return new HikariDataSource(hikariConfig);
  }

  private String buildPostgresJdbcUrl() {
    return String.format(
        PG_JDBC_URL_TEMPLATE, dbHost, dbPort, dbName, toPostgresSslMode(dbSslMode));
  }

  private static String toPostgresSslMode(final String sslMode) {
    final String normalized = sslMode.trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "disabled" -> "disable";
      case "required" -> "require";
      default -> normalized;
    };
  }
}