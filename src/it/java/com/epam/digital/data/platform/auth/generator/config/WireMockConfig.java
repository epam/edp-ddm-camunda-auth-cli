package com.epam.digital.data.platform.auth.generator.config;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WireMockConfig {

  public static final String bpmsUrl = "http://localhost:8081";

  @Bean(destroyMethod = "stop")
  @Qualifier("bpmsWireMockServer")
  public WireMockServer bpmsWireMockServer() throws MalformedURLException {
    URL url = new URL(bpmsUrl);
    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(url.getPort()));
    WireMock.configureFor(url.getHost(), url.getPort());
    wireMockServer.start();
    return wireMockServer;
  }
}
