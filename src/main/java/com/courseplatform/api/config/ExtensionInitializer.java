package com.courseplatform.api.config;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Attempts to enable the pg_trgm extension on startup in a crash-resistant way.
 * Some hosted Postgres providers disallow extension creation; we must not fail startup.
 */
@Component
public class ExtensionInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionInitializer.class);

    private final DataSource dataSource;

    public ExtensionInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // CREATE EXTENSION is safe to run repeatedly; if permissions are missing it will throw.
            st.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm;");
            logger.info("pg_trgm extension ensured");
        } catch (Exception e) {
            // Do not rethrow — log and continue so restricted clouds don't crash the app.
            logger.warn("Could not ensure pg_trgm extension. Fuzzy search will be disabled if the extension is unavailable. Reason: {}", e.getMessage());
            logger.debug("Full exception while creating pg_trgm extension", e);
        }
    }
}
