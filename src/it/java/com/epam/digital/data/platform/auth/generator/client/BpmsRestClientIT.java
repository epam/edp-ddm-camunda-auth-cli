package com.epam.digital.data.platform.auth.generator.client;

import static com.epam.digital.data.platform.auth.generator.config.WireMockConfig.bpmsUrl;
import static com.epam.digital.data.platform.auth.generator.service.BpmsAuthServiceImpl.PROCESS_DEFINITION_AUTH_SEARCH_QUERY_PARAMS;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.auth.generator.dto.rest.AuthResponseDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.AuthorizationCreateDto;
import com.epam.digital.data.platform.auth.generator.enums.Permission;
import com.epam.digital.data.platform.auth.generator.enums.Resource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class BpmsRestClientIT {

  @Autowired
  @Qualifier("bpmsWireMockServer")
  private WireMockServer bpmsWireMockServer;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private BpmsRestClient restClient;

  @Test
  public void shouldReturnAuthIds() throws JsonProcessingException {
    List<AuthResponseDto> authListResponse = List
        .of(new AuthResponseDto("authId1", null), new AuthResponseDto("authId2", null));
    bpmsWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/authorization"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withQueryParam("permissions", equalTo("READ,CREATE_INSTANCE"))
            .withQueryParam("resourceType", equalTo("6"))
            .willReturn(aResponse()
                .withBody(objectMapper.writeValueAsString(authListResponse))
                .withStatus(200))));

    List<AuthResponseDto> authorizations = restClient.searchAuthorizationsByParams(bpmsUrl, "token",
        PROCESS_DEFINITION_AUTH_SEARCH_QUERY_PARAMS);

    assertThat(authorizations).isNotNull();
    assertThat(authorizations.stream().anyMatch(a -> "authId1".equals(a.getId()))).isTrue();
    assertThat(authorizations.stream().anyMatch(a -> "authId2".equals(a.getId()))).isTrue();

  }

  @Test
  public void shouldDeleteAuthorization() {
    bpmsWireMockServer.addStubMapping(
        stubFor(delete(urlPathEqualTo("/api/authorization/authId"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .willReturn(aResponse()
                .withStatus(204))));

    restClient.deleteAuthorization(bpmsUrl, "token", "authId");
  }

  @Test
  public void shouldCreateAuthorization() throws JsonProcessingException {
    AuthorizationCreateDto authorizationCreateDto = AuthorizationCreateDto.builder()
        .resourceType(Resource.PROCESS_INSTANCE.getValue())
        .permissions(List.of("READ"))
        .resourceId("*")
        .groupId("officer")
        .type(1)
        .build();
    bpmsWireMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/authorization/create"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(authorizationCreateDto)))
            .willReturn(aResponse()
                .withStatus(204))));

    restClient.createAuthorization(bpmsUrl, "token", authorizationCreateDto);
  }
}