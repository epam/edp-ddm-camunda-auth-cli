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

package com.epam.digital.data.platform.auth.generator.runner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.auth.generator.BaseTest;
import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthConfigDto;
import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthDefinitionDto;
import com.epam.digital.data.platform.auth.generator.dto.configuration.ProcessDefinitionAuthConfigDto;
import com.epam.digital.data.platform.auth.generator.service.AuthService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.ApplicationArguments;

class BpmsCamundaAuthCliRunnerTest extends BaseTest {

  @Mock
  private AuthService authService;
  @Mock
  private ApplicationArguments args;

  private BpmsCamundaAuthCliRunner runner;

  @BeforeEach
  public void init() throws URISyntaxException {
    var ymlObjectMapper = new ObjectMapper(new YAMLFactory());
    ymlObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    var tokenFilePath = Paths.get(BpmsCamundaAuthCliRunnerTest.class.getResource("/testToken.txt").toURI()).toString();
    var config1 = Paths.get(BpmsCamundaAuthCliRunnerTest.class.getResource("/test-config1.yml").toURI()).toString();
    var config2 = Paths.get(BpmsCamundaAuthCliRunnerTest.class.getResource("/test-config2.yml").toURI()).toString();
    var authFilesPaths =  new StringJoiner(",").add(config1).add(config2).toString();
    runner = new BpmsCamundaAuthCliRunner(authService, ymlObjectMapper);
    lenient().when(args.getOptionValues("BPMS_URL")).thenReturn(List.of(bpmsUrl));
    lenient().when(args.getOptionValues("BPMS_TOKEN")).thenReturn(List.of(tokenFilePath));
    lenient().when(args.getOptionValues("AUTH_FILES")).thenReturn(List.of(authFilesPaths));
  }

  @Test
  void shouldCleanAndCreateAuthorizationsBasedOnConfigFiles() throws IOException {
    AuthConfigDto authConfigDto1 = new AuthConfigDto(
        new AuthDefinitionDto(List.of(
            new ProcessDefinitionAuthConfigDto("Process_1", List.of("officer", "citizen")),
            new ProcessDefinitionAuthConfigDto("business-process", List.of("officer")))
        ));
    AuthConfigDto authConfigDto2 = new AuthConfigDto(
        new AuthDefinitionDto(List.of(
            new ProcessDefinitionAuthConfigDto("Process_2", List.of("citizen"))
        )));

    runner.run(args);

    verify(authService).cleanAuthorizations(bpmsUrl, "token");
    verify(authService).createAuthorizations(bpmsUrl, "token", List.of(authConfigDto1, authConfigDto2));
  }

  @Test
  void shouldThrowExceptionWhenBpmsUrlNotFound() {
    when(args.getOptionValues("BPMS_URL")).thenReturn(null);

    var exception = assertThrows(IllegalArgumentException.class,
        () -> runner.run(args));

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo("Bpms url is not found");
  }

  @Test
  void shouldThrowExceptionWhenBpmsTokenNotFound() {
    when(args.getOptionValues("BPMS_TOKEN")).thenReturn(null);

    var exception = assertThrows(IllegalArgumentException.class,
        () -> runner.run(args));

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo("Bpms token is not found");
  }

  @Test
  void shouldThrowExceptionWhenAuthFilesNotFound() {
    when(args.getOptionValues("AUTH_FILES")).thenReturn(null);

    var exception = assertThrows(IllegalArgumentException.class,
        () -> runner.run(args));

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo("Config files are not found");
  }
}