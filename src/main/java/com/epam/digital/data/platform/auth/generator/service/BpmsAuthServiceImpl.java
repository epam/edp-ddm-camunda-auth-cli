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

package com.epam.digital.data.platform.auth.generator.service;

import com.epam.digital.data.platform.auth.generator.client.BpmsRestClient;
import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthConfigDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.ProcessDefinitionAuthDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BpmsAuthServiceImpl implements AuthService {

  public static final int RETRY_DELAY_MILLIS = 20000;

  private final BpmsRestClient bpmsRestClient;


  @Override
  @Retryable(backoff = @Backoff(delay = RETRY_DELAY_MILLIS))
  public void createAuthorizations(String bpmsUrl, String token, List<AuthConfigDto> authConfigDtos) {
    log.info("Creating authorizations...");
    var pdAuthCount = createProcessDefinitionAuthorizations(bpmsUrl, token, authConfigDtos);
    var piAuthCount = createProcessInstanceAuthorizations(bpmsUrl, token, authConfigDtos);
    log.info("Authorizations created: {}", Long.sum(piAuthCount, pdAuthCount));
  }

  @Override
  @Retryable(backoff = @Backoff(delay = RETRY_DELAY_MILLIS))
  public void cleanAuthorizations(String bpmsUrl, String token) {
    log.info("Deleting authorizations...");
    var response = bpmsRestClient.deleteAuthorizations(bpmsUrl, token);
    log.info("Authorizations deleted: {}", response.getCount());
  }

  private long createProcessDefinitionAuthorizations(String bpmsUrl, String token,
      List<AuthConfigDto> authList) {
    var body = authList.stream()
        .flatMap(ac -> ac.getAuthorization().getProcessDefinitions().stream())
        .flatMap(pdAuthConfig -> pdAuthConfig.getRoles().stream().map(role -> {
          var processDefinitionAuthDto = new ProcessDefinitionAuthDto();
          processDefinitionAuthDto.setProcessDefinitionId(pdAuthConfig.getProcessDefinitionId());
          processDefinitionAuthDto.setGroupId(role);
          return processDefinitionAuthDto;
        })).collect(Collectors.toList());

    var response = bpmsRestClient.createProcessDefinitionAuthorizations(bpmsUrl, token, body);
    return response.getCount();
  }

  private long createProcessInstanceAuthorizations(String bpmsUrl, String token,
      List<AuthConfigDto> authList) {
    var uniqueRoles = getUniqueRoles(authList);
    var response = bpmsRestClient.createProcessInstanceAuthorizations(bpmsUrl, token, uniqueRoles);
    return response.getCount();
  }

  private List<String> getUniqueRoles(List<AuthConfigDto> authConfigDtoList) {
    return authConfigDtoList.stream()
        .flatMap(ac -> ac.getAuthorization().getProcessDefinitions().stream())
        .flatMap(pdc -> pdc.getRoles().stream())
        .distinct()
        .collect(Collectors.toList());
  }
}