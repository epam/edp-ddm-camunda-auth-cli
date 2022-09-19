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

package com.epam.digital.data.platform.auth.generator.service;

import com.epam.digital.data.platform.auth.generator.client.BpmsRestClient;
import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthConfigDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.AuthResponseDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.AuthorizationCreateDto;
import com.epam.digital.data.platform.auth.generator.enums.Permission;
import com.epam.digital.data.platform.auth.generator.enums.Resource;
import com.epam.digital.data.platform.starter.logger.annotation.Confidential;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BpmsAuthServiceImpl implements AuthService {

  public static final int AUTH_TYPE_GRANT = 1;
  public static final String RESOURCE_TYPE = "resourceType";
  public static final String RESOURCE_ID = "resourceId";

  public static final Map<String, String> PROCESS_DEFINITION_AUTH_SEARCH_QUERY_PARAMS = Map.of(
      RESOURCE_TYPE, String.valueOf(Resource.PROCESS_DEFINITION.getValue()));
  public static final Map<String, String> PROCESS_INSTANCE_AUTH_SEARCH_QUERY_PARAMS = Map.of(
      RESOURCE_TYPE, String.valueOf(Resource.PROCESS_INSTANCE.getValue()),
      RESOURCE_ID, "*");
  public static final Set<String> PROCESS_DEFINITION_PERMISSIONS = Set.of(
      Permission.READ.name(), Permission.CREATE_INSTANCE.name());

  private final BpmsRestClient bpmsRestClient;

  @Override
  public void cleanAuthorizations(String bpmsUrl, @Confidential String token,
      List<AuthConfigDto> authConfigDtoList) {
    log.info("Deleting authorizations...");
    var pdAuthorizations = bpmsRestClient.searchAuthorizationsByParams(bpmsUrl, token,
            PROCESS_DEFINITION_AUTH_SEARCH_QUERY_PARAMS).stream()
        .filter(a -> new HashSet<>(a.getPermissions()).equals(PROCESS_DEFINITION_PERMISSIONS))
        .collect(Collectors.toList());
    log.info("Process Definition authorizations found: {}", pdAuthorizations.size());
    log.debug("Process Definitions details: {}", pdAuthorizations);
    var piAuthorizations = bpmsRestClient.searchAuthorizationsByParams(bpmsUrl, token,
        PROCESS_INSTANCE_AUTH_SEARCH_QUERY_PARAMS);
    log.info("Process Instance authorizations found: {}", piAuthorizations.size());
    log.debug("Process Instances details: {}", piAuthorizations);
    var authIds = Stream.of(pdAuthorizations, piAuthorizations).flatMap(Collection::stream)
        .filter(a -> !"camunda-admin".equals(a.getGroupId()))
        .map(AuthResponseDto::getId).collect(Collectors.toList());

    authIds.forEach(aId -> bpmsRestClient.deleteAuthorization(bpmsUrl, token, aId));
    log.info("Authorizations deleted: {}", authIds.size());
  }

  @Override
  public void createAuthorizations(String bpmsUrl, @Confidential String token,
      List<AuthConfigDto> authConfigDtoList) {
    log.info("Creating authorizations...");
    var pdAuthCount = createProcessDefinitionAuthorizations(bpmsUrl, token, authConfigDtoList);
    var piAuthCount = createProcessInstanceAuthorizations(bpmsUrl, token, authConfigDtoList);
    log.info("Authorizations created: {}", piAuthCount + pdAuthCount);
  }

  private int createProcessDefinitionAuthorizations(String bpmsUrl, String token,
      List<AuthConfigDto> authConfigDtoList) {
    return authConfigDtoList.stream()
        .flatMap(ac -> ac.getAuthorization().getProcessDefinitions().stream())
        .peek(pdAuthConfig -> pdAuthConfig.getRoles().forEach(role ->
            createProcessDefinitionAuthorization(bpmsUrl, token, pdAuthConfig.getProcessDefinitionId(), role)))
        .mapToInt(pdc -> pdc.getRoles().size()).sum();
  }

  private int createProcessInstanceAuthorizations(String bpmsUrl, String token,
      List<AuthConfigDto> authConfigDtoList) {
    var uniqRoles = getUniqRoles(authConfigDtoList);
    uniqRoles.forEach(role -> createProcessInstanceAuthorization(bpmsUrl, token, "*", role));
    return uniqRoles.size();
  }

  private void createProcessDefinitionAuthorization(String bpmsBaseUrl, String token,
      String processDefinitionId, String groupId) {
    AuthorizationCreateDto authCreateDto = createAuthorizationCreateDto(
        Resource.PROCESS_DEFINITION,
        Arrays.asList(Permission.CREATE_INSTANCE.name(), Permission.READ.name()), AUTH_TYPE_GRANT,
        processDefinitionId, groupId);
    bpmsRestClient.createAuthorization(bpmsBaseUrl, token, authCreateDto);
  }

  private void createProcessInstanceAuthorization(String bpmsBaseUrl, String token,
      String processInstanceId, String groupId) {
    AuthorizationCreateDto authCreateDto = createAuthorizationCreateDto(
        Resource.PROCESS_INSTANCE, Collections.singletonList(Permission.CREATE.name()),
        AUTH_TYPE_GRANT, processInstanceId, groupId);
    bpmsRestClient.createAuthorization(bpmsBaseUrl, token, authCreateDto);
  }

  private AuthorizationCreateDto createAuthorizationCreateDto(Resource resource,
      List<String> permissions, int authType, String resourceId, String groupId) {
    return AuthorizationCreateDto.builder()
        .resourceType(resource.getValue())
        .permissions(permissions)
        .type(authType)
        .resourceId(resourceId)
        .groupId(groupId)
        .build();
  }

  private List<String> getUniqRoles(List<AuthConfigDto> authConfigDtoList) {
    return authConfigDtoList.stream()
        .flatMap(ac -> ac.getAuthorization().getProcessDefinitions().stream())
        .flatMap(pdc -> pdc.getRoles().stream()).distinct().collect(Collectors.toList());
  }
}
