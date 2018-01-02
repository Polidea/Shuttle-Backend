package com.polidea.shuttle.configuration;

import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

import javax.sql.DataSource;
import java.io.IOException;

import static de.flapdoodle.embed.process.runtime.Network.getFreeServerPort;

@Configuration
@Profile("embeddedPostgres")
@SuppressWarnings("unused")
public class EmbeddedPostgresConfiguration {

    private final PostgresConfig postgresConfig = new PostgresConfig(
        Version.V9_5_0,
        new AbstractPostgresConfig.Net("localhost", getFreeServerPort()),
        new AbstractPostgresConfig.Storage("test"),
        new AbstractPostgresConfig.Timeout(),
        new AbstractPostgresConfig.Credentials("user", "pass")
    );

    public EmbeddedPostgresConfiguration() throws IOException {
    }

    @Bean(destroyMethod = "stop")
    public PostgresProcess postgresProcess() throws IOException {
        PostgresStarter<PostgresExecutable, PostgresProcess> postgresStarter = PostgresStarter.getDefaultInstance();
        return postgresStarter.prepare(postgresConfig).start();
    }

    @Bean(destroyMethod = "close")
    @DependsOn("postgresProcess")
    DataSource dataSource() {
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setUser(postgresConfig.credentials().username());
        dataSource.setPassword(postgresConfig.credentials().password());
        dataSource.setPortNumber(postgresConfig.net().port());
        dataSource.setServerName(postgresConfig.net().host());
        dataSource.setDatabaseName(postgresConfig.storage().dbName());
        return dataSource;
    }
}
