package org.rus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.rus.product.infrastructure")
@EnableTransactionManagement
@EnableJpaAuditing
public class Main {

    public static void main(String[] args) {
        // Set default time zone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SpringApplication app = new SpringApplication(Main.class);
        Environment env = app.run(args).getEnvironment();

        try {
            logApplicationStartup(env);
        } catch (UnknownHostException e) {
            log.error("Error during application startup", e);
        }
    }

    private static void logApplicationStartup(Environment env) throws UnknownHostException {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }

        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        log.info("""
            
            ----------------------------------------------------------
            \tApplication '{}' is running! Access URLs:
            \tLocal: \t\t{}://localhost:{}{}
            \tExternal: \t{}://{}:{}{}
            \tProfile(s): \t{}
            \tSwagger UI: \t{}://localhost:{}{}/swagger-ui/index.html
            ----------------------------------------------------------""",
                env.getProperty("spring.application.name", "Product Service"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles().length == 0 ? "default" : String.join(",", env.getActiveProfiles()),
                protocol,
                serverPort,
                contextPath
        );
    }

}