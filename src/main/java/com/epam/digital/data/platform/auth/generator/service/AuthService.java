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

import com.epam.digital.data.platform.auth.generator.dto.configuration.AuthConfigDto;
import java.util.List;

/**
 * Service that manages authorizations based on specified config files.
 */
public interface AuthService {

  /**
   * Remove authorizations for process-instances (permissions = 'CREATE', resource_id = '*') and
   * process-definitions (permissions = 'READ', 'CREATE_INSTANCE').
   *
   * @param clientUrl bpms base url.
   * @param jwtToken  authorization token.
   */
  void cleanAuthorizations(String clientUrl, String jwtToken);

  /**
   * Create authorizations for specific roles and process definition based on config files.
   * Process-definitions -> permissions = 'READ', 'CREATE_INSTANCE'.
   * Process-instances  -> permissions = 'CREATE', resource_id = '*'
   *
   * @param clientUrl         bpms base url.
   * @param jwtToken          authorization token.
   * @param authConfigDtoList deserialized list of config yaml files.
   */
  void createAuthorizations(String clientUrl, String jwtToken, List<AuthConfigDto> authConfigDtoList);
}
