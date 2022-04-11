/*
 * Copyright 2021 EPAM Systems.
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

import com.epam.digital.data.platform.auth.generator.dto.rest.AuthResponseDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.AuthorizationCreateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  private static final String API = "api";
  private static final String AUTHORIZATION = "authorization";

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public List<AuthResponseDto> searchAuthorizationsByParams(String bpmsBaseUrl, String token,
      Map<String, String> queryParams) {
    log.debug("Get authorizations by {}", queryParams);
    var response = performGet(bpmsBaseUrl, token, AUTHORIZATION, queryParams);
    return deserializeResponse(response.getBody());
  }

  public void deleteAuthorization(String bpmsBaseUrl, String token, String authId) {
    log.debug("Delete authorization with id {}", authId);
    performDelete(bpmsBaseUrl, token, AUTHORIZATION, authId);
  }

  public void createAuthorization(String bpmsBaseUrl, String token, AuthorizationCreateDto createDto) {
    log.debug("Create authorization {}", createDto);
    var uri = UriComponentsBuilder.fromHttpUrl(bpmsBaseUrl).pathSegment(API)
        .pathSegment(AUTHORIZATION).pathSegment("create").build().toUri();

    perform(RequestEntity.post(uri).headers(getHeaders(token)).body(serializeAuthorizationCreateDto(createDto)));
  }

  private ResponseEntity<String> performGet(String bpmsBaseUrl, String token, String resource,
      Map<String, String> searchCriteria) {
    var uri = UriComponentsBuilder.fromHttpUrl(bpmsBaseUrl).pathSegment(API)
        .pathSegment(resource).encode();
    if (Objects.nonNull(searchCriteria)) {
      searchCriteria.forEach(uri::queryParam);
    }
    return perform(RequestEntity.get(uri.build().toUri()).headers(getHeaders(token)).build());
  }

  private ResponseEntity<String> performDelete(String bpmsBaseUrl, String token, String resource,
      String resourceId) {
    var uri = UriComponentsBuilder.fromHttpUrl(bpmsBaseUrl).pathSegment(API).pathSegment(resource)
        .pathSegment(resourceId).build().toUri();

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

  private List<AuthResponseDto> deserializeResponse(String response) {
    try {
      return objectMapper.readValue(response, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      e.clearLocation();
      throw new IllegalArgumentException("Couldn't deserialize response", e);
    }
  }

  private String serializeAuthorizationCreateDto(AuthorizationCreateDto authorizationCreateDto) {
    try {
      return objectMapper.writeValueAsString(authorizationCreateDto);
    } catch (JsonProcessingException e) {
      e.clearLocation();
      throw new IllegalArgumentException("Couldn't serialize authorizationCreateDto", e);
    }
  }
}
