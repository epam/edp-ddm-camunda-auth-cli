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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.auth.generator.BaseTest;
import com.epam.digital.data.platform.auth.generator.client.BpmsRestClient;
import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthConfigDto;
import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthDefinitionDto;
import com.epam.digital.data.platform.auth.generator.dto.configuration.ProcessDefinitionAuthConfigDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.CountResultDto;
import com.epam.digital.data.platform.auth.generator.dto.rest.ProcessDefinitionAuthDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class BpmsAuthServiceImplTest extends BaseTest {

  @Mock
  private BpmsRestClient bpmsRestClient;

  private AuthService authService;

  @BeforeEach
  public void init() {
    authService = new BpmsAuthServiceImpl(bpmsRestClient);
  }

  @Test
  void shouldCleanAuthorizations() {
    var countResult = new CountResultDto(1);

    when(bpmsRestClient.deleteAuthorizations(bpmsUrl, jwtToken)).thenReturn(countResult);
    authService.cleanAuthorizations(bpmsUrl, jwtToken);

    verify(bpmsRestClient).deleteAuthorizations(bpmsUrl, jwtToken);
  }

  @Test
  void shouldCreateAuthorizations() {
    var roles = List.of("officer", "citizen");
    AuthConfigDto authConfigDto1 = new AuthConfigDto(
        new AuthDefinitionDto(List.of(
            new ProcessDefinitionAuthConfigDto("Process_1", roles),
            new ProcessDefinitionAuthConfigDto("business-process", List.of("officer")))
        ));
    AuthConfigDto authConfigDto2 = new AuthConfigDto(
        new AuthDefinitionDto(List.of(
            new ProcessDefinitionAuthConfigDto("Process_2", List.of("citizen"))
        )));
    var processDefinitionAuthDto = new ProcessDefinitionAuthDto("officer",
        "Process_1");
    var processDefinitionAuthDto2 = new ProcessDefinitionAuthDto("citizen",
        "Process_1");
    var processDefinitionAuthDto3 = new ProcessDefinitionAuthDto("officer",
        "business-process");
    var processDefinitionAuthDto4 = new ProcessDefinitionAuthDto("citizen",
        "Process_2");
    var definitionBody = List.of(processDefinitionAuthDto, processDefinitionAuthDto2,
        processDefinitionAuthDto3, processDefinitionAuthDto4);

    when(bpmsRestClient.createProcessDefinitionAuthorizations(bpmsUrl, jwtToken,
        definitionBody)).thenReturn(new CountResultDto(4));
    when(bpmsRestClient.createProcessInstanceAuthorizations(bpmsUrl, jwtToken, roles)).thenReturn(
        new CountResultDto(2));
    authService.createAuthorizations(bpmsUrl, jwtToken, List.of(authConfigDto1, authConfigDto2));

    verify(bpmsRestClient).createProcessDefinitionAuthorizations(bpmsUrl, jwtToken, definitionBody);
    verify(bpmsRestClient).createProcessInstanceAuthorizations(bpmsUrl, jwtToken, roles);
  }
}