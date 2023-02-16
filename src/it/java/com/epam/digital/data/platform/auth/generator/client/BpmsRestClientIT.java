/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.auth.generator.client;

import static com.epam.digital.data.platform.auth.generator.config.WireMockConfig.bpmsUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.epam.digital.data.platform.auth.generator.dto.rest.CountResultDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.ProcessDefinitionAuthDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BpmsRestClientIT {

  @Autowired
  @Qualifier("bpmsWireMockServer")
  private WireMockServer bpmsWireMockServer;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private BpmsRestClient restClient;

  @Test
  void shouldDeleteAuthorization() throws JsonProcessingException {
    var expectedCountResponse = new CountResultDto(1);
    bpmsWireMockServer.addStubMapping(
        stubFor(delete(urlPathEqualTo("/api/extended/authorizations/delete"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .willReturn(
                aResponse().withBody(objectMapper.writeValueAsString(expectedCountResponse)))));

    var result = restClient.deleteAuthorizations(bpmsUrl, "token");
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(expectedCountResponse);
  }

  @Test
  void shouldCreateProcessDefinitionAuthorizations() throws JsonProcessingException {
    var expectedCountResponse = new CountResultDto(2);
    var processDefinitionAuthDto = new ProcessDefinitionAuthDto("firstGroupId",
        "firstProcessDefinitionId");
    var processDefinitionAuthDto2 = new ProcessDefinitionAuthDto("secondGroupId",
        "secondProcessDefinitionId");
    var body = List.of(processDefinitionAuthDto, processDefinitionAuthDto2);
    bpmsWireMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/extended/authorizations/process-definition/create"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(body)))
            .willReturn(
                aResponse().withBody(objectMapper.writeValueAsString(expectedCountResponse)))));

    var result = restClient.createProcessDefinitionAuthorizations(bpmsUrl, "token", body);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(expectedCountResponse);
  }

  @Test
  void shouldCreateProcessInstanceAuthorizations() throws JsonProcessingException {
    var expectedCountResponse = new CountResultDto(1);
    var body = List.of("officer");

    bpmsWireMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/extended/authorizations/process-instance/create"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(body)))
            .willReturn(
                aResponse().withBody(objectMapper.writeValueAsString(expectedCountResponse)))));

    var result = restClient.createProcessInstanceAuthorizations(bpmsUrl, "token", body);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(expectedCountResponse);
  }
}