package com.picpay.mockserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Log4j2
@SpringBootApplication
public class MockServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> ctx.getBean(MockServer.class).getMockedServices().forEach(this::start);
    }

    @Bean
    public MockServer mockServer() throws URISyntaxException, IOException {
        final URI uri = this.getClass().getClassLoader().getResource("application.yml").toURI();
        final String yml = new String(Files.readAllBytes(Paths.get(uri)));
        return new ObjectMapper(new YAMLFactory()).readValue(yml, MockServer.class);
    }

    private void start(final MockedServices mockedService) {
        log.info(String.format("Starting: %s:%s", mockedService.getServiceName(), mockedService.getPort()));

        if (isPortAvailable(mockedService.getPort())) {
            try {
                mockedService.setServer(buildServer(mockedService));
                mockedService.start();
                mockedService.addStubsMapping();
                mockedService.setRunning(true);
            } catch (Exception e) {
                log.info(String.format("Failed to start: %s:%s", mockedService.getServiceName(), mockedService.getPort()), e);
                stop(mockedService);
            }
        } else {
            log.info(String.format("Did not start: %s:%s, port already in use.", mockedService.getServiceName(), mockedService.getPort()));
        }
    }

    private WireMockServer buildServer(final MockedServices mockedService) {
        return new WireMockServer(WireMockConfiguration.options()
            .extensions(new ResponseTemplateTransformer(false))
            .dynamicPort()
            .port(mockedService.getPort())
            .disableRequestJournal()
            .asynchronousResponseEnabled(true));
    }

    private void stop(final MockedServices mockedService) {
        log.info(String.format("Stopping: %s:%s", mockedService.getServiceName(), mockedService.getPort()));
        mockedService.stop();

        while (mockedService.isRunning()) {
            log.warn("Shutting down...");

            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (Exception e) {

            }
        }

        mockedService.setRunning(false);
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port), 1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
