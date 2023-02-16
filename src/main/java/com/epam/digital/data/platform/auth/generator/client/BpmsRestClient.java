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

import com.epam.digital.data.platform.auth.generator.dto.rest.CountResultDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.ProcessDefinitionAuthDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class BpmsRestClient {

  private static final String BASE_PATH = "api/extended/authorizations";
  private static final String DELETE = "delete";
  private static final String CREATE = "create";
  private static final String PROCESS_DEFINITION = "process-definition";
  private static final String PROCESS_INSTANCE = "process-instance";

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public CountResultDto deleteAuthorizations(String bpmsBaseUrl, String token) {
    log.debug("Delete authorizations");
    var response = performDelete(bpmsBaseUrl, token);
    return deserializeResponse(response.getBody());
  }

  public CountResultDto createProcessInstanceAuthorizations(String bpmsBaseUrl, String token,
      List<String> roles) {
    log.debug("Create process instance authorizations {}", roles);
    var response = performPost(bpmsBaseUrl, token, PROCESS_INSTANCE, roles);
    return deserializeResponse(response.getBody());
  }

  public CountResultDto createProcessDefinitionAuthorizations(String bpmsBaseUrl, String token,
      List<ProcessDefinitionAuthDto> body) {
    log.debug("Create process definition authorizations {}", body);
    var response = performPost(bpmsBaseUrl, token, PROCESS_DEFINITION, body);
    return deserializeResponse(response.getBody());
  }

  private ResponseEntity<String> performPost(String bpmsBaseUrl, String token, String path,
      Object body) {
    var uri = UriComponentsBuilder.fromHttpUrl(bpmsBaseUrl)
        .pathSegment(BASE_PATH.split("/"))
        .pathSegment(path)
        .pathSegment(CREATE)
        .build().toUri();

    return perform(RequestEntity.post(uri).headers(getHeaders(token))
        .body(serializeAuthorizationCreateDto(body)));
  }

  private ResponseEntity<String> performDelete(String bpmsBaseUrl, String token) {
    var uri = UriComponentsBuilder.fromHttpUrl(bpmsBaseUrl)
        .pathSegment(BASE_PATH.split("/"))
        .pathSegment(DELETE)
        .build().toUri();

    return perform(RequestEntity.delete(uri).headers(getHeaders(token)).build());
  }

  private ResponseEntity<String> perform(RequestEntity<?> requestEntity) {
    return restTemplate.exchange(requestEntity, String.class);
  }

  private HttpHeaders getHeaders(String token) {
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", token);
    return headers;
  }

  private CountResultDto deserializeResponse(String response) {
    try {
      return objectMapper.readValue(response, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      e.clearLocation();
      throw new IllegalArgumentException("Couldn't deserialize response", e);
    }
  }

  private String serializeAuthorizationCreateDto(Object requestBody) {
    try {
      return objectMapper.writeValueAsString(requestBody);
    } catch (JsonProcessingException e) {
      e.clearLocation();
      throw new IllegalArgumentException("Couldn't serialize request body", e);
    }
  }
}