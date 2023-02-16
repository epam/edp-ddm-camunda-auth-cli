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

import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthConfigDto;
import com.epam.digital.data.platform.auth.generator.service.AuthService;
import com.epam.digital.data.platform.auth.generator.util.ArgsParserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class BpmsCamundaAuthCliRunner implements ApplicationRunner {

  private final AuthService authService;
  private final ObjectMapper ymlObjectMapper;

  @Override
  public void run(ApplicationArguments args) throws IOException {
    var bpmsUrl = ArgsParserUtil.getSingleParamOrThrow("BPMS_URL", "Bpms url is not found", args);
    var bpmsToken = getBpmsToken(args);
    var authConfigDtos = getAuthConfigurations(args);

    authService.cleanAuthorizations(bpmsUrl, bpmsToken);
    authService.createAuthorizations(bpmsUrl, bpmsToken, authConfigDtos);
  }

  private String getBpmsToken(ApplicationArguments args) throws IOException {
    var filePath = ArgsParserUtil.getSingleParamOrThrow("BPMS_TOKEN", "Bpms token is not found", args);
    return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
  }

  private List<AuthConfigDto> getAuthConfigurations(ApplicationArguments args) {
    var filePaths = ArgsParserUtil.getSingleParamOrThrow("AUTH_FILES", "Config files are not found", args);
    var files = Stream.of(filePaths.split(",")).map(File::new).collect(Collectors.toList());
    return deserializeAuthConfigDtos(files);
  }

  private List<AuthConfigDto> deserializeAuthConfigDtos(List<File> files) {
    return files.stream().map(this::deserializeAuthConfigDto).collect(Collectors.toList());
  }

  private AuthConfigDto deserializeAuthConfigDto(File file) {
    try {
      return ymlObjectMapper.readValue(file, AuthConfigDto.class);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Invalid configuration: %s", file));
    }
  }
}
